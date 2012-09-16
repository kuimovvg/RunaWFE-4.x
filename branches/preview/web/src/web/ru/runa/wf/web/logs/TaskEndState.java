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
package ru.runa.wf.web.logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ecs.Element;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.LI;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.UL;

import ru.runa.bpm.context.log.VariableCreateLog;
import ru.runa.bpm.context.log.VariableUpdateLog;
import ru.runa.bpm.context.log.variableinstance.ByteArrayUpdateLog;
import ru.runa.bpm.context.log.variableinstance.DateUpdateLog;
import ru.runa.bpm.context.log.variableinstance.DoubleUpdateLog;
import ru.runa.bpm.context.log.variableinstance.LongUpdateLog;
import ru.runa.bpm.context.log.variableinstance.StringUpdateLog;
import ru.runa.bpm.graph.log.ActionLog;
import ru.runa.bpm.graph.log.NodeLog;
import ru.runa.bpm.graph.log.SignalLog;
import ru.runa.bpm.graph.log.TokenCreateLog;
import ru.runa.bpm.graph.log.TokenEndLog;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.bpm.taskmgmt.log.TaskCreateLog;
import ru.runa.bpm.taskmgmt.log.TaskEndLog;
import ru.runa.common.web.Messages;

class TaskEndState extends BaseState {
    private static Map<Class<? extends ProcessLog>, Class<? extends BaseState>> stateTransitions = new HashMap<Class<? extends ProcessLog>, Class<? extends BaseState>>();

    static {
        stateTransitions.put(VariableUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(ByteArrayUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(DateUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(DoubleUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(LongUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(StringUpdateLog.class, VariableUpdateState.class);
        stateTransitions.put(VariableCreateLog.class, SilentState.class);
        stateTransitions.put(TaskCreateLog.class, NodeEnterState.class);
        stateTransitions.put(TaskAssignLog.class, AssignmentState.class);
        stateTransitions.put(SignalLog.class, SilentState.class);
        stateTransitions.put(TransitionLog.class, TransitionState.class);
        stateTransitions.put(ActionLog.class, ActionState.class);
        stateTransitions.put(NodeLog.class, SilentState.class);
        stateTransitions.put(TokenCreateLog.class, SilentState.class);
        stateTransitions.put(TokenEndLog.class, SilentState.class);
    }

    public TaskEndState(BaseState parent) {
        super(parent);
    }

    @Override
    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        TaskEndLog invLog = (TaskEndLog) currentLog;
        List<Element> result = new ArrayList<Element>();
        //end-state
        if (invLog.getTaskInstance().getTask().getNode() == null) {
            return result;
        }

        String actor = invLog.getTaskInstance().getAssignedActorId();
        if (invLog.getParent() != null && invLog.getParent() instanceof SignalLog
                && ((SignalLog) invLog.getParent()).getTransition().getName().equals("time-out-transition")) {
            result.add(new TR(new StringElement(invLog.getDate() + ": "
                    + replacePlaceholders(Messages.HISTORY_TASK_DONE_BY_TIMER, invLog.getTaskInstance(), actor, null, null, null))));
            return result;
        }
        int offset = 1;
        logs.prev();
        while (!(logs.current() instanceof TaskAssignLog)) {
            offset++;
            logs.prev();
        }
        TaskAssignLog prev = (TaskAssignLog) logs.current();
        while (offset != 0) {
            --offset;
            logs.next();
        }

        String substitutor = null;
        if (prev != null && prev.getToken().equals(invLog.getToken())) {
            if (prev.getTaskOldActorId() != null && prev.getTaskNewActorId() != null && !prev.getTaskOldActorId().equals(prev.getTaskNewActorId())
                    && prev.getTaskNewActorId().equals(actor)) {
                substitutor = prev.getTaskOldActorId();
            }
        }
        result.add(new TR(new StringElement(invLog.getDate() + ": "
                + replacePlaceholders(Messages.HISTORY_TASK_DONE, invLog.getTaskInstance(), actor, null, null, substitutor))));

        boolean isLogAccepted = false;
        UL ul = new UL();
        do {
            if (!logs.hasNext()) {
                break;
            }
            ProcessLog nextLog = logs.next();
            List<Element> dispatchResult = dispatchLog(stateTransitions, nextLog, logs);
            isLogAccepted = dispatchResult != null;
            if (isLogAccepted) {
                for (Element element : dispatchResult) {
                    ul.addElement(new LI(element));
                }
            } else {
                logs.prev();
            }
        } while (isLogAccepted);
        if (ul.elements().hasMoreElements()) {
            result.add(ul);
        }

        return result;
    }
}
