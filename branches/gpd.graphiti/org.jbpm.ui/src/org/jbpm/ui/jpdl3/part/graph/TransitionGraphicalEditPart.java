package org.jbpm.ui.jpdl3.part.graph;

import java.beans.PropertyChangeEvent;

import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.model.Transition;
import org.jbpm.ui.jpdl3.model.ActionNode;
import org.jbpm.ui.jpdl3.model.TaskState;


public class TransitionGraphicalEditPart extends org.jbpm.ui.common.part.graph.TransitionGraphicalEditPart {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (PROPERTY_NAME.equals(evt.getPropertyName()) && evt.getSource() instanceof Transition) {
        	refreshVisuals();
        }
    }
    
    @Override
    protected void refreshVisuals() {
        Transition transition = getModel();
        if (transition.getSource() instanceof TaskState && !PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
            String label;
            if (((TaskState) transition.getSource()).hasMultipleOutputTransitions()) {
                label = transition.getName();
            } else {
                label = "";
            }
            getFigure().setLabelText(label);
        } 
        if (transition.getSource() instanceof ActionNode){
            String label;
            if (transition.getSource().getLeavingTransitions().size() > 1) {
                label = transition.getName();
            } else {
                label = "";
            }
            getFigure().setLabelText(label);
        }
    	super.refreshVisuals();
    }

}
