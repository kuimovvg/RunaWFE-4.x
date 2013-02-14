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
package ru.runa.wfe.service.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.decl.ProfileServiceLocal;
import ru.runa.wfe.service.decl.ProfileServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ProfileLogic;

import com.google.common.base.Preconditions;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ProfileAPI", serviceName = "ProfileWebService")
@SOAPBinding
public class ProfileServiceBean implements ProfileServiceLocal, ProfileServiceRemote {
    @Autowired
    private ProfileLogic profileLogic;

    @Override
    public Profile getProfile(User user) {
        Preconditions.checkNotNull(user);
        return profileLogic.getProfile(user, user.getActor().getId());
    }

    @Override
    public void setActiveBatchPresentation(User user, String batchPresentationId, String newActiveBatchName) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentationId);
        Preconditions.checkNotNull(newActiveBatchName);
        profileLogic.changeActiveBatchPresentation(user, batchPresentationId, newActiveBatchName);
    }

    @Override
    public void deleteBatchPresentation(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        profileLogic.deleteBatchPresentation(user, batchPresentation);
    }

    @Override
    public BatchPresentation createBatchPresentation(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return profileLogic.createBatchPresentation(user, batchPresentation);
    }

    @Override
    public void saveBatchPresentation(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        profileLogic.saveBatchPresentation(user, batchPresentation);
    }

}
