package ru.runa.wfe.graph.image;

import java.util.List;

import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.job.CancelTimerAction;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.job.Timer;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.lang.Transition;

public class GraphImageHelper {
	public static int processActionsInEvent(Event event) {
        int result = 0;
        for (Action action : event.getActions()) {
            if (action instanceof CreateTimerAction || action instanceof CancelTimerAction || Timer.ESCALATION_NAME.equals(action.getName())) {
                continue;
            }
            result++;
        }
        return result;
    }

	public static int getNodeActionsCount(GraphElement node) {
        int result = 0;
        for (Event event : node.getEvents().values()) {
            result += processActionsInEvent(event);
        }
        if (node instanceof TaskNode) {
            for (TaskDefinition taskDefinition : ((TaskNode) node).getTasks()) {
                result += getNodeActionsCount(taskDefinition);
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

    public static void initNodeModel(Node node, NodeModel nodeModel) {
        nodeModel.setType(node.getNodeType());
        // nodeModel contains only id
        nodeModel.setName(node.getName());
        boolean hasTimer = false;
        for (CreateTimerAction createTimerAction : node.getTimerActions()) {
            if (!Timer.ESCALATION_NAME.equals(createTimerAction.getName())) {
                hasTimer = true;
                break;
            }
        }
        nodeModel.setWithTimer(hasTimer);
        if (node instanceof Synchronizable) {
            nodeModel.setAsync(((Synchronizable) node).isAsync());
        }
        TaskDefinition taskDefinition = null;
        if (node instanceof InteractionNode) {
            taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        }
        if (taskDefinition != null && taskDefinition.getSwimlane() != null) {
            nodeModel.setSwimlane(taskDefinition.getSwimlane().getName());
        }
    }

    public static String getTimerInfo(Node node) {
        try {
            List<CreateTimerAction> actions = node.getTimerActions();
            if (actions.size() == 0) {
                return "No timer";
            }
            return actions.get(0).getDueDate();
        } catch (Exception e) {
            return e.getClass().getName();
        }
    }
}
