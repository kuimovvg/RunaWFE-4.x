package ru.runa.wfe.lang;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class ServiceTask extends Node {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ServiceTask.class);
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.ActionNode;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        try {
            executionContext.addLog(new ActionLog(this));
            ActionHandler actionHandler = delegation.getInstance();
            log.info("Executing " + this); // TODO debug
            actionHandler.execute(executionContext);
            leave(executionContext);
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
    }

}
