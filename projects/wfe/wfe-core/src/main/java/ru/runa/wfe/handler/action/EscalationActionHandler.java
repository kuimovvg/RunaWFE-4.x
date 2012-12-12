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
package ru.runa.wfe.handler.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.audit.TaskEscalationLog;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.user.logic.ExecutorLogic;

public class EscalationActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(EscalationActionHandler.class);
    @Autowired
    @Value(value = "${escalation.enabled}")
    private boolean escalationEnabled;
    @Autowired
    @Value(value = "${escalation.default.orgFunction}")
    private String defaultOrgFunctionClassName;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private ExecutorLogic executorLogic;
    private String orgFunctionClassName;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        orgFunctionClassName = configuration;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        if (!escalationEnabled) {
            log.info("Escalation disabled");
            return;
        }
        if (orgFunctionClassName == null || orgFunctionClassName.length() == 0) {
            orgFunctionClassName = defaultOrgFunctionClassName;
        }
        orgFunctionClassName = orgFunctionClassName.trim();
        try {
            ClassLoaderUtil.instantiate(orgFunctionClassName);
        } catch (Throwable e) {
            log.error("Unknown orgFunction: " + orgFunctionClassName);
            return;
        }
        if (executionContext.getNode() instanceof TaskNode) {
            Task task = executionContext.getTask();
            Executor swimlaneExecutor = task.getSwimlane() != null ? task.getSwimlane().getExecutor() : null;
            if (swimlaneExecutor == null) {
                log.warn("Task swimlane '" + task + "' is not assigned");
                return;
            }
            log.info("Escalation for '" + task + "' with current swimlane value '" + swimlaneExecutor + "'");
            Executor originalExecutor;
            int previousEscalationLevel = 0;
            Set<Actor> previousSwimlaneActors = new HashSet<Actor>();

            if (swimlaneExecutor instanceof Group) {
                Group swimlaneGroup = (Group) swimlaneExecutor;
                if (swimlaneGroup instanceof EscalationGroup) {
                    EscalationGroup escalationGroup = (EscalationGroup) swimlaneGroup;
                    originalExecutor = escalationGroup.getOriginalExecutor();
                    previousEscalationLevel = escalationGroup.getEscalationLevel();
                } else {
                    originalExecutor = swimlaneGroup;
                }
                for (Executor executor : executorDAO.getGroupChildren(swimlaneGroup)) {
                    if (executor instanceof Actor) {
                        previousSwimlaneActors.add((Actor) executor);
                    } else {
                        log.warn("Unexpected: group in TmpGroup: " + executor);
                    }
                }
            } else {
                Actor swimlaneActor = (Actor) swimlaneExecutor;
                originalExecutor = swimlaneActor;
                previousSwimlaneActors.add(swimlaneActor);
            }

            Set<Executor> assignedExecutors = new HashSet<Executor>();
            assignedExecutors.addAll(previousSwimlaneActors);
            for (Actor previousActor : previousSwimlaneActors) {
                String swimlaneInitializer = orgFunctionClassName + "(" + previousActor.getCode() + ")";
                List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(swimlaneInitializer, null);
                if (executors.size() == 0) {
                    log.debug("No escalation will be done for member: " + swimlaneInitializer);
                } else {
                    for (Executor functionExecutor : executors) {
                        assignedExecutors.add(functionExecutor);
                    }
                }
            }
            if (assignedExecutors.size() == previousSwimlaneActors.size()) {
                log.debug("Escalation ignored. No new members found for " + previousSwimlaneActors);
                return;
            }
            int escalationLevel = previousEscalationLevel + 1;
            Process process = executionContext.getProcess();
            Group escalationGroup = EscalationGroup.create(process, task, originalExecutor, escalationLevel);
            escalationGroup = executorLogic.saveTemporaryGroup(escalationGroup, assignedExecutors);
            executionContext.addLog(new TaskEscalationLog(task, assignedExecutors));
            task.assignExecutor(executionContext, escalationGroup, false);
        } else {
            log.warn("Incorrect NodeType for escalation: " + executionContext.getNode());
        }
    }
}
