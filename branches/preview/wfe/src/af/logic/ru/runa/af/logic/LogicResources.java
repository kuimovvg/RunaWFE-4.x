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

import ru.runa.commons.ResourceCommons;

/**
 * This Resource class is used by both logic and dao layers in future if we devide logic from dao ResourcesDAO will be introduced Created on 25.03.2005
 *
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public final class LogicResources extends ResourceCommons {
    private static final String PROPERTIES = "af_logic";

    private LogicResources() {
        super(PROPERTIES);
    }

    /* logic DAO */
    private static final String DAO_IMPL_PACKAGE_NAME = "ru.runa.af.dao.impl.package.name";

    private static final String DAO_IMPL_DB_PREFIX_NAME = "ru.runa.af.dao.impl.db.prefix.name";

    public static final String IS_DATABASE_INITIALIZED_VARIABLE_NAME = "ru.runa.database_initialized";
    public static final String DATABASE_VERSION_VARIABLE_NAME = "ru.runa.database_version";

    public static final String ON_LOGIN_HANDLERS = "ru.runa.af.logic.on_login";
    public static final String ON_STATUSCHANGE_HANDLERS = "ru.runa.af.logic.on_status_change";

    public static String getAFDAOImplPackageName() {
        return readProperty(DAO_IMPL_PACKAGE_NAME, PROPERTIES);
    }

    public static String getAFDAOImplDBPrefixName() {
        return readProperty(DAO_IMPL_DB_PREFIX_NAME, PROPERTIES);
    }

    public static final String LOGIN_MODULE_CONFIGURATION = "LoginModuleConfiguration";

    private static final String INITIALIZER_CLASS_NAME = "ru.runa.af.db.initializer.impl.class.name";

    public static String getDbInitializeClassName() {
        return readProperty(INITIALIZER_CLASS_NAME, PROPERTIES);
    }

    public static String[] getOnLoginHandlers() {
        String property = readPropertyIfExist(ON_LOGIN_HANDLERS, PROPERTIES);
        return property == null ? new String[0] : property.split(";");
    }

    public static String[] getOnStatusChangeHandlers() {
        String property = readPropertyIfExist(ON_STATUSCHANGE_HANDLERS, PROPERTIES);
        return property == null ? new String[0] : property.split(";");
    }
}
