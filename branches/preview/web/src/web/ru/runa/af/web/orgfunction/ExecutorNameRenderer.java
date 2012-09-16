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
package ru.runa.af.web.orgfunction;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.Executor;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;

public class ExecutorNameRenderer extends ExecutorRendererBase {

    @Override
    protected List<? extends Executor> loadExecutors(Subject subject) throws Exception {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        return executorService.getAll(subject, batchPresentation);
    }

    @Override
    protected String getValue(Executor executor) {
        return executor.getName();
    }

    @Override
    protected Executor getExecutor(Subject subject, String name) throws Exception {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        return executorService.getExecutor(subject, name);
    }
}
