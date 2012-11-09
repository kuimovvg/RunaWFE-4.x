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

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;

public class CancelOldProcesses implements TaskHandler {
    @Override
    public void configure(String configurationName) throws TaskHandlerException {
    }

    @Override
    public void configure(byte[] configuration) throws TaskHandlerException {
    }

    @Override
    public void handle(Subject subject, IVariableProvider variableProvider, WfTask wfTask) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getExecutionService();
            BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
            FilterCriteria filter = FilterCriteriaFactory.getFilterCriteria(batchPresentation, 3);
            Date lastDate = new Date();
            long timeout = variableProvider.getNotNull(long.class, "timeout");
            lastDate.setTime(System.currentTimeMillis() - timeout * 3600 * 1000);
            filter.applyFilterTemplates(new String[] { "", "" });
            Map<Integer, FilterCriteria> map = batchPresentation.getFilteredFields();
            map.put(new Integer(3), filter);
            batchPresentation.setFilteredFields(map);
            List<WfProcess> processes = executionService.getProcesses(subject, batchPresentation);
            for (WfProcess process : processes) {
                if (process.getStartDate().before(lastDate) && !Objects.equal(process.getId(), wfTask.getProcessId())) {
                    executionService.cancelProcess(subject, process.getId());
                }
            }
            boolean periodic = variableProvider.getNotNull(boolean.class, "isPeriodic");
            if (!periodic) {
                executionService.completeTask(subject, wfTask.getId(), new HashMap<String, Object>());
            }
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
