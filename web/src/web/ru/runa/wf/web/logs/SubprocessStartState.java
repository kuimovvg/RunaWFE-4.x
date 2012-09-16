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
import org.apache.ecs.html.A;
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
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.bpm.graph.log.SignalLog;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.bpm.taskmgmt.log.TaskCreateLog;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.ShowGraphModeHelper;

class SubprocessStartState extends BaseState {
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
        stateTransitions.put(TaskAssignLog.class, SilentState.class);
        stateTransitions.put(TransitionLog.class, TransitionState.class);
        stateTransitions.put(ActionLog.class, ActionState.class);
        stateTransitions.put(NodeLog.class, SilentState.class);
    }

    public SubprocessStartState(BaseState parent) {
        super(parent);
    }

    @Override
    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        ProcessStateLog invLog = (ProcessStateLog) currentLog;
        List<Element> result = new ArrayList<Element>();
        result.add(new TR(new StringElement(invLog.getEnter()
                + ": "
                + Messages.getMessage(Messages.HISTORY_SUBPROCESS_START, pageContext).replace(
                        "{0}",
                        new A(Commons.getActionUrl(ShowGraphModeHelper.getManageProcessInstanceAction(), IdForm.ID_INPUT_NAME, invLog
                                .getSubProcessInstance().getId(), pageContext, PortletUrl.Render), Long.toString(invLog.getSubProcessInstance()
                                .getId())).setClass(Resources.CLASS_LINK).toString()))));

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

        result.add(new TR(new StringElement(invLog.getDate()
                + ": "
                + Messages.getMessage(Messages.HISTORY_SUBPROCESS_COMPLETE, pageContext).replace(
                        "{0}",
                        new A(Commons.getActionUrl(ShowGraphModeHelper.getManageProcessInstanceAction(), IdForm.ID_INPUT_NAME, invLog
                                .getSubProcessInstance().getId(), pageContext, PortletUrl.Render), Long.toString(invLog.getSubProcessInstance()
                                .getId())).setClass(Resources.CLASS_LINK).toString()))));

        Map<Class<? extends ProcessLog>, Class<? extends BaseState>> stateTransitions = new HashMap<Class<? extends ProcessLog>, Class<? extends BaseState>>(
                SubprocessStartState.stateTransitions);
        stateTransitions.put(SignalLog.class, SilentState.class);
        isLogAccepted = false;
        ul = new UL();
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
        stateTransitions.remove(SignalLog.class);
        return result;
    }
}
