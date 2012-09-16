package ru.runa.bpm.ui.common.model;

import java.util.List;

public class EndState extends Node {

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return false;
    }

}
