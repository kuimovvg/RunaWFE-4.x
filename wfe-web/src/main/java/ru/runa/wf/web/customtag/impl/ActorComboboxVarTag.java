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
package ru.runa.wf.web.customtag.impl;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Actor;

/**
 * Created on 10.11.2005
 * 
 */
public class ActorComboboxVarTag extends AbstractActorComboBoxVarTag {

    @Override
    public List<Actor> getActors(Subject subject, String varName, Object varValue) {
        BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        return DelegateFactory.getExecutorService().getActors(subject, batchPresentation);
    }

    @Override
    public String getActorPropertyToUse() {
        return "code";
    }

    @Override
    public String getActorPropertyToDisplay() {
        return "fullName";
    }
}
