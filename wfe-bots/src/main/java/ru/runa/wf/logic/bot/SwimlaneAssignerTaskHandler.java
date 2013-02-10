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

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.os.OrgFunction;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;

/**
 * (Re)Assigns swimlane.
 * 
 * @author dofs
 * @since 2.0
 */
public class SwimlaneAssignerTaskHandler extends TaskHandlerBase {
    private static final String SWIMLANE_NAME_PROPERTY = "swimlaneName";
    private static final String ASSIGNER_FUNCTION_PROPERTY = "assignerFunction";
    private String swimlaneName;
    private OrgFunction function;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        Properties properties = new Properties();
        properties.load(new StringReader(configuration));
        swimlaneName = properties.getProperty(SWIMLANE_NAME_PROPERTY);
        Preconditions.checkNotNull(swimlaneName, SWIMLANE_NAME_PROPERTY);
        String swimlaneInitializer = properties.getProperty(ASSIGNER_FUNCTION_PROPERTY);
        function = OrgFunctionHelper.parseOrgFunction(swimlaneInitializer);
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(function, variableProvider);
        if (executors.size() != 1) {
            throw new Exception("assigner (organization) function return more than 1 actor to be assigned in swimlane");
        }
        Delegates.getExecutionService().assignSwimlane(user, task.getProcessId(), swimlaneName, executors.get(0));
        return null;
    }
}
