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
package ru.runa.af.service.impl.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.logic.ProfileLogic;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.ProfileServiceLocal;
import ru.runa.af.service.ProfileServiceRemote;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class ProfileServiceBean implements ProfileServiceLocal, ProfileServiceRemote {
    @Autowired
    private ProfileLogic profileLogic;

    @Override
    public void saveProfile(Subject subject, Profile profile) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(profile);
        profileLogic.saveProfile(subject, profile);
    }

    @Override
    public Profile getProfile(Subject subject) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return profileLogic.getProfile(subject);
    }

    @Override
    public void deleteProfile(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        profileLogic.deleteProfile(subject, actorId);
    }

    @Override
    public void deleteProfiles(Subject subject, List<Long> actorIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(actorIds);
        profileLogic.deleteProfiles(subject, actorIds);
    }

    @Override
    public void changeActiveBatchPresentation(Subject subject, Long actorId, String batchPresentationId, String newActiveBatchName)
            throws ExecutorOutOfDateException, AuthenticationException, AuthorizationException, BatchPresentationNotFoundException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentationId);
        ArgumentsCommons.checkNotNull(newActiveBatchName);
        profileLogic.changeActiveBatchPresentation(subject, actorId, batchPresentationId, newActiveBatchName);
    }

}
