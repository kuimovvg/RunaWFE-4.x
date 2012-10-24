package ru.runa.jbpm.ui.actions;

import org.jbpm.ui.view.PropertiesView;

public class OpenProperties extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return PropertiesView.ID;
    }

}
