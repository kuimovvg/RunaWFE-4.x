package ru.runa.gpd.formeditor.action;

import ru.runa.gpd.ui.action.OpenViewBaseAction;

public class OpenFormComponentsViewAction extends OpenViewBaseAction {
    public static final String VIEW_ID = "ru.runa.gpd.formeditor.ftl.formComponentsView";

    @Override
    protected String getViewId() {
        return VIEW_ID;
    }

}
