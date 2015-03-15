package ru.runa.wfe.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.lang.utils.MultiInstanceParameters;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultiProcessState extends SubProcessState {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient ProcessFactory processFactory;
    @Autowired
    private transient NodeProcessDAO nodeProcessDAO;

    @Override
    public NodeType getNodeType() {
        return NodeType.MULTI_SUBPROCESS;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        MultiInstanceParameters parameters = new MultiInstanceParameters(executionContext, this);
        int subprocessesCount = TypeConversionUtil.getListSize(parameters.getDiscriminatorValue());
        List<Process> subProcesses = Lists.newArrayList();
        ProcessDefinition subProcessDefinition = getSubProcessDefinition();
        for (int index = 0; index < subprocessesCount; index++) {
            Map<String, Object> variables = Maps.newHashMap();
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is readable
                String variableName = variableMapping.getName();
                if (variableMapping.isReadable()) {
                    Object value = executionContext.getVariableProvider().getValue(variableName);
                    String mappedName = variableMapping.getMappedName();
                    if (value != null) {
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value + " of "
                                + value.getClass());
                    } else {
                        log.warn("super process var '" + variableName + "' is null (ignored mapping to '" + mappedName + "')");
                        continue;
                    }
                    if (variableMapping.isMultiinstanceLink()) {
                        variables.put(mappedName, TypeConversionUtil.getListValue(value, index));
                    } else {
                        variables.put(mappedName, value);
                    }
                }
                Object value = TypeConversionUtil.getListValue(parameters.getDiscriminatorValue(), index);
                if (value instanceof ISelectable) {
                    value = ((ISelectable) value).getValue();
                }
                log.debug("setting discriminator var '" + parameters.getDiscriminatorVariableName() + "' to sub process var '"
                        + parameters.getIteratorVariableName() + "': " + value);
                variables.put(parameters.getIteratorVariableName(), value);
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
                        list.set(nodeProcess.getIndex(), subExecutionContext.getVariableProvider().getValue(subprocessVariableName));
                    } else {
                        value = subExecutionContext.getVariableProvider().getValue(subprocessVariableName);
                    }
                    log.debug("copying sub process var '" + subprocessVariableName + "' to process var '" + processVariableName + "': " + value);
                    executionContext.setVariableValue(processVariableName, value);
                }
            }
            executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), nodeProcess.getSubProcess()));
            if (executionContext.getActiveSubprocesses().size() == 0) {
                log.debug("Leaving multisubprocess state");
                super.leave(executionContext, transition);
            }
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
                        if (variable == null || variable.getDefinition().getFormatNotNull() instanceof ListFormat) {
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
