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
package ru.runa.service.af.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.ProfileServiceLocal;
import ru.runa.service.af.ProfileServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.logic.ProfileLogic;

import com.google.common.base.Preconditions;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ProfileServiceBean implements ProfileServiceLocal, ProfileServiceRemote {
    @Autowired
    private ProfileLogic profileLogic;

    @Override
    public Profile getProfile(Subject subject) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        return profileLogic.getProfile(subject, actor.getId());
    }

    @Override
    public void setActiveBatchPresentation(Subject subject, String batchPresentationId, String newActiveBatchName) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentationId);
        Preconditions.checkNotNull(newActiveBatchName);
        profileLogic.changeActiveBatchPresentation(subject, batchPresentationId, newActiveBatchName);
    }

    @Override
    public void deleteBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        profileLogic.deleteBatchPresentation(subject, batchPresentation);
    }

    @Override
    public BatchPresentation createBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return profileLogic.createBatchPresentation(subject, batchPresentation);
    }

    @Override
    public void saveBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        profileLogic.saveBatchPresentation(subject, batchPresentation);
    }

}
