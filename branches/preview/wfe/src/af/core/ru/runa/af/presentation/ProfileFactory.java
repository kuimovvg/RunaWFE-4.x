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
package ru.runa.af.presentation;

import ru.runa.af.CoreResources;

public class ProfileFactory {
    private static ProfileFactory instance;

    private final ProfileStrategy profileStrategy;

    private ProfileFactory() {
        try {
            Class<?> clazz = Class.forName(CoreResources.getDefaultProfileStrategyClassName());
            profileStrategy = ((ProfileStrategy) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        } catch (InstantiationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static ProfileFactory getInstance() {
        if (instance == null) {
            instance = new ProfileFactory();
        }
        return instance;
    }

    public Profile getDefaultProfile(Long actorId) {
        Profile profile = profileStrategy.createDefaultProfile();
        profile.setActorId(actorId);
        return profile;
    }
}
