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
package ru.runa.wfe.service.delegate;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class ProfileServiceDelegate extends EJB3Delegate implements ProfileService {

    public ProfileServiceDelegate() {
        super(ProfileService.class);
    }

    private ProfileService getProfileService() {
        return getService();
    }

    @Override
    public Profile getProfile(User user) {
        return getProfileService().getProfile(user);
    }

    @Override
    public void setActiveBatchPresentation(User user, String batchPresentationId, String newActiveBatchName) {
        getProfileService().setActiveBatchPresentation(user, batchPresentationId, newActiveBatchName);
    }

    @Override
    public void deleteBatchPresentation(User user, BatchPresentation batchPresentation) {
        getProfileService().deleteBatchPresentation(user, batchPresentation);
    }

    @Override
    public BatchPresentation createBatchPresentation(User user, BatchPresentation batchPresentation) {
        return getProfileService().createBatchPresentation(user, batchPresentation);
    }

    @Override
    public BatchPresentation saveBatchPresentation(User user, BatchPresentation batchPresentation) {
        return getProfileService().saveBatchPresentation(user, batchPresentation);
    }

}
