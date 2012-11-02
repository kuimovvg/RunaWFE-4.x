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
package ru.runa.wfe.user.logic;

import java.util.List;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.dao.ProfileDAO;

import com.google.common.collect.Lists;

/**
 * Actor's profile management.
 * 
 * @author Dofs
 * @since 1.0
 */
public class ProfileLogic extends CommonLogic {
    @Autowired
    private ProfileDAO profileDAO;

    public void saveProfiles(Subject subject, List<Profile> profiles) throws AuthorizationException {
        for (Profile profile : profiles) {
            checkPermissionAllowed(subject, profile.getActor(), ExecutorPermission.UPDATE);
            profileDAO.store(profile);
        }
    }

    private Profile getProfileNotNull(Actor actor) {
        Profile profile = profileDAO.getProfile(actor);
        if (profile == null) {
            profile = new Profile();
            profile.setActor(actor);
            profileDAO.store(profile);
        }
        return profile;
    }

    public List<Profile> getProfiles(Subject subject, List<Long> actorIds) throws ExecutorDoesNotExistException {
        List<Profile> result = Lists.newArrayListWithCapacity(actorIds.size());
        for (Long actorId : actorIds) {
            Actor actor = executorDAO.getActor(actorId);
            checkPermissionAllowed(subject, actor, Permission.READ);
            result.add(getProfileNotNull(actor));
        }
        return result;
    }

    public Profile getProfile(Subject subject, Long actorId) throws AuthorizationException, ExecutorDoesNotExistException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionAllowed(subject, actor, Permission.READ);
        return getProfileNotNull(actor);
    }

    public void changeActiveBatchPresentation(Subject subject, String batchPresentationId, String newActiveBatchName) {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Profile profile = profileDAO.getProfile(actor);
        profile.setActiveBatchPresentation(batchPresentationId, newActiveBatchName);
        profileDAO.store(profile);
    }

    public void deleteBatchPresentation(Subject subject, BatchPresentation batchPresentation) {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Profile profile = profileDAO.getProfile(actor);
        profile.deleteBatchPresentation(batchPresentation);
        profileDAO.store(profile);
    }

    public BatchPresentation createBatchPresentation(Subject subject, BatchPresentation batchPresentation) {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Profile profile = profileDAO.getProfile(actor);
        profile.addBatchPresentation(batchPresentation);
        profile.setActiveBatchPresentation(batchPresentation.getCategory(), batchPresentation.getName());
        profileDAO.store(profile);
        return batchPresentation;
    }

    public void saveBatchPresentation(Subject subject, BatchPresentation batchPresentation) {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        Profile profile = profileDAO.getProfile(actor);
        if (BatchPresentationConsts.DEFAULT_NAME.equals(batchPresentation.getName())) {
            throw new InternalApplicationException("default batch presentation cannot be changed");
        }
        profile.addBatchPresentation(batchPresentation);
        profileDAO.store(profile);
    }
}
