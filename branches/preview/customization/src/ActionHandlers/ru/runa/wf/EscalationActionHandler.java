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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.logic.CommonLogic;
import ru.runa.af.organizationfunction.FunctionParserException;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.af.organizationfunction.OrganizationFunctionException;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.node.TaskNode;
import ru.runa.bpm.taskmgmt.def.Task;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.exe.TaskMgmtInstance;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.commons.system.CommonResources;
import ru.runa.wf.dao.TmpDAO;

public class EscalationActionHandler implements ActionHandler {
    private static final String ESCALATION_ENABLED = "escalation.enabled";
    private static final String DEFAULT_ORG_FUNCTION = "default.escalation.orgFunction";
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(EscalationActionHandler.class);
    private static boolean escalationEnabled = false;
    private String orgFunctionClassName = null;
    static {
        try {
            String escalationEnabledStr = new CommonResources().readPropertyIfExist(ESCALATION_ENABLED);
            if (escalationEnabledStr != null) {
                escalationEnabled = Boolean.parseBoolean(escalationEnabledStr.trim());
            }
            log.info(ESCALATION_ENABLED + " = " + escalationEnabled);
        } catch (Exception e) {
            log.error("Unable to configure escalation", e);
        }
    }
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private TmpDAO tmpDAO;
    @Autowired
    private CommonLogic commonLogic;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        orgFunctionClassName = configuration.trim();
    }

    private Set<Executor> getExecutorsByChiefFunction(Map<String, Object> variables, Long executorCode) throws OrganizationFunctionException,
            FunctionParserException, ExecutorOutOfDateException {
        Set<Executor> executors = new HashSet<Executor>();
        List<Long> executorIds = OrgFunctionHelper.evaluateOrgFunction(variables, orgFunctionClassName + "(" + executorCode + ")", null);
        if (executorIds.size() == 0) {
            log.debug("No assignment will be done (OrgFunction (" + orgFunctionClassName + ") return empty array of executor ids)");
        } else {
            for (Long id : executorIds) {
                Executor executor = executorDAO.getExecutor(id);
                executors.add(executor);
            }
        }
        return executors;
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        if (!escalationEnabled) {
            log.info("Escalation disabled");
            return;
        }
        if (orgFunctionClassName == null) {
            orgFunctionClassName = new CommonResources().readPropertyIfExist(DEFAULT_ORG_FUNCTION);
            if (orgFunctionClassName != null) {
                orgFunctionClassName = orgFunctionClassName.trim();
            }
        }
        Node node = executionContext.getToken().getNode();
        node = (Node) ru.runa.commons.JBPMLazyLoaderHelper.getImplementation(node); // TODO
        if (node.getNodeType() == Node.NodeType.Task) {
            TaskNode taskNode = (TaskNode) node;
            Task task = taskNode.getTasks().iterator().next();
            Map<String, Object> variables = executionContext.getContextInstance().getVariables();
            String swimlaneValue = (String) variables.get(task.getSwimlane().getName());
            if (swimlaneValue == null || orgFunctionClassName == null) {
                log.warn(swimlaneValue == null ? "swimlaneValue=null" : "orgFunction=null");
                return;
            }
            log.info("Escalation for " + swimlaneValue);
            Long executorID = 0L;
            long escalationLevel = 0;
            Set<Executor> swimlaneExecutors = new HashSet<Executor>();
            if (swimlaneValue.startsWith("G")) {
                Group swimlaneGroup = executorDAO.getGroup(Long.valueOf(swimlaneValue.substring(1, swimlaneValue.length())));
                if (swimlaneGroup.isTemporary() && swimlaneGroup.getDescription().startsWith(Group.ESCALATION_GROUP_PREFIX)) {
                    String info = swimlaneGroup.getDescription().substring(Group.ESCALATION_GROUP_PREFIX.length()).trim();
                    String[] s = info.split(" ");
                    if (s.length > 1) {
                        executorID = new Long(s[0]);
                        escalationLevel = Long.parseLong(s[1]);
                    }
                }
                swimlaneExecutors.addAll(executorDAO.getGroupActors(swimlaneGroup));
            } else {
                Executor executor = executorDAO.getActorByCode(new Long(swimlaneValue));
                executorID = executor.getId();
                swimlaneExecutors.add(executor);
            }
            Set<Executor> executors = new HashSet<Executor>();
            executors.addAll(swimlaneExecutors);
            for (Executor executor : swimlaneExecutors) {
                executors.addAll(getExecutorsByChiefFunction(variables, ((Actor) executor).getCode()));
            }
            Group tokenGroup = Group.createTemporaryGroup(executionContext.getProcessInstance().getId() + "_" + task.getSwimlane().getName(),
                    Group.ESCALATION_GROUP_PREFIX + " " + executorID + " " + (escalationLevel + 1));
            log.info("executorID = " + executorID + ";  level = " + (escalationLevel + 1));
            commonLogic.getTemporaryGroup(tokenGroup, executors);
            executionContext.getToken().addLog(
                    new TaskAssignLog(executionContext.getTaskInstance(), Group.TEMPORARY_GROUP_PREFIX + tokenGroup.getId(), commonLogic
                            .encodeExecutors(executors)));
            TaskMgmtInstance taskMgmtInstance = executionContext.getProcessInstance().getTaskMgmtInstance();
            for (TaskInstance taskInstance : taskMgmtInstance.getTaskInstances()) {
                if (task.equals(taskInstance.getTask())) {
                    String actorId = "G" + String.valueOf(tokenGroup.getId());
                    log.debug("Assigning " + task.getName() + " to " + actorId);
                    taskInstance.setActorId(executionContext, actorId);
                }
            }
            tmpDAO.saveProcessInstance(executionContext.getToken().getProcessInstance());
        } else {
            log.warn("Incorrect Node for escalation");
        }
    }
}
