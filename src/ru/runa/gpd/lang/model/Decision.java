package ru.runa.gpd.lang.model;

import java.util.Collection;

import ru.runa.gpd.handler.CustomizationRegistry;
import ru.runa.gpd.handler.decision.IDecisionProvider;
import ru.runa.wfe.handler.decision.GroovyDecisionHandler;

public class Decision extends DescribableNode implements Delegable, Active {
    public Decision() {
        setDelegationClassName(GroovyDecisionHandler.class.getName());
    }

    @Override
    public String getDelegationType() {
        return DECISION_HANDLER;
    }

    @Override
    protected void validate() {
        super.validate();
        IDecisionProvider provider = CustomizationRegistry.getProvider(this);
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
