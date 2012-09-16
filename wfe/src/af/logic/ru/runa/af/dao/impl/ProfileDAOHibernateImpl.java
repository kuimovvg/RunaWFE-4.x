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
package ru.runa.af.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.dao.ProfileDAO;
import ru.runa.af.presentation.Profile;

import com.google.common.collect.Lists;

/**
  * DAO for managing user profiles implementation via hibernate.
  * @author Konstantinov Aleksey 23.02.2012
  */
public class ProfileDAOHibernateImpl extends HibernateDaoSupport implements ProfileDAO {

    @Override
    public void store(Profile profile) {
//  ?      synchronized (ProfileDAOHibernateImpl.class) {
//            Profile loadedProfile = getProfile(profile.getActorId());
//            if (loadedProfile != null) {
//                loadedProfile.syncWith(profile);
//                profile = loadedProfile;
//            }
            getHibernateTemplate().saveOrUpdate(profile);
//        }
    }

    @Override
    public void store(List<Profile> profiles) {
    //  ?        synchronized (ProfileDAOHibernateImpl.class) {
//            long[] actorIds = new long[profiles.size()];
//            for (int i = 0; i < profiles.size(); ++i) {
//                actorIds[i] = profiles.get(i).getActorId();
//            }
//            List<Profile> loadedProfiles = getProfile(actorIds);
//            for (int newProfileIdx = 0; newProfileIdx < profiles.size(); ++newProfileIdx) {
//                Profile storedProfile = findProfile(loadedProfiles, profiles.get(newProfileIdx).getActorId());
//                if (storedProfile != null) {
//                    storedProfile.syncWith(profiles.get(newProfileIdx));
//                } else {
//                    storedProfile = profiles.get(newProfileIdx);
//                }
//                getHibernateTemplate().saveOrUpdate(storedProfile);
//            }
//        }
        getHibernateTemplate().saveOrUpdateAll(profiles);
    }

    @Override
    public Profile getProfile(Long actorId) {
        List<Profile> list = getHibernateTemplate().find("from Profile where actorId = ?", actorId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Profile> getProfile(final List<Long> actorIds) {
        if (actorIds.size() == 0) {
            return Lists.newArrayList();
        }
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Profile>>() {

            @Override
            public List<Profile> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from Profile where actorId in (:ids)");
                query.setParameterList("ids", actorIds);
                return query.list();
            }
        });
    }

    @Override
    public void deleteProfile(Long actorId) {
        Profile profile = getProfile(actorId);
        if (profile != null) {
            getHibernateTemplate().delete(profile);
        }
    }

    @Override
    public void deleteProfiles(List<Long> actorIds) {
        for (Long actorId : actorIds) {
            deleteProfile(actorId);
        }
    }

}
