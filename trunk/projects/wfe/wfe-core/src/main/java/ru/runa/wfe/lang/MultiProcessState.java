package ru.runa.wfe.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.collect.Maps;

public class MultiProcessState extends SubProcessState {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(MultiProcessState.class);

    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private ProcessFactory processFactory;

    @Override
    public NodeType getNodeType() {
        return NodeType.MultiSubprocess;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        String miVarName = null;
        String miRelationDiscriminatorTypeParam = null;
        String miVarSubName = null;
        String miDiscriminatorType = null;

        {
            String varName = null, groupName = null, relationName = null;
            String varSubName = null, groupSubName = null, relationSubName = null;
            for (VariableMapping vm : variableMappings) {
                if (vm.isMultiinstanceVariable()) {
                    if ("tabVariableProcessVariable".equals(vm.getName())) {
                        varName = vm.getMappedName();
                    } else if ("tabVariableSubProcessVariable".equals(vm.getName())) {
                        varSubName = vm.getMappedName();
                    } else if ("tabGroupName".equals(vm.getName())) {
                        groupName = vm.getMappedName();
                    } else if ("tabGroupSubProcessVariable".equals(vm.getName())) {
                        groupSubName = vm.getMappedName();
                    } else if ("tabRelationName".equals(vm.getName())) {
                        relationName = vm.getMappedName();
                    } else if ("tabRelationParam".equals(vm.getName())) {
                        miRelationDiscriminatorTypeParam = vm.getMappedName();
                    } else if ("tabRelationSubProcessVariable".equals(vm.getName())) {
                        relationSubName = vm.getMappedName();
                    } else if ("typeMultiInstance".equals(vm.getName())) {
                        miDiscriminatorType = vm.getMappedName();
                    }
                }
            }
            if ("variable".equals(miDiscriminatorType)) {
                miVarName = varName;
                miVarSubName = varSubName;
            } else if ("group".equals(miDiscriminatorType)) {
                miVarName = groupName;
                miVarSubName = groupSubName;
            } else if ("relation".equals(miDiscriminatorType)) {
                miVarName = relationName;
                miVarSubName = relationSubName;
            }
        }

        Object discriminatorValue = null;
        if (miDiscriminatorType != null) {
            if ("variable".equals(miDiscriminatorType) && miVarName != null) {
                discriminatorValue = executionContext.getVariable(miVarName);
            } else if ("group".equals(miDiscriminatorType) && miVarName != null) {
                Group group = executorDAO.getGroup((String) ExpressionEvaluator.evaluateVariable(executionContext, miVarName));
                discriminatorValue = getActorCodes(executorDAO.getGroupActors(group));
            } else if ("relation".equals(miDiscriminatorType) && miVarName != null && miRelationDiscriminatorTypeParam != null) {
                String relationName = (String) ExpressionEvaluator.evaluateVariable(executionContext, miVarName);
                String rightExecutorName = (String) ExpressionEvaluator.evaluateVariable(executionContext, miRelationDiscriminatorTypeParam);
                discriminatorValue = getActorCodes(getActorsByRelation(relationName, rightExecutorName));
            }
        } else {
            for (VariableMapping variableMapping : variableMappings) {
                if (variableMapping.isMultiinstanceLink() && variableMapping.isReadable()) {
                    String variableName = variableMapping.getName();
                    discriminatorValue = executionContext.getVariable(variableName);
                    if (discriminatorValue != null) {
                        miVarSubName = variableMapping.getMappedName();
                        break;
                    }
                }
            }
        }
        if (discriminatorValue == null) {
            throw new RuntimeException("discriminatorValue == null");
        }
        int forkProcessesCount = TypeConversionUtil.getArraySize(discriminatorValue);
        if (forkProcessesCount == 0) {
            leave(executionContext);
            log.warn("Leaving multiinstance due to 0 fork count");
        }
        for (int idx = 0; idx < forkProcessesCount; idx++) {
            Map<String, Object> variables = Maps.newHashMap();
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is readable
                if (variableMapping.isReadable()) {
                    String variableName = variableMapping.getName();
                    Object value = executionContext.getVariable(variableName);
                    String mappedName = variableMapping.getMappedName();
                    if (value != null) {
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value);
                    } else {
                        log.warn("super process var '" + variableName + "' is null (ignored mapping to '" + mappedName + "')");
                        continue;
                    }
                    if (variableMapping.isMultiinstanceLink()) {
                        variables.put(mappedName, TypeConversionUtil.getArrayVariable(value, idx));
                    } else {
                        variables.put(mappedName, value);
                    }
                }
                Object value = TypeConversionUtil.getArrayVariable(discriminatorValue, idx);
                if (value instanceof ISelectable) {
                    value = ((ISelectable) value).getValue();
                }
                log.debug("setting discriminator var '" + miVarName + "' to sub process var '" + miVarSubName + "': " + value);
                variables.put(miVarSubName, value);
            }

