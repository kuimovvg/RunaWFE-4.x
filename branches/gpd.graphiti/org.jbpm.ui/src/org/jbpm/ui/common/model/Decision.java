package org.jbpm.ui.common.model;

import java.util.Collection;

import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.custom.IDecisionProvider;

public class Decision extends DescribableNode implements Delegable, Active {

    public Decision() {
        setDelegationClassName("ru.runa.wf.jbpm.delegation.decision.BSFDecisionHandler");
    }

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
