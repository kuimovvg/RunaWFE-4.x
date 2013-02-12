package ru.runa.gpd.lang.model;

import java.util.Collection;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.decision.IDecisionProvider;
import ru.runa.wfe.extension.decision.GroovyDecisionHandler;

public class Decision extends Node implements Delegable, Active {
    public Decision() {
        setDelegationClassName(GroovyDecisionHandler.class.getName());
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.DECISION;
    }

    @Override
    protected void validate() {
        super.validate();
        if (isDelegable()) {
            IDecisionProvider provider = HandlerRegistry.getProvider(this);
            Collection<String> modelTransitionNames = provider.getTransitionNames(this);
            for (Transition transition : getLeavingTransitions()) {
                if (!modelTransitionNames.remove(transition.getName())) {
                    addWarning("decision.unreachableTransition", transition.getName());
                }
            }
            for (String modelTransitionName : modelTransitionNames) {
                addError("decision.transitionDoesNotExist", modelTransitionName);
            }
        }
    }
}
