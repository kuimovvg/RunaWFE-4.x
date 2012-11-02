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
package ru.runa.wfe.user.dao;

import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Profile;

/**
 * DAO for managing user profiles.
 * 
 * @author Konstantinov Aleksey 23.02.2012
 */
public class ProfileDAO extends CommonDAO {

    /**
     * Store or update user profile.
     * 
     * @param profile
     *            New user profile.
     */
    public void store(Profile profile) {
        if (profile.getId() == null && getProfile(profile.getActor()) != null) {
            throw new IllegalArgumentException("profile.id == null but insert not allowed");
        }
        getHibernateTemplate().saveOrUpdate(profile);
    }

    /**
     * Load profile for user. Return null, if no profile for user found.
     * 
     * @param actor
     *            Actor to load profile.
     * @return Actor profile or null.
     */
    public Profile getProfile(Actor actor) {
        return (Profile) getFirstOrNull(getHibernateTemplate().find("from Profile where actor = ?", actor));
    }

    /**
     * Removes profile for user.
     * 
     * @param actor
     *            Actor to remove profile.
     */
    public void deleteProfile(Actor actor) {
        Profile profile = getProfile(actor);
        if (profile != null) {
            getHibernateTemplate().delete(profile);
        }
    }

}
