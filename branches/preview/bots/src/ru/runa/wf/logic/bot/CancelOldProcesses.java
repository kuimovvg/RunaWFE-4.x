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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.presentation.filter.FilterCriteriaFactory;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;

public class CancelOldProcesses implements TaskHandler {
    public void configure(String configurationName) throws TaskHandlerException {
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());
            BatchPresentation batchPresentation = WFProfileStrategy.PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation()
                    .clone();
            FilterCriteria filter = FilterCriteriaFactory.getFilterCriteria(batchPresentation, 3);
            Date lastDate = new Date();
            lastDate.setTime(System.currentTimeMillis() - ((Long) variables.get("timeout")) * 3600 * 1000);
            filter.applyFilterTemplates(new String[] { "", "" });
            Map<Integer, FilterCriteria> map = batchPresentation.getFilteredFieldsMap();
            map.put(new Integer(3), filter);
            batchPresentation.setFilteredFieldsMap(map);
            List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(subject, batchPresentation);
            for (ProcessInstanceStub process : processes) {
                if (process.getStartDate().before(lastDate) && process.getId() != taskStub.getProcessInstanceId()) {
                    executionService.cancelProcessInstance(subject, process.getId());
                }
            }
            if (!(Boolean) variables.get("isPeriodic")) {
                executionService
                        .completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), new HashMap<String, Object>());
            }
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
