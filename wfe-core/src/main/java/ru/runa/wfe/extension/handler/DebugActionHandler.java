package ru.runa.wfe.extension.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

/**
 * Empty action handler. Can be used as a stub for old action handlers with
 * mapping defined in {@link ClassLoaderUtil}.
 * 
 * @author Dofs
 */
public class DebugActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(DebugActionHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        log.debug("Executing in " + executionContext.getProcess());
        log.debug("data: " + configuration);
    }

}
