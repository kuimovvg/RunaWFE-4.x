package org.jbpm.ui.common.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.resource.Messages;

public class Transition extends NamedGraphElement implements Active {

    private Node target;

    private List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

    public List<Bendpoint> getBendpoints() {
        return bendpoints;
    }

    public void setBendpoints(List<Bendpoint> bendpoints) {
        this.bendpoints = bendpoints;
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, 1);
    }

    public void addBendpoint(int index, Bendpoint bendpoint) {
        getBendpoints().add(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void removeBendpoint(int index) {
        getBendpoints().remove(index);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void setBendpoint(int index, Bendpoint bendpoint) {
        getBendpoints().set(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    @Override
    protected boolean canSetNameTo(String name) {
        Node source = getSource();
        if (source == null) {
            return false;
        }
        List<Transition> list = source.getLeavingTransitions();
        for (Transition transition : list) {
            if (name.equals(transition.getName())) {
                return false;
            }
        }
        return true;
    }

    public Node getSource() {
        return (Node) getParent();
    }

    public Node getTarget() {
        return target;
    }

    public String getTargetName() {
        return target.getName();
    }

    public void setTarget(Node target) {
        Node old = this.target;
        this.target = target;
        if (old != null) {
            old.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, this);
        }
        if (this.target != null) {
            this.target.firePropertyChange(NODE_ARRIVING_TRANSITION_ADDED, null, this);
        }
    }

    private static final List<IPropertyDescriptor> DESCRIPTORS = new ArrayList<IPropertyDescriptor>();
    static {
        DESCRIPTORS.add(new PropertyDescriptor(PROPERTY_SOURCE, Messages.getString("Transition.property.source")));
        DESCRIPTORS.add(new PropertyDescriptor(PROPERTY_TARGET, Messages.getString("Transition.property.target")));
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SOURCE.equals(id) && getSource() != null) {
            return getSource().getName();
        } else if (PROPERTY_TARGET.equals(id) && getTarget() != null) {
            return target != null ? target.getName() : "";
        }
        return super.getPropertyValue(id);
    }

    @Override
    public String toString() {
        if (getParent() == null || target == null) {
            return "not_completed";
        }
        return ((Node) getParent()).getName() + " -> " + target.getName();
    }

}
