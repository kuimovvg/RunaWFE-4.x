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
package ru.runa.service.delegate;

import javax.security.auth.Subject;

import ru.runa.service.af.ProfileService;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.Profile;

public class ProfileServiceDelegate extends EJB3Delegate implements ProfileService {

    public ProfileServiceDelegate() {
        super(ProfileService.class);
    }

    private ProfileService getProfileService() {
        return getService();
    }

    @Override
    public Profile getProfile(Subject subject) throws AuthenticationException {
        return getProfileService().getProfile(subject);
    }

    @Override
    public void setActiveBatchPresentation(Subject subject, String batchPresentationId, String newActiveBatchName) throws AuthenticationException {
        getProfileService().setActiveBatchPresentation(subject, batchPresentationId, newActiveBatchName);
    }

    @Override
    public void deleteBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        getProfileService().deleteBatchPresentation(subject, batchPresentation);
    }

    @Override
    public BatchPresentation createBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        return getProfileService().createBatchPresentation(subject, batchPresentation);
    }

    @Override
    public void saveBatchPresentation(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        getProfileService().saveBatchPresentation(subject, batchPresentation);
    }

}
