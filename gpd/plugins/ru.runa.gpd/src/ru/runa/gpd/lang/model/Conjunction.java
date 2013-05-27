package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;

public class Conjunction extends Node {
    private boolean minimizedView = false;

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("minimizedView".equals(name)) {
            return Objects.equal(value, isMinimizedView());
        }
        return false;
    }


    public boolean isMinimizedView() {
        return minimizedView;
    }

    public void setMinimizedView(boolean minimazedView) {
        this.minimizedView = minimazedView;
        firePropertyChange(PROPERTY_MINIMAZED_VIEW, !minimizedView, minimizedView);
    }

}
