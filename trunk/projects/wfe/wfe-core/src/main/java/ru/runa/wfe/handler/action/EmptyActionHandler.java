package ru.runa.wfe.handler.action;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.ExecutionContext;

/**
 * Empty action handler. Can be used as a stub for old action handlers with mapping defined in {@link ClassLoaderUtil}.
 * 
 * @author Dofs
 */
public class EmptyActionHandler implements ActionHandler {

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
    }

}
