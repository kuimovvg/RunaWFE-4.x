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
import org.apache.ecs.html.TR;

import ru.runa.bpm.graph.log.ProcessInstanceEndLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.common.web.Messages;

class ProcessCompleteState extends BaseState {
    public ProcessCompleteState(BaseState parent) {
        super(parent);
    }

    @Override
    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        ProcessInstanceEndLog invLog = (ProcessInstanceEndLog) currentLog;
        List<Element> retVal = new ArrayList<Element>();
        if (invLog.getParent() == null) {
            String lastAssignUser = null;
            int offset = 0;
            // search forward
            while (!(logs.current() instanceof TaskAssignLog) && logs.hasNext()) {
                logs.next();
                ++offset;
            }
            if (logs.current() instanceof TaskAssignLog) {
                lastAssignUser = ((TaskAssignLog) logs.current()).getTaskNewActorId();
            }
            while (offset != 0) {
                offset--;
                logs.prev();
            }
            if (lastAssignUser == null) {
                // search backward
                do {
                    ++offset;
                    logs.prev();
                } while (!(logs.current() instanceof TaskAssignLog));
                lastAssignUser = ((TaskAssignLog) logs.current()).getTaskNewActorId();
                while (offset != 0) {
                    offset--;
                    logs.next();
                }
            }

            retVal.add(new TR(new StringElement(invLog.getDate() + ": "
                    + replacePlaceholders(Messages.HISTORY_CANCEL_PROCESS, null, lastAssignUser, null, null, null))));
        } else {
            retVal.add(new TR(new StringElement(invLog.getDate() + ": "
                    + replacePlaceholders(Messages.HISTORY_END_PROCESS, null, null, null, null, null))));
        }
        while (logs.hasNext()) {
            logs.next();
        }
        return retVal;
    }
}
