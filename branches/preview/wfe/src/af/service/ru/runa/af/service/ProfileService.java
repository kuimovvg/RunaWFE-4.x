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
package ru.runa.af.service;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;

public interface ProfileService {

    public void saveProfile(Subject subject, Profile profile) throws AuthenticationException;

    public Profile getProfile(Subject subject) throws AuthenticationException;

    public void changeActiveBatchPresentation(Subject subject, Long actorId, String batchPresentationId, String newActiveBatchName)
            throws ExecutorOutOfDateException, AuthenticationException, AuthorizationException, BatchPresentationNotFoundException;

    public void deleteProfile(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;

    public void deleteProfiles(Subject subject, List<Long> actorIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException;
}
