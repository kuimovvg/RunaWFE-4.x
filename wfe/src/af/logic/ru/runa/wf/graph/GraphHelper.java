/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.graph;

import java.util.Collection;
import java.util.List;

import org.hibernate.proxy.HibernateProxy;

import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Node.NodeType;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.node.InteractionNode;
import ru.runa.bpm.graph.node.TaskNode;
import ru.runa.bpm.scheduler.def.CancelTimerAction;
import ru.runa.bpm.scheduler.def.CreateTimerAction;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.wf.graph.model.NodeModel;
import ru.runa.wf.graph.util.DrawProperties;

public class GraphHelper {
    public static void setTypeToNode(Node node, NodeModel nodeModel) {

        if (node.getNodeType() == NodeType.Decision) {
            nodeModel.setType(NodeModel.DECISION);
        } else if ((node.getNodeType() == NodeType.Fork) || (node.getNodeType() == NodeType.Join)) {
            nodeModel.setType(NodeModel.FORK_JOIN);
        } else if (node.getNodeType() == NodeType.EndState) {
            nodeModel.setType(NodeModel.END_STATE);
        } else if (node.getNodeType() == NodeType.StartState) {
            nodeModel.setType(NodeModel.START_STATE);
        } else if (node.getNodeType() == NodeType.SubProcess) {
            nodeModel.setType(NodeModel.PROCESS_STATE);
        } else if (node.getNodeType() == NodeType.MultiInstance) {
            nodeModel.setType(NodeModel.MULTI_INSTANCE);
        } else if (node.getNodeType() == NodeType.Node) {
            nodeModel.setType(NodeModel.ACTION_NODE);
        } else {
            CreateTimerAction createTimerAction = getTimerActionIfExists(node);
            boolean hasTimer = (createTimerAction != null && !"__GLOBAL".equals(createTimerAction.getTimerName()) && !"__LOCAL"
                    .equals(createTimerAction.getTimerName()));
            boolean hasTimeOutTransition = false;
            Collection<Transition> transitions = node.getLeavingTransitions();

            for (Transition tr : transitions) {

                if (DrawProperties.TIMEOUT_TRANSITION.equals(tr.getName())) {
                    hasTimeOutTransition = true;
                }
            }

            if (!hasTimer) {
                nodeModel.setType(NodeModel.STATE);
            } else if (hasTimeOutTransition && (transitions.size() == 1)) {
                nodeModel.setType(NodeModel.WAIT_STATE);
            } else {
                nodeModel.setType(NodeModel.STATE_WITH_TIMER);
            }
        }
    }

    public static void setSwimlaneToNode(Node node, NodeModel nodeModel) {
        Task task = null;
        if (node instanceof HibernateProxy) {
            node = (Node) ((HibernateProxy) node).getHibernateLazyInitializer().getImplementation();
        }
        if (node instanceof InteractionNode) {
            task = ((InteractionNode) node).getFirstTaskNotNull();
        }
        if (task != null && task.getSwimlane() != null) {
            nodeModel.setSwimlane(task.getSwimlane().getName());
        }
    }

    public static CreateTimerAction getTimerActionIfExists(Node node) {
        Event nodeEnterEvent = node.getEvent(Event.EVENTTYPE_NODE_ENTER);

        if (nodeEnterEvent != null) {
            List<Action> actions = nodeEnterEvent.getActions();

            if (actions.size() > 0) {
                Action action = actions.get(0);

                if (action instanceof HibernateProxy) {
                    action = (Action) ((HibernateProxy) action).getHibernateLazyInitializer().getImplementation();
                }

                if (action instanceof CreateTimerAction) {
                    return (CreateTimerAction) action;
                }
            }
        }

        return null;
    }

    public static int processActionsInEvent(Event nodeEnterEvent) {
        int result = 0;

        if (nodeEnterEvent != null) {
            List<Action> actions = nodeEnterEvent.getActions();

            for (Action action : actions) {

                if (action instanceof HibernateProxy) {
                    action = (Action) ((HibernateProxy) action).getHibernateLazyInitializer().getImplementation();
                }

                if ((action instanceof CreateTimerAction) || (action instanceof CancelTimerAction)) {
                    continue;
                }

                result++;
            }
        }

        return result;
    }

    public static int getNodeActionsCount(Node node) {
        int result = 0;
        result += processActionsInEvent(node.getEvent(Event.EVENTTYPE_NODE_ENTER));
        result += processActionsInEvent(node.getEvent(Event.EVENTTYPE_NODE_LEAVE));

        if (node instanceof TaskNode) {
            for (Task task : ((TaskNode) node).getTasks()) {
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_CREATE));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_ASSIGN));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_START));
                result += processActionsInEvent(task.getEvent(Event.EVENTTYPE_TASK_END));
            }
        }

        return result;
    }

    public static int getTransitionActionsCount(Transition tr) {
        Event event = tr.getEvent(Event.EVENTTYPE_TRANSITION);

        if (event != null) {
            return event.getActions().size();
        }

        return 0;
    }
}
