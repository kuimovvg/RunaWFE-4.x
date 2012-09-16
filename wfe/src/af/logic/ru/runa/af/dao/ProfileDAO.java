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
package ru.runa.af.dao;

import java.util.List;

import ru.runa.af.presentation.Profile;

/**
  * DAO for managing user profiles.
  * @author Konstantinov Aleksey 23.02.2012
  */
public interface ProfileDAO {

    /**
     * Store or update user profile. 
     * @param profile New user profile.
     */
    public void store(Profile profile);

    /**
     * Store or update users profiles.
     * @param profiles New users profiles.
     */
    public void store(List<Profile> profiles);

    /**
     * Load profile for user. Return null, if no profile for user found.
     * @param actorId Actor identity to load profile. 
     * @return Actor profile or null.
     */
    public Profile getProfile(Long actorId);

    /**
     * Load profiles for users. Return null, if no profiles found.
     * Result list length may be less, when actors count if some users havn't profile.
     * Result order is not specified. 
     * @param actorId Actors identity to load profile. 
     * @return Actors profiles or null.
     */
    public List<Profile> getProfile(List<Long> actorIds);

    /**
     * Removes profile for user.
     * @param actorId Actor identity to remove profile.
     */
    public void deleteProfile(Long actorId);

    /**
     * Removes profiles for users.
     * @param actorIds Actors identities to remove profile.
     */
    public void deleteProfiles(List<Long> actorIds);
}
