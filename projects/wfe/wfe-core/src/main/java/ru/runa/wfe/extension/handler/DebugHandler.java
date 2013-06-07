package ru.runa.wfe.extension.handler;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Empty handler. Can be used as a stub for old action handlers with mapping
 * defined in {@link ClassLoaderUtil}.
 * 
 * @author Dofs
 */
public class DebugHandler extends CommonHandler {
    private static final Log log = LogFactory.getLog(DebugHandler.class);
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        log.debug("Executing in process " + variableProvider.getProcessId());
        log.debug("data: " + configuration);
        return null;
    }

}
