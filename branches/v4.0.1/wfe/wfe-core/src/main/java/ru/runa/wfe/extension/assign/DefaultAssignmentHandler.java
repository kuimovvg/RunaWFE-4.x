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
package ru.runa.wfe.extension.assign;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.orgfunction.OrgFunctionHelper;
import ru.runa.wfe.user.Executor;

/**
 * Created on 09.12.2004
 */
public class DefaultAssignmentHandler implements AssignmentHandler {
    @Autowired
    protected AssignmentHelper assignmentHelper;
    private OrgFunction function;

    @Override
    public void setConfiguration(String configuration) {
        // Parse and save assignment parameters separate each other
        function = OrgFunctionHelper.parseOrgFunction(configuration);
    }

    @Override
    public void assign(ExecutionContext executionContext, Assignable assignable) {
        // Get executors, which will be assigned.
        List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(function, executionContext.getVariableProvider());
        if (executors.size() == 0) {
            // Nobody can be assigned. Return unassigned value.
            return;
        }
        assignmentHelper.assignSwimlane(executionContext, assignable, executors);
    }
}
