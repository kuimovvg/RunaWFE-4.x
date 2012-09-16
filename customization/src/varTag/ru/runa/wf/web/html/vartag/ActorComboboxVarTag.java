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
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 10.11.2005
 * 
 */
public class ActorComboboxVarTag extends AbstractActorComboBoxVarTag {

    @Override
    public List<Actor> getActors(Subject subject, String varName, Object varValue) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        return executorService.getActors(subject, batchPresentation);
    }

    public String getActorPropertyToUse() {
        return "code";
    }

    public String getActorPropertyToDisplay() {
        return "fullName";
    }
}
