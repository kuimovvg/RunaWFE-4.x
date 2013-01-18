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

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Executor;

public class ExecutorNameRenderer extends ExecutorRendererBase {

    @Override
    protected List<? extends Executor> loadExecutors(Subject subject) throws Exception {
        ExecutorService executorService = Delegates.getExecutorService();
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        return executorService.getAll(subject, batchPresentation);
    }

    @Override
    protected String getValue(Executor executor) {
        return executor.getName();
    }

    @Override
    protected Executor getExecutor(Subject subject, String name) throws Exception {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutor(subject, name);
    }
}
