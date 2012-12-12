package ru.runa.wfe.lang;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SubProcessState extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(SubProcessState.class);
    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_SUBPROCESS_CREATED, Event.EVENTTYPE_SUBPROCESS_END,
            Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_BEFORE_SIGNAL, Event.EVENTTYPE_AFTER_SIGNAL };

    private String subProcessName;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessFactory processFactory;

    @Override
    public NodeType getNodeType() {
        return NodeType.Subprocess;
    }

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(subProcessName, "subProcessName");
    }

    public String getSubProcessName() {
        return subProcessName;
    }

    public void setSubProcessName(String subProcessName) {
        this.subProcessName = subProcessName;
    }

    protected ProcessDefinition getSubProcessDefinition(ExecutionContext executionContext) {
        return processDefinitionLoader.getDefinition(subProcessName);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        ProcessDefinition subProcessDefinition = getSubProcessDefinition(executionContext);
        // create the subprocess
        Map<String, Object> variables = Maps.newHashMap();
        for (VariableMapping variableMapping : variableMappings) {
            // if this variable mapping is readable
            if (variableMapping.isReadable()) {
                // the variable is copied from the super process variable
                // name to the sub process mapped name
                String variableName = variableMapping.getName();
                Object value = executionContext.getVariable(variableName);
                if (value != null) {
                    String mappedName = variableMapping.getMappedName();
                    log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value);
                    variables.put(mappedName, value);
                }
            }
        }
        processFactory.startSubprocess(executionContext, subProcessDefinition, variables);
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        performLeave(executionContext);
        super.leave(executionContext, transition);
    }

    protected void performLeave(ExecutionContext executionContext) {
        List<Process> childProcesses = executionContext.getChildProcesses();
        if (childProcesses.size() != 1) {
            throw new InternalApplicationException("ProcessState has " + childProcesses + " (instead of 1 instance) at leave stage!");
        }
        Process subProcess = childProcesses.get(0);
        ExecutionContext subExecutionContext = new ExecutionContext(getSubProcessDefinition(executionContext), subProcess);
        for (VariableMapping variableMapping : variableMappings) {
            // if this variable access is writable
            if (variableMapping.isWritable()) {
                // the variable is copied from the sub process mapped name
                // to the super process variable name
                String mappedName = variableMapping.getMappedName();
                Object value = subExecutionContext.getVariable(mappedName);
                if (value != null) {
                    String variableName = variableMapping.getName();
                    log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                    executionContext.setVariable(variableName, value);
                }
            }
        }

        // fire the subprocess ended event
        fireEvent(executionContext, Event.EVENTTYPE_SUBPROCESS_END);
        executionContext.addLog(new SubprocessEndLog(this, subProcess));
    }

}
