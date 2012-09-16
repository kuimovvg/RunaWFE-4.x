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
package ru.runa.af.authenticaion;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.login.AppConfigurationEntry;

import ru.runa.InternalApplicationException;
import ru.runa.commons.ResourceCommons;

/**
 * 
 * Created on 30.06.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class LoginModuleResources extends ResourceCommons {

    private final static String PROPERTY_FILE = "login_module";

    private LoginModuleResources() {
        super(PROPERTY_FILE);
    }

    public static AppConfigurationEntry[] getAppConfigurationEntry() {
        List<AppConfigurationEntry> loginModuleList = new ArrayList<AppConfigurationEntry>();
        Enumeration<String> moduleNamesEnum = getKeys(PROPERTY_FILE);
        while (moduleNamesEnum.hasMoreElements()) {
            String moduleName = moduleNamesEnum.nextElement();
            AppConfigurationEntry.LoginModuleControlFlag flag = getLoginModuleControlFlags(moduleName);
            loginModuleList.add(new AppConfigurationEntry(moduleName, flag, new HashMap<String, Object>()));
        }
        return loginModuleList.toArray(new AppConfigurationEntry[loginModuleList.size()]);
    }

    private static AppConfigurationEntry.LoginModuleControlFlag getLoginModuleControlFlags(String loginModuleName) {
        String flagName = readProperty(loginModuleName, PROPERTY_FILE);
        if (flagName != null) {
            if (flagName.equalsIgnoreCase("OPTIONAL")) {
                return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
            }
            if (flagName.equalsIgnoreCase("SUFFICIENT")) {
                return AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
            }
            if (flagName.equalsIgnoreCase("REQUIRED")) {
                return AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
            }
            if (flagName.equalsIgnoreCase("REQUISITE")) {
                return AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
            }
        }
        throw new InternalApplicationException(flagName + "is not a valid LoginModuleControlFlag");
    }
}
