package ru.runa.wfe.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultiProcessState extends SubProcessState {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;
    @Autowired
    private ProcessFactory processFactory;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;

    @Override
    public NodeType getNodeType() {
        return NodeType.MULTI_SUBPROCESS;
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
        if ("variable".equals(miDiscriminatorType) && miVarName != null) {
            discriminatorValue = executionContext.getVariableValue(miVarName);
        } else if ("group".equals(miDiscriminatorType) && miVarName != null) {
            Object miVar = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(), miVarName);
            Group group = TypeConversionUtil.convertTo(Group.class, miVar);
            discriminatorValue = Lists.newArrayList(executorDAO.getGroupActors(group));
        } else if ("relation".equals(miDiscriminatorType) && miVarName != null && miRelationDiscriminatorTypeParam != null) {
            String relationName = (String) ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(), miVarName);
            Object relationParam = ExpressionEvaluator.evaluateVariableNotNull(executionContext.getVariableProvider(),
                    miRelationDiscriminatorTypeParam);
            Executor rightExecutor = TypeConversionUtil.convertTo(Executor.class, relationParam);
            discriminatorValue = getActorsByRelation(relationName, rightExecutor);
        }
        if (discriminatorValue == null) {
            for (VariableMapping variableMapping : variableMappings) {
                if (variableMapping.isMultiinstanceLink() && variableMapping.isReadable()) {
                    String variableName = variableMapping.getName();
                    discriminatorValue = executionContext.getVariableValue(variableName);
                    if (discriminatorValue != null) {
                        miVarSubName = variableMapping.getMappedName();
                        break;
                    }
                }
            }
        }
        if (discriminatorValue == null) {
            // if (SystemProperties.isV3CompatibilityMode()) {
            // discriminatorValue = new ArrayList<Object>();
            // } else {
            throw new RuntimeException("discriminatorValue == null");
            // }
        }
        int subprocessesCount = TypeConversionUtil.getArraySize(discriminatorValue);
        List<Process> subProcesses = Lists.newArrayList();
        ProcessDefinition subProcessDefinition = getSubProcessDefinition();
        for (int index = 0; index < subprocessesCount; index++) {
            Map<String, Object> variables = Maps.newHashMap();
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is readable
                String variableName = variableMapping.getName();
                if (variableMapping.isReadable()) {
                    Object value = executionContext.getVariableValue(variableName);
                    String mappedName = variableMapping.getMappedName();
                    if (value != null) {
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value + " of "
                                + value.getClass());
                    } else {
                        log.warn("super process var '" + variableName + "' is null (ignored mapping to '" + mappedName + "')");
                        continue;
                    }
                    if (variableMapping.isMultiinstanceLink()) {
                        variables.put(mappedName, TypeConversionUtil.getArrayVariable(value, index));
                    } else {
                        variables.put(mappedName, value);
                    }
                }
                Object value = TypeConversionUtil.getArrayVariable(discriminatorValue, index);
                if (value instanceof ISelectable) {
                    value = ((ISelectable) value).getValue();
                }
                log.debug("setting discriminator var '" + miVarName + "' to sub process var '" + miVarSubName + "': " + value);
                variables.put(miVarSubName, value);
            }
            Process subProcess = processFactory.createSubprocess(executionContext, subProcessDefinition, variables, index);
            subProcesses.add(subProcess);
        }
        for (VariableMapping variableMapping : variableMappings) {
            if (variableMapping.isMultiinstanceLink() && variableMapping.isWritable()) {
                log.debug("setting to null writable var '" + variableMapping.getName());
                executionContext.setVariableValue(variableMapping.getName(), null);
            }
        }
        for (Process subprocess : subProcesses) {
            ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocess);
            processFactory.startSubprocess(executionContext, subExecutionContext);
        }
        if (subProcesses.size() == 0) {
            log.debug("Leaving multisubprocess state due to 0 subprocesses");
            super.leave(executionContext, null);
        }
    }

    private List<Actor> getActorsByRelation(String relationName, Executor paramExecutor) {
        // TODO add reversed option in GPD
        List<Executor> executors = Lists.newArrayList(paramExecutor);
        Relation relation = relationDAO.getNotNull(relationName);
        List<RelationPair> relationPairs = relationPairDAO.getExecutorsRelationPairsLeft(relation, executors);
        Set<Actor> actors = new HashSet<Actor>();
        for (RelationPair pair : relationPairs) {
            Executor executor = pair.getRight();
            if (executor instanceof Actor) {
                actors.add((Actor) executor);
            } else if (executor instanceof Group) {
                actors.addAll(executorDAO.getGroupActors((Group) executor));
            }
        }
        return Lists.newArrayList(actors);
    }

    @Override
    public void leave(ExecutionContext subExecutionContext, Transition transition) {
        ExecutionContext executionContext = getParentExecutionContext(subExecutionContext);
        NodeProcess nodeProcess = subExecutionContext.getParentNodeProcess();
        if (nodeProcess.getIndex() == null) {
            // pre AddSubProcessIndexColumn mode
            leaveBackCompatiblePre410(executionContext, transition);
        } else {
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is writable
                if (variableMapping.isWritable()) {
                    String subprocessVariableName = variableMapping.getMappedName();
                    String processVariableName = variableMapping.getName();
                    WfVariable variable = executionContext.getVariableProvider().getVariableNotNull(processVariableName);
                    Object value;
                    if (variableMapping.isMultiinstanceLink()) {
                        value = TypeConversionUtil.convertTo(List.class, variable.getValue());
                        if (value == null) {
                            value = Lists.newArrayList();
                        }
                        List<Object> list = (List<Object>) value;
                        while (list.size() <= nodeProcess.getIndex()) {
                            list.add(null);
                        }
                        list.set(nodeProcess.getIndex(), subExecutionContext.getVariableValue(subprocessVariableName));
                    } else {
                        value = subExecutionContext.getVariableValue(subprocessVariableName);
                    }
                    log.debug("copying sub process var '" + subprocessVariableName + "' to process var '" + processVariableName + "': " + value);
                    executionContext.setVariableValue(processVariableName, value);
                }
            }
        }
        executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), nodeProcess.getSubProcess()));
        if (executionContext.getActiveSubprocesses().size() == 0) {
            log.debug("Leaving multisubprocess state");
            super.leave(executionContext, transition);
        }
    }

    private void leaveBackCompatiblePre410(ExecutionContext executionContext, Transition transition) {
        if (executionContext.getActiveSubprocesses().size() == 0) {
            log.debug("Leaving multisubprocess state [in backcompatibility mode] due to 0 active subprocesses");
            List<Process> subprocesses = nodeProcessDAO.getSubprocesses(executionContext.getProcess(), executionContext.getToken().getNodeId(),
                    executionContext.getToken(), null);
            if (!subprocesses.isEmpty()) {
                ProcessDefinition subProcessDefinition = getSubProcessDefinition();
                for (VariableMapping variableMapping : variableMappings) {
                    // if this variable access is writable
                    if (variableMapping.isWritable()) {
                        String subprocessVariableName = variableMapping.getMappedName();
                        String processVariableName = variableMapping.getName();
                        WfVariable variable = executionContext.getVariableProvider().getVariable(processVariableName);
                        Object value;
                        if (variable == null || variable.getFormatNotNull() instanceof ListFormat) {
                            value = new ArrayList<Object>();
                            for (Process subprocess : subprocesses) {
                                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocess);
                                ((List<Object>) value).add(subExecutionContext.getVariableValue(subprocessVariableName));
                            }
                        } else {
                            if (subprocesses.size() > 0) {
                                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocesses.get(0));
                                value = subExecutionContext.getVariableValue(subprocessVariableName);
                            } else {
                                value = null;
                            }
                        }
                        log.debug("copying sub process var '" + subprocessVariableName + "' to process var '" + processVariableName + "': " + value);
                        executionContext.setVariableValue(processVariableName, value);
                    }
                }
            }
            for (Process subProcess : subprocesses) {
                executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), subProcess));
            }
            super.leave(executionContext, transition);
        }
    }

}
