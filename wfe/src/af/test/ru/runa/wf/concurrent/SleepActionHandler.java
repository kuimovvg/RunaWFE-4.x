package ru.runa.wf.concurrent;

import ru.runa.ConfigurationException;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;

/**
 * Test action handler. Sleeps 2 seconds.
 */
public class SleepActionHandler implements ActionHandler {

    private static final long serialVersionUID = 1L;

    public void setConfiguration(String configurationName) throws ConfigurationException {
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Thread.sleep(2000);
    }
}
