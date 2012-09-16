package ru.runa.bpm.ui.common.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Node extends NamedGraphElement {

    @Override
    protected void validate() {
        super.validate();
        if (!(this instanceof StartState)) {
            if (getArrivingTransitions().size() == 0) {
                addError("noInputTransitions");
            }
        }
        if (!(this instanceof EndState)) {
            if (getLeavingTransitions().size() == 0) {
                addError("noOutputTransitions");
            }
        }
        /* allow duplicated transitions to attach different action handlers
        Set<String> uniqueNames = new HashSet<String>();
        for (Transition transition : getLeavingTransitions()) {
            if (uniqueNames.contains(transition.getName())) {
                addError("model.validation.duplicatedTransitionName", transition.getName());
            }
            uniqueNames.add(transition.getName());
        }
        */
        if (getName() != null && getName().contains("/")) {
            // for jbpm
            addError("invalidNameCharacters");
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
    }

    public void removeLeavingTransition(Transition transition) {
        removeChild(transition);
        firePropertyChange(NODE_LEAVING_TRANSITION_REMOVED, null, transition);
        Node target = transition.getTarget();
        if (target != null) {
            target.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, transition);
        }
    }

    public List<Transition> getLeavingTransitions() {
        return getChildren(Transition.class);
    }

    public List<Transition> getArrivingTransitions() {
        // TODO add to field
        List<Transition> arrivingTransitions = new ArrayList<Transition>();
        List<Node> allNodes = getProcessDefinition().getNodes();
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

    @Override
    public boolean canSetNameTo(String name) {
        ProcessDefinition definition = getProcessDefinition();
        if (definition == null) {
            return false;
        }
        Node node = definition.getNodeByName(name);
        if (node != null) {
            return false;
        }
        return true;
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
        /* allow duplicated transitions to attach different action handlers
        for (Transition transition : transitions) {
            if (transition.getTarget().equals(target)) {
                return false;
            }
        }
        */
        return true;
    }

    public boolean isExclusive() {
    	return false;
    }
}
