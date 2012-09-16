package ru.runa.jbpm.ui.actions;

import ru.runa.bpm.ui.view.PropertiesView;

public class OpenProperties extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return PropertiesView.ID;
    }

}
