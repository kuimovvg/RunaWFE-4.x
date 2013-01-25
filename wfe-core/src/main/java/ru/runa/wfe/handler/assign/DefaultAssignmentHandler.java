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
package ru.runa.wfe.handler.assign;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.os.OrgFunctionHelper;
import ru.runa.wfe.user.Executor;

/**
 * Created on 09.12.2004
 */
public class DefaultAssignmentHandler implements AssignmentHandler {
    private static final char FUNCTION_SEPARATER = ';';

    // Function to calculate assigned actor
    private String swimlaneInititalizer;

    @Autowired
    protected AssignmentHelper assignmentHelper;

    @Override
    public void setConfiguration(String configuration) {
        // Parse and save assignment parameters separate each other
        int functionSeparatorIndex = configuration.indexOf(FUNCTION_SEPARATER);
        swimlaneInititalizer = (functionSeparatorIndex == -1) ? configuration : configuration.substring(0, functionSeparatorIndex);
    }

    @Override
    public void assign(ExecutionContext executionContext, Assignable assignable) {
        // Get executor IDs, which will be assigned.
        List<? extends Executor> orgFunctionExecutors = OrgFunctionHelper.evaluateOrgFunction(executionContext.getVariableProvider(),
                swimlaneInititalizer, null);
        // Create (or get if exist) group G, which contains all executors,
        // asigned to task.
        // Substitutors also in group G. Assign task to what group G.
        // We assume, what assigned executors set is immutable during token
        // livetime. If this
        // condition violated, need to reassign token.
        // Reassign can be done as:
        // 1. Periodically reassign all tasks (at night, then loading is
        // small)
        // 2. On state one of executor's in set is changing (new Actor in
        // group, and so one)
        if (orgFunctionExecutors == null || orgFunctionExecutors.size() == 0) {
            // Nobody can be assigned. Return unassigned value.
            return;
        }
        assignmentHelper.assignSwimlane(executionContext, assignable, orgFunctionExecutors);
    }
}
