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
package ru.runa.wfe.handler.action.user;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.handler.action.ActionHandler;
import ru.runa.wfe.handler.assign.AssignmentHelper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.user.Executor;

public class AssignSwimlaneActionHandler implements ActionHandler {
    private static final Log log = LogFactory.getLog(AssignSwimlaneActionHandler.class);

    private String swimlaneInititalizer;
    private String swimlaneName;
    @Autowired
    protected AssignmentHelper assignmentHelper;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        Element root = XmlUtils.parseWithoutValidation(configuration).getRootElement();
        swimlaneName = root.attributeValue("swimlaneName");
        if (swimlaneName == null) {
            swimlaneName = root.elementTextTrim("swimlaneName");
        }
        swimlaneInititalizer = root.attributeValue("swimlaneInititalizer");
        if (swimlaneInititalizer == null) {
            swimlaneInititalizer = root.elementTextTrim("swimlaneInititalizer");
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        List<? extends Executor> executors = OrgFunctionHelper
                .evaluateOrgFunction(executionContext.getVariableProvider(), swimlaneInititalizer, null);
        if (executors.size() == 0) {
            log.warn("No assignment will be done (OrgFunction return empty array of executor ids)");
            return;
        }
        SwimlaneDefinition swimlaneDefinition = executionContext.getProcessDefinition().getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = executionContext.getProcess().getSwimlaneNotNull(swimlaneDefinition);
        assignmentHelper.assignSwimlane(executionContext, swimlane, executors);
    }
}
