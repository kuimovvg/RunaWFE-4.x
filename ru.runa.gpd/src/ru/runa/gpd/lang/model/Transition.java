package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;

import com.google.common.base.Objects;

public class Transition extends NamedGraphElement implements Active {
    private Node target;
    private List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();
    private boolean exclusiveFlow;
    private boolean defaultFlow;

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

    public boolean isExclusiveFlow() {
        return exclusiveFlow;
    }

    public boolean isDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(boolean defaultFlow) {
        if (this.defaultFlow != defaultFlow) {
            this.defaultFlow = defaultFlow;
            firePropertyChange(TRANSITION_FLOW, !defaultFlow, defaultFlow);
        }
    }

    public void setExclusiveFlow(boolean exclusiveFlow) {
        if (this.exclusiveFlow != exclusiveFlow) {
            this.exclusiveFlow = exclusiveFlow;
            firePropertyChange(TRANSITION_FLOW, !exclusiveFlow, exclusiveFlow);
        }
    }

    @Override
    public void setName(String name) {
        Node source = getSource();
        if (source == null) {
            return;
        }
        List<Transition> list = source.getLeavingTransitions();
        for (Transition transition : list) {
            if (Objects.equal(name, transition.getName())) {
                return;
            }
        }
        super.setName(name);
    }

    public Node getSource() {
        return (Node) getParent();
    }

    public Node getTarget() {
        return target;
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
        DESCRIPTORS.add(new PropertyDescriptor(PROPERTY_SOURCE, Localization.getString("Transition.property.source")));
        DESCRIPTORS.add(new PropertyDescriptor(PROPERTY_TARGET, Localization.getString("Transition.property.target")));
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
        return getParent().toString() + " -> (" + getName() + ") -> " + target.toString();
    }
    
    public String getLabel() {
        if (getSource() instanceof Decision || getSource() instanceof ExclusiveGateway) {
            return getName();
        }
        if (PluginConstants.TIMER_TRANSITION_NAME.equals(getName()) && getSource() instanceof ITimed) {
            Timer timer = ((ITimed) getSource()).getTimer();
            return timer != null ? timer.getDelay().toString() : "";
        }
        if (getSource() instanceof TaskState) {
            int count = 0;
            for (Transition transition : getSource().getLeavingTransitions()) {
                if (!PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
                    count++;
                }
            }
            if (count > 1) {
                return getName();
            }
        }
        return "";
    }
}
