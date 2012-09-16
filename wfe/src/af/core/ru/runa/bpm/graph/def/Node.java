/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.graph.def;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.NodeLog;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;

public class Node extends GraphElement implements Parsable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Node.class);

    public enum NodeType {
        Node, StartState, EndState, State, Task, Fork, Join, Decision, SubProcess, MultiInstance, SendMessage, ReceiveMessage
    };

    protected List<Transition> leavingTransitions = null;
    private transient Map<String, Transition> leavingTransitionMap = null;
    protected Set<Transition> arrivingTransitions = null;
    protected Action action = null;

    public NodeType getNodeType() {
        return NodeType.Node;
    }

    public String getNameExt() {
        String name = super.getName();
        if (name == null) {
            name = "#anonymous" + getNodeType();
        }

        return name;
    }

    // event types
    // //////////////////////////////////////////////////////////////

    public static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE,
            Event.EVENTTYPE_BEFORE_SIGNAL, Event.EVENTTYPE_AFTER_SIGNAL };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element nodeElement, JpdlXmlReader jpdlXmlReader) {
        action = jpdlXmlReader.readSingleAction(processDefinition, nodeElement);
    }

    public List<Transition> getLeavingTransitions() {
        return leavingTransitions;
    }

    public List<Transition> getLeavingTransitionsList() {
        return leavingTransitions;
    }

    /**
     * are the leaving {@link Transition}s, mapped by their name
     * (java.lang.String).
     */
    public Map<String, Transition> getLeavingTransitionsMap() {
        if (leavingTransitionMap == null && leavingTransitions != null) {
            // initialize the cached leaving transition map
            leavingTransitionMap = new HashMap<String, Transition>();
            ListIterator<Transition> iter = leavingTransitions.listIterator(leavingTransitions.size());
            while (iter.hasPrevious()) {
                Transition leavingTransition = iter.previous();
                leavingTransitionMap.put(leavingTransition.getName(), leavingTransition);
            }
        }
        return leavingTransitionMap;
    }

    /**
     * creates a bidirection relation between this node and the given leaving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if leavingTransition is null.
     */
    public Transition addLeavingTransition(Transition leavingTransition) {
        if (leavingTransition == null) {
            throw new IllegalArgumentException("can't add a null leaving transition to an node");
        }
        if (leavingTransitions == null) {
            leavingTransitions = new ArrayList<Transition>();
        }
        leavingTransitions.add(leavingTransition);
        leavingTransition.setFrom(this);
        leavingTransitionMap = null;
        return leavingTransition;
    }

    /**
     * removes the bidirection relation between this node and the given leaving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if leavingTransition is null.
     */
    public void removeLeavingTransition(Transition leavingTransition) {
        if (leavingTransition == null) {
            throw new IllegalArgumentException("can't remove a null leavingTransition from an node");
        }
        if (leavingTransitions != null) {
            if (leavingTransitions.remove(leavingTransition)) {
                leavingTransition.setFrom(null);
                leavingTransitionMap = null;
            }
        }
    }

    /**
     * checks for the presence of a leaving transition with the given name.
     * 
     * @return true if this node has a leaving transition with the given name,
     *         false otherwise.
     */
    public boolean hasLeavingTransition(String transitionName) {
        if (leavingTransitions == null) {
            return false;
        }
        return getLeavingTransitionsMap().containsKey(transitionName);
    }

    /**
     * retrieves a leaving transition by name. note that also the leaving
     * transitions of the supernode are taken into account.
     */
    public Transition getLeavingTransition(String transitionName) {
        Transition transition = null;
        if (leavingTransitions != null) {
            transition = getLeavingTransitionsMap().get(transitionName);
        }
        return transition;
    }

    /**
     * true if this transition has leaving transitions.
     */
    public boolean hasNoLeavingTransitions() {
        return leavingTransitions == null || leavingTransitions.size() == 0;
    }

    /**
     * generates a new name for a transition that will be added as a leaving
     * transition.
     */
    public String generateNextLeavingTransitionName() {
        String name = null;
        if (leavingTransitions != null && containsName(leavingTransitions, null)) {
            int n = 1;
            while (containsName(leavingTransitions, Integer.toString(n))) {
                n++;
            }
            name = Integer.toString(n);
        }
        return name;
    }

    boolean containsName(List<Transition> leavingTransitions, String name) {
        for (Transition transition : leavingTransitions) {
            if (name == null && transition.getName() == null) {
                return true;
            } else if (name != null && name.equals(transition.getName())) {
                return true;
            }
        }
        return false;
    }

    // default leaving transition and leaving transition ordering
    // ///////////////

    /**
     * is the default leaving transition.
     */
    public Transition getDefaultLeavingTransition() {
        for (Transition auxTransition : leavingTransitions) {
            return auxTransition;
        }
        return null;
    }

    /**
     * moves one leaving transition from the oldIndex and inserts it at the
     * newIndex.
     */
    public void reorderLeavingTransition(int oldIndex, int newIndex) {
        if ((leavingTransitions != null) && (Math.min(oldIndex, newIndex) >= 0) && (Math.max(oldIndex, newIndex) < leavingTransitions.size())) {
            Transition o = leavingTransitions.remove(oldIndex);
            leavingTransitions.add(newIndex, o);
        }
    }

    // arriving transitions
    // /////////////////////////////////////////////////////

    /**
     * are the arriving transitions.
     */
    public Set<Transition> getArrivingTransitions() {
        return arrivingTransitions;
    }

    /**
     * add a bidirection relation between this node and the given arriving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if t is null.
     */
    public Transition addArrivingTransition(Transition arrivingTransition) {
        if (arrivingTransition == null) {
            throw new IllegalArgumentException("can't add a null arrivingTransition to a node");
        }
        if (arrivingTransitions == null) {
            arrivingTransitions = new HashSet<Transition>();
        }
        arrivingTransitions.add(arrivingTransition);
        arrivingTransition.setTo(this);
        return arrivingTransition;
    }

    /**
     * removes the bidirection relation between this node and the given arriving
     * transition.
     * 
     * @throws IllegalArgumentException
     *             if t is null.
     */
    public void removeArrivingTransition(Transition arrivingTransition) {
        if (arrivingTransition == null) {
            throw new IllegalArgumentException("can't remove a null arrivingTransition from a node");
        }
        if (arrivingTransitions != null) {
            if (arrivingTransitions.remove(arrivingTransition)) {
                arrivingTransition.setTo(null);
            }
        }
    }

    // various
    // //////////////////////////////////////////////////////////////////

    /**
     * is the {@link SuperState} or the {@link ExecutableProcessDefinition} in which this
     * node is contained.
     */
    @Override
    public GraphElement getParent() {
        return processDefinition;
    }

    // behaviour methods
    // ////////////////////////////////////////////////////////

    /**
     * called by a transition to pass execution to this node.
     */
    public void enter(ExecutionContext executionContext) {
        Token token = executionContext.getToken();

        // update the runtime context information
        token.setNode(this);

        // fire the leave-node event for this node
        fireEvent(Event.EVENTTYPE_NODE_ENTER, executionContext);

        // keep track of node entrance in the token, so that a node-log can be
        // generated at node leave time.
        token.setNodeEnterDate(new Date());

        // remove the transition references from the runtime context
        executionContext.setTransitionSource(null);

        execute(executionContext);
    }

    /**
     * override this method to customize the node behaviour.
     */
    public void execute(ExecutionContext executionContext) {
        Transition leavingTransition = null;
        // if there is a custom action associated with this node
        if (action != null) {
            try {
                // execute the action
                executeAction(action, executionContext);
                Object handler = action.getDelegation().getInstance();
                if (handler instanceof TransitionAware) {
                    String transitionName = ((TransitionAware) handler).getTransitionName();
                    leavingTransition = getLeavingTransition(transitionName);
                    if (leavingTransition == null) {
                        log.warn("ActionNode[TransitionAware] returns unknown transition: " + transitionName);
                    }
                }
            } catch (Exception exception) {
                // NOTE that Error's are not caught because that might halt the
                // JVM and mask the original Error.
                // search for an exception handler or throw to the client
                raiseException(exception, executionContext);
            }

        }

        if (leavingTransition == null) {
            leavingTransition = getDefaultLeavingTransition();
        }
        // let this node handle the token
        // the default behaviour is to leave the node over the default
        // transition.
        leave(executionContext, leavingTransition);
    }

    /**
     * called by the implementation of this node to continue execution over the
     * default transition.
     */
    public void leave(ExecutionContext executionContext) {
        leave(executionContext, getDefaultLeavingTransition());
    }

    /**
     * called by the implementation of this node to continue execution over the
     * specified transition.
     */
    public void leave(ExecutionContext executionContext, String transitionName) {
        Transition transition = getLeavingTransition(transitionName);
        if (transition == null) {
            throw new InternalApplicationException("transition '" + transitionName + "' is not a leaving transition of node '" + this + "'");
        }
        leave(executionContext, transition);
    }

    /**
     * called by the implementation of this node to continue execution over the
     * given transition.
     */
    public void leave(ExecutionContext executionContext, Transition transition) {
        if (transition == null) {
            throw new InternalApplicationException("can't leave node '" + this + "' without leaving transition");
        }
        Token token = executionContext.getToken();
        token.setNode(this);

        // fire the leave-node event for this node
        fireEvent(Event.EVENTTYPE_NODE_LEAVE, executionContext);

        // log this node
        if (token.getNodeEnterDate() != null) {
            addNodeLog(token);
        }

        // update the runtime information for taking the transition
        // the transitionSource is used to calculate events on superstates
        executionContext.setTransitionSource(this);

        // take the transition
        transition.take(executionContext);
    }

    protected void addNodeLog(Token token) {
        token.addLog(new NodeLog(this, token.getNodeEnterDate(), new Date()));
    }

    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public ExecutableProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    // change the name of a node
    // ////////////////////////////////////////////////
    /**
     * updates the name of this node
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * the slash separated name that includes all the superstate names.
     */
    public String getFullyQualifiedName() {
        String fullyQualifiedName = name;
        // if (superState != null) {
        // fullyQualifiedName = superState.getFullyQualifiedName() + "/" + name;
        // }
        return fullyQualifiedName;
    }

    /** indicates wether this node is a superstate. */
    public boolean isSuperStateNode() {
        return false;
    }

    /** returns a list of child nodes (only applicable for {@link SuperState})s. */
    public List<Node> getNodes() {
        return null;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
