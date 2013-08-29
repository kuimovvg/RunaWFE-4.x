package ru.runa.gpd.lang.model;

import java.util.List;

public class StartState extends FormNode {
    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return false;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return true;
    }
}
