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
package ru.runa.wf.swimlane;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.bpm.db.TaskDAO;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

public class AssignSwimlaneActionHandler implements ActionHandler {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AssignSwimlaneActionHandler.class);

    private String swimlaneInititalizer;
    private String swimlaneName;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private TaskDAO taskDAO;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        try {
            Element root = DocumentHelper.parseText(configuration).getRootElement();
            swimlaneName = root.attributeValue("swimlaneName");
            if (swimlaneName == null) {
                swimlaneName = root.elementTextTrim("swimlaneName");
            }
            swimlaneInititalizer = root.attributeValue("swimlaneInititalizer");
            if (swimlaneInititalizer == null) {
                swimlaneInititalizer = root.elementTextTrim("swimlaneInititalizer");
            }
        } catch (DocumentException e) {
            throw new ConfigurationException("Invalid XML for " + getClass(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            List<Long> executorIds = OrgFunctionHelper.evaluateOrgFunction(executionContext.getContextInstance().getVariables(),
                    swimlaneInititalizer, null);
            if (executorIds.size() == 0) {
                log.warn("No assignment will be done (OrgFunction return empty array of executor ids)");
                return;
            }
            if (executorIds.size() > 1) {
                log.warn("OrgFunction return array " + executorIds + ". Assignemt will be done for first item.");
            }
            String assignedTo = null;
            try {
                Actor actor = executorDAO.getActor(executorIds.get(0));
                assignedTo = String.valueOf(actor.getCode());
            } catch (ExecutorOutOfDateException e) {
                Group group = executorDAO.getGroup(executorIds.get(0));
                assignedTo = "G" + group.getId();
            }
            if (assignedTo != null) {
                log.info("Assigning variable " + swimlaneName + " to " + assignedTo);
                executionContext.setVariable(swimlaneName, assignedTo);
            }
            // change actor for already assigned tasks
            List<TaskInstance> taskInstances = taskDAO.findTaskInstancesByProcessInstance(executionContext.getProcessInstance());
            for (TaskInstance taskInstance : taskInstances) {
                if (taskInstance.getSwimlaneInstance() != null) {
                    Swimlane swimlane = taskInstance.getSwimlaneInstance().getSwimlane();
                    if (swimlane != null && swimlaneName.equals(swimlane.getName())) {
                        log.debug("Assigning " + taskInstance.getName() + " to " + assignedTo);
                        taskInstance.setActorId(executionContext, assignedTo);
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
