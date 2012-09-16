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

package ru.runa.wf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

import com.google.common.base.Objects;

/**
 * Class for creating instances of {@link TaskStub}.
 */
public class TaskStubFactory {
    private static final Log log = LogFactory.getLog(TaskStubFactory.class);
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private SecuredObjectDAO securedObjectDAO;

    public TaskStub create(TaskInstance taskInstance, Actor targetActor, String formType) {
        ProcessInstance process = taskInstance.getProcessInstance();
        ArchievedProcessDefinition definition = process.getProcessDefinition();
        Long id = taskInstance.getId();
        String name = taskInstance.getName();
        String description = null;
        if (taskInstance.getDescription() != null) {
            description = taskInstance.getDescription();
        } else {
            Task task = taskInstance.getTask();
            if (task != null) {
                description = task.getNode().getDescription();
            } else {
                description = null;
            }
        }
        Long processInstanceId = process.getId();
        Long processDefinitionId = definition.getId();
        String processDefinitionName = definition.getName();
        int processDefinitionVersion = definition.getVersion().intValue();
        SwimlaneStub swimlaneStub = new SwimlaneStub(taskInstance, null);
        boolean groupAssigned = taskInstance.getAssignedActorId() != null && taskInstance.getAssignedActorId().startsWith("G");
        Token superProcessToken = getSuperProcessToken(taskInstance.getProcessInstance(), targetActor);

        Long superProcessInstanceId = null;
        String superProcessDefinitionName = processDefinitionName;
        Long superProcessDefinitionId = processDefinitionId;

        if (superProcessToken != null) {
            superProcessInstanceId = superProcessToken.getProcessInstance().getId();
            superProcessDefinitionName = superProcessToken.getProcessInstance().getProcessDefinition().getName();
            superProcessDefinitionId = superProcessToken.getProcessInstance().getProcessDefinition().getId();
        }

        boolean escalation = false;
        try {
            Long actorID = targetActor.getId();
            Long executorID = null;
            String sID = taskInstance.getAssignedActorId();
            if (sID.startsWith("G")) {
                executorID = Long.valueOf(sID.substring(1));
                // } else {
                // executorID =
                // executorDAO.getActorByCode(Long.valueOf(sID)).getId();
            }
            escalation = !Objects.equal(actorID, executorID);
            if (escalation && executorID != null) {
                Group group = executorDAO.getGroup(executorID);
                if (group.getDescription().startsWith(Group.ESCALATION_GROUP_PREFIX)) {
                    String info = group.getDescription().substring(Group.ESCALATION_GROUP_PREFIX.length()).trim();
                    String[] s = info.split(" ");
                    if (s.length > 1) {
                        escalation = actorID != Long.parseLong(s[0]);
                    }
                } else {
                    escalation = false;
                }
            }
        } catch (Exception e) {
            log.error("Unable to build task " + name, e);
        }

        return new TaskStub(id, name, description, targetActor, processInstanceId, processDefinitionId, processDefinitionName,
                processDefinitionVersion, superProcessInstanceId, superProcessDefinitionName, superProcessDefinitionId, swimlaneStub,
                taskInstance.getCreateDate(), taskInstance.getDueDate(), formType, groupAssigned, escalation);
    }

    private Token getSuperProcessToken(ProcessInstance processInstance, Actor actor) {
        final Token token = processInstance.getSuperProcessToken();
        if (token == null) {
            return null;
        }
        try {
            if (actor == null || isReadAllowed(actor, token)) {
                Token superToken = getSuperProcessToken(token.getProcessInstance(), actor);
                return superToken != null ? superToken : token;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Unable to getSuperProcessToken", e);
            return null;
        }
    }

    private boolean isReadAllowed(Actor actor, Token token) throws ExecutorOutOfDateException, SecuredObjectOutOfDateException {
        ProcessInstance processInstance = token.getProcessInstance();
        return permissionDAO.isAllowed(actor, ProcessInstancePermission.READ, securedObjectDAO.get(new ProcessInstanceStub(processInstance)))
                && permissionDAO.isAllowed(actor, ProcessDefinitionPermission.READ,
                        securedObjectDAO.get(new ProcessDefinition(processInstance.getProcessDefinition())));
    }
}
