package ru.runa.bpm.graph.node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.FlyweightAttribute;

import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.context.exe.ContextInstance;
import ru.runa.bpm.context.exe.ISelectable;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.bpm.instantiation.Delegation;
import ru.runa.commons.ApplicationContextFactory;

@SuppressWarnings("unchecked")
public class MultiInstanceState extends ProcessState {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(MultiInstanceState.class);

    public static final String OUT_VARIABLE_NAME = "InternalActionHandlerOutVar";

    @Override
    public NodeType getNodeType() {
        return NodeType.MultiInstance;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token superProcessToken = executionContext.getToken();

        // if this process has late binding
        if (subProcessDefinition == null && subProcessName != null) {
            SubProcessResolver subProcessResolver = ApplicationContextFactory.getSubProcessResolver();
            List<FlyweightAttribute> attributes = new ArrayList<FlyweightAttribute>();
            attributes.add(new FlyweightAttribute("name", subProcessName));
            Element subProcessElement = new DefaultElement("sub-process");
            subProcessElement.setAttributes(attributes);

            subProcessDefinition = subProcessResolver.findSubProcess(subProcessElement);
            if (subProcessDefinition == null) {
                throw new RuntimeException("Unable to start subprocess '" + subProcessName + "' because it does not exist");
            }
        }

        String miVarName = null;
        String miRelationDiscriminatorTypeParam = null;
        String miVarSubName = null;
        String miDiscriminatorType = null;

        {
            String varName = null, groupName = null, relationName = null;
            String varSubName = null, groupSubName = null, relationSubName = null;
            for (VariableMapping vm : variableMappings) {
                if ("multiinstance-vars".equals(vm.getAccess())) {
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

        ContextInstance superContextInstance = executionContext.getContextInstance();
        long forkProcessesCount = -1;

        Object discriminatorValue = null;

        if (miDiscriminatorType != null) {
            if ("variable".equals(miDiscriminatorType) && miVarName != null) {
                discriminatorValue = superContextInstance.getVariable(miVarName, superProcessToken);
            } else if ("group".equals(miDiscriminatorType) && miVarName != null) {
                discriminatorValue = invokeHelperAction(executionContext, "ru.runa.wf.InternalGetActorsByGroupActionHandler", miVarName, null);
            } else if ("relation".equals(miDiscriminatorType) && miVarName != null && miRelationDiscriminatorTypeParam != null) {
                discriminatorValue = invokeHelperAction(executionContext, "ru.runa.wf.InternalGetActorsByRelationActionHandler", miVarName,
                        miRelationDiscriminatorTypeParam);
            }
        } else {
            for (VariableMapping variableMapping : variableMappings) {
                if (variableMapping.isMultiinstanceLink() && variableMapping.isReadable()) {
                    String variableName = variableMapping.getName();
                    discriminatorValue = superContextInstance.getVariable(variableName, superProcessToken);
                    if (discriminatorValue != null) {
                        miVarSubName = variableMapping.getMappedName();
                        break;
                    }
                }
            }
        }
        if (discriminatorValue != null) {
            if (discriminatorValue.getClass().isArray()) {
                forkProcessesCount = Array.getLength(discriminatorValue);
            } else if (discriminatorValue instanceof List) {
                forkProcessesCount = ((List<?>) discriminatorValue).size();
            } else {
                throw new RuntimeException("Invalid discriminatorValue type " + discriminatorValue.getClass());
            }
        }

        if (forkProcessesCount <= 0) {
            leave(executionContext);
            log.warn("Leaving multiinstance due to 0 fork count, discriminatorValue = " + discriminatorValue);
        }

        for (int idx = 0; idx < forkProcessesCount; idx++) {
            // create the subprocess
            ProcessInstance subProcessInstance = superProcessToken.createSubProcessInstance(subProcessDefinition);

            // fire the subprocess created event
            fireEvent(Event.EVENTTYPE_SUBPROCESS_CREATED, executionContext);

            ContextInstance subContextInstance = subProcessInstance.getContextInstance();
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is readable
                if (variableMapping.isReadable()) {
                    if (variableMapping.isMultiinstanceLink()) {
                        String variableName = variableMapping.getName();
                        Object value = superContextInstance.getVariable(variableName, superProcessToken);
                        String mappedName = variableMapping.getMappedName();
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value);
                        if (value != null) {
                            if (value.getClass().isArray()) {
                                Object[] array = (Object[]) value;
                                if (array.length > idx) {
                                    subContextInstance.setVariable(executionContext, mappedName, array[idx]);
                                } else {
                                    log.warn("Array has insufficient length for parameter");
                                }
                            } else if (value instanceof List) {
                                List<?> list = (List<?>) value;
                                if (list.size() > idx) {
                                    subContextInstance.setVariable(executionContext, mappedName, list.get(idx));
                                } else {
                                    log.warn("List has insufficient size for parameter");
                                }
                            } else {
                                throw new RuntimeException("Invalid MultiinstanceLink value type " + value.getClass());
                            }
                        }
                    } else {
                        // the variable is copied from the super process
                        // variable name
                        // to the sub process mapped name
                        String variableName = variableMapping.getName();
                        Object value = superContextInstance.getVariable(variableName, superProcessToken);
                        String mappedName = variableMapping.getMappedName();
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value);
                        if (value != null) {
                            subContextInstance.setVariable(executionContext, mappedName, value);
                        }
                    }
                }

                if (discriminatorValue != null) {
                    Object value;
                    if (discriminatorValue.getClass().isArray()) {
                        value = Array.get(discriminatorValue, idx);
                    } else if (discriminatorValue instanceof List) {
                        value = ((List<?>) discriminatorValue).get(idx);
                    } else {
                        throw new RuntimeException("Invalid discriminatorValue type " + discriminatorValue.getClass());
                    }
                    if (value instanceof ISelectable) {
                        value = ((ISelectable) value).getValue();
                    }
                    log.debug("setting discriminator var '" + miVarName + "' to sub process var '" + miVarSubName + "': " + value);
                    subContextInstance.setVariable(executionContext, miVarSubName, value);
                }
            }

            // TODO session
            ApplicationContextFactory.getCurrentSession().save(
                    new StartedSubprocesses(superProcessToken.getProcessInstance(), subProcessInstance, this));
            // send the signal to start the subprocess
            subProcessInstance.signal(new ExecutionContext(subProcessDefinition, subProcessInstance));
        }
    }

