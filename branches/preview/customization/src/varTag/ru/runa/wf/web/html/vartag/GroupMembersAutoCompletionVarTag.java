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
package ru.runa.wf.web.html.vartag;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;

public class GroupMembersAutoCompletionVarTag extends AbstractAutoCompletionComboBoxVarTag {

    @Override
    public List<Actor> getActors(Subject subject, String varName, Object varValue) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);

        Group group = executorService.getGroup(subject, varName);
//        Executor[] executors = delegate.getGroupChildren(subject, group, batchPresentation, false);
//        ArrayList<Executor> actorsList = new ArrayList<Executor>();
//        for (int i = 0; i < executors.length; i++) {
//            if (executors[i] instanceof Actor) {
//                actorsList.add(executors[i]);
//            }
//        }
        return executorService.getGroupActors(subject, group);
    }
}
