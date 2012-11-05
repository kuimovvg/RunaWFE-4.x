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

package ru.runa.notifier.auth;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.commons.ResourceCommons;

/**
 * Created on 2006
 * 
 * @author Gritsenko_S
 */
class LoginModuleResources extends ResourceCommons {

    private final static String PROPERTY_FILE = "kerberos_module";
    private static Map<String, String> loginModuleParams;
    private static boolean inited = false;

    private static final String APP_NAME_PROPERTY = "appName";
    private static final String MODULE_CLASS_NAME_PROPERTY = "moduleClassName";
    private static final String SERVER_PRINCIPAL_PROPERTY = "serverPrincipal";

    private LoginModuleResources() {
        super(PROPERTY_FILE);
    }

    private static void init() {
        if (!inited) {
            loginModuleParams = new HashMap<String, String>();
            Enumeration<String> propertiesEnum = getKeys(PROPERTY_FILE);
            while (propertiesEnum.hasMoreElements()) {
                String propertyName = propertiesEnum.nextElement();
                String propertyValue = readProperty(propertyName, PROPERTY_FILE);
                loginModuleParams.put(propertyName, propertyValue);
            }
            inited = true;
        }
    }

    public static Map<String, String> getInitParameters() {
        init();
        return loginModuleParams;
    }

    private static String getParameter(String name) {
        init();
        return loginModuleParams.get(name);
    }

    public static String getAppName() {
        return getParameter(APP_NAME_PROPERTY);
    }

    public static String getServerPrincipal() {
        return getParameter(SERVER_PRINCIPAL_PROPERTY);
    }

    public static String getModuleClassName() {
        return getParameter(MODULE_CLASS_NAME_PROPERTY);
    }

}
