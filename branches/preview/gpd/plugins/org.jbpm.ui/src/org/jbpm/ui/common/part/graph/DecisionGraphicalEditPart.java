package ru.runa.bpm.ui.common.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.custom.IDecisionProvider;

public class DecisionGraphicalEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public Decision getModel() {
        return (Decision) super.getModel();
    }
    
    @Override
    protected String getTooltipMessage() {
        return getModel().getDelegationConfiguration();
    }

    @SuppressWarnings("unchecked")
	@Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (NODE_LEAVING_TRANSITION_ADDED.equals(evt.getPropertyName()) 
                || NODE_LEAVING_TRANSITION_REMOVED.equals(evt.getPropertyName())
                || PROPERTY_CONFIGURATION.equals(evt.getPropertyName())) {
            Decision decision = getModel();
            IDecisionProvider provider = CustomizationRegistry.getProvider(decision);
            for (TransitionGraphicalEditPart part : (List<TransitionGraphicalEditPart>) getSourceConnections()) {
                if (part.getFigure().setDefaultFlow(part.getModel().getName().equals(provider.getDefaultTransitionName(decision)))) {
                    part.refreshVisuals();
                }
            }
        }
        if (PROPERTY_CONFIGURATION.equals(evt.getPropertyName())) {
            updateTooltip(getFigure());
        }
    }
}