            processFactory.startSubprocess(executionContext, getSubProcessDefinition(executionContext), variables);
        }
    }

    private List<String> getActorCodes(Collection<Actor> actors) {
        List<String> actorCodes = new ArrayList<String>(actors.size());
        for (Actor actor : actors) {
            actorCodes.add(String.valueOf(actor.getCode()));
        }
        return actorCodes;
    }

    private Set<Actor> getActorsByRelation(String relationName, String rightExecutorName) {
        List<Executor> executorRightList = new ArrayList<Executor>();
        try {
            Executor executor = executorDAO.getExecutor(rightExecutorName);
            executorRightList.add(executor);
            // if (executor instanceof Actor) {
            // BatchPresentation batchPresentation =
            // AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
            // executorRightList.addAll(executorDAO.getExecutorGroups(executor,
            // batchPresentation));
            // }
            List<RelationPair> relationPairList = relationDAO.getExecutorsRelationPairsRight(relationName, executorRightList);
            Set<Actor> actorList = new HashSet<Actor>();
            for (RelationPair pair : relationPairList) {
                Executor executorleft = pair.getLeft();
                if (executorleft instanceof Actor) {
                    actorList.add((Actor) executorleft);
                } else if (executorleft instanceof Group) {
                    actorList.addAll(executorDAO.getGroupActors((Group) executorleft));
                }
            }
            return actorList;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        for (Process subprocess : executionContext.getChildProcesses()) {
            if (!subprocess.hasEnded()) {
                return;
            }
        }
        super.leave(executionContext, transition);
    }

    @Override
    protected void performLeave(ExecutionContext executionContext) {
        List<Process> subprocesses = executionContext.getChildProcesses();
        if (!subprocesses.isEmpty()) {
            ProcessDefinition subProcessDefinition = getSubProcessDefinition(executionContext);
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is writable
                if (variableMapping.isWritable()) {
                    if (variableMapping.isMultiinstanceLink()) {
                        String mappedName = variableMapping.getMappedName();
                        List<Object> value = new ArrayList<Object>();
                        for (Process subprocess : subprocesses) {
                            ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocess);
                            value.add(subExecutionContext.getVariable(mappedName));
                        }
                        String variableName = variableMapping.getName();
                        log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                        if (value != null) {
                            executionContext.setVariable(variableName, value);
                        }
                    } else {
                        // the variable is copied from the sub process mapped
                        // name
                        // to the super process variable name
                        String mappedName = variableMapping.getMappedName();
                        ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocesses.get(0));
                        Object value = subExecutionContext.getVariable(mappedName);
                        String variableName = variableMapping.getName();
                        log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                        if (value != null) {
                            executionContext.setVariable(variableName, value);
                        }
                    }
                }
            }
        }

        // fire the subprocess ended event
        fireEvent(executionContext, Event.EVENTTYPE_SUBPROCESS_END);

        for (Process subProcess : subprocesses) {
            executionContext.addLog(new SubprocessEndLog(this, subProcess));
        }
    }

}
