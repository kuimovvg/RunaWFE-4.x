package ru.runa.jbpm.ui.actions;

public class OpenErrorLogView extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return "org.eclipse.pde.runtime.LogView";
    }

}
