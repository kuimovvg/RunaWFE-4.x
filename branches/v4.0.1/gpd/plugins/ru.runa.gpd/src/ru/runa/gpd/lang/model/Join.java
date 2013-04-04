package ru.runa.gpd.lang.model;

import java.util.List;

public class Join extends Node {
    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return super.allowLeavingTransition(target, transitions) && transitions.size() == 0;
    }
}
