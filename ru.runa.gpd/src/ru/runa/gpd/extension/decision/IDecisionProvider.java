package ru.runa.gpd.extension.decision;

import java.util.Set;

import ru.runa.gpd.lang.model.Decision;

public interface IDecisionProvider {

    public Set<String> getTransitionNames(Decision decision);
    
    public void transitionRenamed(Decision decision, String oldName, String newName);
    
    public String getDefaultTransitionName(Decision decision);
    
}
