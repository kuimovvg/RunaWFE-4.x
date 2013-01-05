package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.util.Delay;

public abstract class Node extends NamedGraphElement implements Describable {
    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            return ((ITimed) this).getTimer().getDelay();
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return ((ITimed) this).getTimer().getAction();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            ((ITimed) this).getTimer().setDelay((Delay) value);
        } else if (PROPERTY_TIMER_ACTION.equals(id)) {
            ((ITimed) this).getTimer().setAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public void removeChild(GraphElement child) {
        super.removeChild(child);
        if (child instanceof Timer) {
            Transition timeoutTransition = getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME);
            if (timeoutTransition != null) {
                removeLeavingTransition(timeoutTransition);
            }
        }
    }

    @Override
    protected void validate() {
        super.validate();
        if (!(this instanceof StartState) && !(this instanceof Timer && getParent() instanceof ITimed)) {
            if (getArrivingTransitions().size() == 0) {
                addError("noInputTransitions");
            }
        }
        if (!(this instanceof EndState) && !(this instanceof EndTokenState)) {
            if (getLeavingTransitions().size() == 0) {
                if (this instanceof Timer) {
                    return; // TODO current in jpdl
                }
                addError("noOutputTransitions");
            }
        }
    }

    public String getNextTransitionName() {
        int runner = 1;
        while (true) {
            String candidate = "tr" + runner;
            if (getTransitionByName(candidate) == null) {
                return candidate;
            }
            runner++;
        }
    }

    public Transition getTransitionByName(String name) {
        List<Transition> transitions = getLeavingTransitions();
        for (Transition transition : transitions) {
            if (name.equals(transition.getName())) {
                return transition;
            }
        }
        return null;
    }

    public void addLeavingTransition(Transition transition) {
        boolean renameAfterAddition = getTransitionByName(transition.getName()) != null;
        addChild(transition);
        if (renameAfterAddition) {
            transition.setName(getNextTransitionName());
        }
        firePropertyChange(NODE_LEAVING_TRANSITION_ADDED, null, transition);
        Node target = transition.getTarget();
        if (target != null) {
            target.firePropertyChange(NODE_ARRIVING_TRANSITION_ADDED, null, transition);
        }
        updateLeavingTransitions();
    }

    public void removeLeavingTransition(Transition transition) {
        removeChild(transition);
        firePropertyChange(NODE_LEAVING_TRANSITION_REMOVED, null, transition);
        Node target = transition.getTarget();
        if (target != null) {
            target.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, transition);
        }
        updateLeavingTransitions();
    }

    private void updateLeavingTransitions() {
        if (isExclusive()) {
            boolean exclusiveFlow = getLeavingTransitions().size() > 1;
            for (Transition leavingTransition : getLeavingTransitions()) {
                leavingTransition.setExclusiveFlow(exclusiveFlow);
            }
        }
    }

    public List<Transition> getLeavingTransitions() {
        return getChildren(Transition.class);
    }

    public List<Transition> getArrivingTransitions() {
        List<Transition> arrivingTransitions = new ArrayList<Transition>();
        List<Node> allNodes = getProcessDefinition().getNodesRecursive();
        for (Node node : allNodes) {
            List<Transition> leaving = node.getLeavingTransitions();
            for (Transition transition : leaving) {
                if (this.equals(transition.getTarget())) {
                    arrivingTransitions.add(transition);
                }
            }
        }
        return arrivingTransitions;
    }

    public final boolean canAddArrivingTransition(Node source) {
        List<Transition> transitions = getArrivingTransitions();
        return allowArrivingTransition(source, transitions);
    }

    public final boolean canReconnectArrivingTransition(Transition transition, Node source) {
        List<Transition> transitions = getArrivingTransitions();
        transitions.remove(transition);
        return allowArrivingTransition(source, transitions);
    }

    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        if (this.equals(source)) {
            // Disable self referencing
            return false;
        }
        return true;
    }

    public final boolean canReconnectLeavingTransition(Transition transition, Node target) {
        List<Transition> transitions = getLeavingTransitions();
        transitions.remove(transition);
        return allowLeavingTransition(target, transitions);
    }

    public final boolean canAddLeavingTransition(Node target) {
        List<Transition> transitions = getLeavingTransitions();
        return allowLeavingTransition(target, transitions);
    }

    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return true;
    }

    public boolean isExclusive() {
        return false;
    }
}
