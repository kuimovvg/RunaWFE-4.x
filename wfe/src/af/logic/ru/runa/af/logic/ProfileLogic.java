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
package ru.runa.af.logic;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.ProfileDAO;
import ru.runa.af.dao.impl.HibernateSessionFactory;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.presentation.ProfileFactory;

/**
 * Created on 14.03.2005
 * 
 */
public class ProfileLogic extends CommonLogic {
    @Autowired
    private ProfileDAO profileDAO;

    private void saveProfile(Profile profile) {
        profileDAO.store(profile);
    }

    /**
     * Saves profile of subject owner.
     */
    public void saveProfile(Subject subject, Profile profile) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        profile.setActorId(actor.getId());
        saveProfile(profile);
    }

    /**
     * Saves profile for actor with id=actorId.
     */
    public void saveProfile(Subject subject, Profile profile, Long actorId) throws AuthenticationException, AuthorizationException,
            ExecutorOutOfDateException {
        checkPermissionAllowed(subject, executorDAO.getActor(actorId), ExecutorPermission.UPDATE);
        profile.setActorId(actorId);
        saveProfile(profile);
    }

    public void saveProfile(Subject subject, List<Profile> profiles) throws AuthenticationException, AuthorizationException,
            ExecutorOutOfDateException {
        for (Profile profile : profiles) {
            checkPermissionAllowed(subject, executorDAO.getActor(profile.getActorId()), ExecutorPermission.UPDATE);
        }
        profileDAO.store(profiles);
    }

    private Profile getProfile(Long actorId) {
        Profile profile = profileDAO.getProfile(actorId);
        if (profile == null) {
            profile = ProfileFactory.getInstance().getDefaultProfile(actorId);
        }
        // Always return profile without hibernate collections
        profile = profile.clone();
        profile.getOmmitedBatchPesentations();
        return profile;
    }

    private Profile findProfile(List<Profile> profiles, Long actorId) {
        for (Profile profile : profiles) {
            if (profile.getActorId() == actorId) {
                return profile;
            }
        }
        return null;
    }

    public List<Profile> getProfile(Subject subject, List<Long> actorIds) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        for (Long actorId : actorIds) {
            checkPermissionAllowed(subject, executorDAO.getActor(actorId), Permission.READ);
        }
        List<Profile> profiles = profileDAO.getProfile(actorIds);
        List<Profile> retVal = new ArrayList<Profile>();
        {
            for (int paramIdx = 0; paramIdx < actorIds.size(); ++paramIdx) {
                Profile loadedProfile = findProfile(profiles, actorIds.get(paramIdx));
                if (loadedProfile == null) {
                    loadedProfile = ProfileFactory.getInstance().getDefaultProfile(actorIds.get(paramIdx));
                } else {
                    // Always return profile without hibernate collections
                    HibernateSessionFactory.getSession().evict(loadedProfile);
                    loadedProfile = loadedProfile.clone();
                    loadedProfile.getOmmitedBatchPesentations();
                }
                retVal.add(loadedProfile);
            }
        }
        return retVal;
    }

    public Profile getProfile(Subject subject) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        return getProfile(actor.getId());
    }

    public Profile getProfile(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        checkPermissionAllowed(subject, executorDAO.getActor(actorId), Permission.READ);
        return getProfile(actorId);
    }

    public void deleteProfile(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionAllowed(subject, actor, ExecutorPermission.UPDATE);
        profileDAO.deleteProfile(actorId);
    }

    public void deleteProfiles(Subject subject, List<Long> actorIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        List<Executor> executors = executorDAO.getExecutors(actorIds);
        executors = checkPermissionsOnExecutors(subject, executors, ExecutorPermission.UPDATE);
        profileDAO.deleteProfiles(actorIds);
    }

    public void changeActiveBatchPresentation(Subject subject, Long actorId, String batchPresentationId, String newActiveBatchName)
            throws ExecutorOutOfDateException, AuthenticationException, AuthorizationException, BatchPresentationNotFoundException {
        Profile profile = getProfile(subject, actorId);
        profile.setActiveBatchPresentation(batchPresentationId, newActiveBatchName);
        saveProfile(subject, profile);
    }
}
