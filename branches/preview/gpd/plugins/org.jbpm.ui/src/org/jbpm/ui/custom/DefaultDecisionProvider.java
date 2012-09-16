package ru.runa.bpm.ui.custom;

import java.util.HashSet;
import java.util.Set;

import ru.runa.bpm.ui.common.model.Decision;

public class DefaultDecisionProvider extends DelegableProvider implements IDecisionProvider {

    public Set<String> getTransitionNames(Decision decision) {
        return new HashSet<String>();
    }
    
    public void transitionRenamed(Decision decision, String oldName, String newName) {
        
    }

    public String getDefaultTransitionName(Decision decision) {
        return null;
    }
    
}