    private List<String> invokeHelperAction(ExecutionContext executionContext, String className, String name, String param) {
        try {
            ContextInstance contextInstance = executionContext.getContextInstance();
            Token token = executionContext.getToken();

            Delegation delegation = new Delegation(className);
            String conf = "";
            if (name.startsWith("${") && name.endsWith("}")) {
                name = name.substring(2, name.length() - 1);
                conf = "<name>" + contextInstance.getVariable(name, token) + "</name>";
            } else {
                conf = "<name>" + name + "</name>";
            }
            if (param != null) {
                if (param.startsWith("${") && param.endsWith("}")) {
                    param = param.substring(2, param.length() - 1);
                    conf += "<param>" + contextInstance.getVariable(param, token) + "</param>";
                } else {
                    conf += "<param>" + param + "</param>";
                }
            }
            delegation.setConfiguration(conf);

            Action action = new Action();
            action.setDelegation(delegation);

            action.execute(executionContext);

            // String actorsStr = (String)
            // executionContext.getVariable(OUT_VARIABLE_NAME);
            // return actorsStr.split("\t");
            return (List<String>) executionContext.getTransientVariable(OUT_VARIABLE_NAME);
        } catch (Exception e) {
            this.raiseException(e, executionContext);
            return null;
        }
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        Token superProcessToken = executionContext.getToken();
        Set<ProcessInstance> subprocesses = new TreeSet<ProcessInstance>(new ProcessInstanceComparator());
        subprocesses.addAll(superProcessToken.getSubProcessMultiInstance(this));
        for (ProcessInstance subprocess : subprocesses) {
            if (!subprocess.hasEnded()) {
                return;
            }
        }
        super.leave(executionContext, getDefaultLeavingTransition());
    }

    @Override
    protected void performLeave(ExecutionContext executionContext, Transition transition) {
        Token superProcessToken = executionContext.getToken();
        Set<ProcessInstance> subprocesses = new TreeSet<ProcessInstance>(new ProcessInstanceComparator());
        subprocesses.addAll(superProcessToken.getSubProcessMultiInstance(this));

        // feed the readable variableInstances
        if (!subprocesses.isEmpty()) {
            ContextInstance superContextInstance = executionContext.getContextInstance();
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is writable
                if (variableMapping.isWritable()) {
                    if (variableMapping.isMultiinstanceLink()) {
                        String mappedName = variableMapping.getMappedName();
                        Object[] value = new Object[subprocesses.size()];
                        int idx = 0;
                        for (ProcessInstance subprocess : subprocesses) {
                            value[idx++] = subprocess.getContextInstance().getVariable(mappedName);
                        }
                        String variableName = variableMapping.getName();
                        log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                        if (value != null) {
                            superContextInstance.setVariable(executionContext, variableName, value, superProcessToken);
                        }
                    } else {
                        // the variable is copied from the sub process mapped
                        // name
                        // to the super process variable name
                        ContextInstance subContextInstance = subprocesses.iterator().next().getContextInstance();
                        String mappedName = variableMapping.getMappedName();
                        Object value = subContextInstance.getVariable(mappedName);
                        String variableName = variableMapping.getName();
                        log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                        if (value != null) {
                            superContextInstance.setVariable(executionContext, variableName, value, superProcessToken);
                        }
                    }
                }
            }
        }

        // fire the subprocess ended event
        fireEvent(Event.EVENTTYPE_SUBPROCESS_END, executionContext);

        // remove the subprocess reference
        superProcessToken.setSubProcessInstance(null);

        // We replaced the normal log generation of super.leave() by creating
        // the log here
        // and overriding the addNodeLog method with an empty version
        superProcessToken.addLog(new ProcessStateLog(this, superProcessToken.getNodeEnterDate(), new Date(), subprocesses.isEmpty() ? null
                : subprocesses.iterator().next()));
    }

    private class ProcessInstanceComparator implements Comparator<ProcessInstance> {

        @Override
        public int compare(ProcessInstance o1, ProcessInstance o2) {
            if (o1.getId() == o2.getId()) {
                return 0;
            }
            return o1.getId() < o2.getId() ? -1 : 1;
        }

    }

}
