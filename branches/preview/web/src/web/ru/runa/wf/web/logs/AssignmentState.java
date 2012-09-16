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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ecs.Element;
import org.apache.ecs.StringElement;

import ru.runa.af.Group;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.common.web.Messages;

public class AssignmentState extends BaseState {
    public AssignmentState(BaseState parent) {
        super(parent);
    }

    @Override
    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        TaskAssignLog invLog = (TaskAssignLog) currentLog;
        if (invLog.getTaskOldActorId() != null && invLog.getTaskOldActorId().startsWith(Group.TEMPORARY_GROUP_PREFIX)) {
            Long groupId = Long.parseLong(invLog.getTaskOldActorId().substring(Group.TEMPORARY_GROUP_PREFIX.length()));
            Set<Long> ids = new HashSet<Long>();
            for (String id : invLog.getTaskNewActorId().split(";")) {
                ids.add(Long.parseLong(id));
            }
            addDynamicGroup(groupId, ids);

            List<Element> result = new ArrayList<Element>();
            StringBuilder message = new StringBuilder();
            message.append(Messages.getMessage(Messages.HISTORY_DYNAMIC_GROUP_CREATED, pageContext).replaceAll("\\{" + getPH_EXECUTOR_NAME() + "\\}",
                    Messages.getMessage(Messages.DYNAMIC_GROUP_NAME, pageContext) + groupId));
            for (Long id : ids) {
                message.append(getExecutorLink(subject, id)).append(", ");
            }
            message.delete(message.length() - 2, message.length());
            result.add(new StringElement(message.toString()));
            return result;
        }

        return new ArrayList<Element>();
    }
}
