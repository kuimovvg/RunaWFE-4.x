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
package ru.runa.wf.logic.bot;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.handler.bot.TaskHandlerException;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

/**
 * (Re)Assigns swimlane.
 * 
 * @author dofs
 * @since 2.0
 */
public class SwimlaneAssignerTaskHandler extends TaskHandlerBase {
    private static final String SWIMLANE_NAME_PROPERTY = "swimlaneName";
    private static final String ASSIGNER_FUNCTION_PROPERTY = "assignerFunction";
    private Properties properties = new Properties();

    @Override
    public void setConfiguration(String configuration) throws Exception {
        properties.load(new StringReader(configuration));
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) {
        String swimlaneName = properties.getProperty(SWIMLANE_NAME_PROPERTY);
        String assignerFunction = properties.getProperty(ASSIGNER_FUNCTION_PROPERTY);
        Long actorCode = SubjectPrincipalsHelper.getActor(subject).getCode();
        List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(variableProvider, assignerFunction, actorCode);
        if (executors.size() != 1) {
            throw new TaskHandlerException("assigner (organization) function return more than 1 actor to be assigned in swimlane");
        }
        Delegates.getExecutionService().assignSwimlane(subject, task.getProcessId(), swimlaneName, executors.get(0));
        return null;
    }
}
