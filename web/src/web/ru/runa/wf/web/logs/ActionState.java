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
import java.util.List;

import org.apache.ecs.Element;
import org.apache.ecs.StringElement;

import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.log.ActionLog;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.log.TaskLog;
import ru.runa.common.web.Messages;

class ActionState extends BaseState {
    public ActionState(BaseState parent) {
        super(parent);
    }

    @Override
    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        ActionLog actionLog = (ActionLog) currentLog;
        List<Element> retVal = new ArrayList<Element>();

        Transition transition = null;
        TaskInstance taskInstance = null;
        int offset = 0;
        while (logs.hasNext()) {
            ++offset;
            ProcessLog next = logs.next();
            if (next instanceof TransitionLog) {
                transition = ((TransitionLog) next).getTransition();
                break;
            }
            if (next instanceof TaskLog) {
                taskInstance = ((TaskLog) next).getTaskInstance();
                break;
            }
        }
        while (offset != 0) {
            --offset;
            logs.prev();
        }
        retVal.add(new StringElement(replacePlaceholders(Messages.HISTORY_FIRE_ACTION, taskInstance, null, actionLog.getAction(), transition, null)));
        return retVal;
    }
}
