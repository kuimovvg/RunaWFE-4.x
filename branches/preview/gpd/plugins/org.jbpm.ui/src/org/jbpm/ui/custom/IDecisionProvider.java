package ru.runa.bpm.ui.custom;

import java.util.Set;

import ru.runa.bpm.ui.common.model.Decision;

public interface IDecisionProvider {

    public Set<String> getTransitionNames(Decision decision);
    
    public void transitionRenamed(Decision decision, String oldName, String newName);
    
    public String getDefaultTransitionName(Decision decision);
    
}
