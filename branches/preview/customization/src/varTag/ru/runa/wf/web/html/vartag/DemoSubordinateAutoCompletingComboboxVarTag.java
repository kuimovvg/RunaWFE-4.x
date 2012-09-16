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

import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.organizationfunction.DemoSubordinateRecursive;
import ru.runa.af.organizationfunction.OrganizationFunctionException;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationComparator;
import ru.runa.delegate.DelegateFactory;

/**
 * <p>
 * Created on 20.03.2006 17:54:22
 * </p>
 * 
 */

public class DemoSubordinateAutoCompletingComboboxVarTag extends AbstractAutoCompletionComboBoxVarTag {
    
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    public List<Actor> getActors(Subject subject, String varName, Object varValue) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        List<Actor> subordinates = getSubordinates(subject);
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        Collections.sort(subordinates, new BatchPresentationComparator(batchPresentation));
        return subordinates;
    }

    private List<Actor> getSubordinates(Subject subject) throws AuthenticationException {
        Actor actor = DelegateFactory.getInstance().getAuthenticationService().getActor(subject);
        Object[] parameters = new Object[1];
        parameters[0] = Long.toString(actor.getCode());
        try {
            List<Actor> actors = new DemoSubordinateRecursive().getSubordinateActors(executorDAO, parameters);
            actors.add(0, actor);
            return actors;
        } catch (OrganizationFunctionException e) {
            throw new InternalApplicationException(e);
        }
    }

}
