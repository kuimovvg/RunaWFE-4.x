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
package ru.runa.wfe.security.auth;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.commons.ResourceCommons;

/**
 * Created on 11.01.2006
 *
 * @author Gritsenko_S
 */
public class KerberosLoginModuleResources extends ResourceCommons {

    private static final String RTN_PROPERTY_FILE = "kerberos_module";
    private static final String WEB_PROPERTY_FILE = "kerberos_web_support";

    private static Map<String, Map<String, String>> loginParams = new HashMap<String, Map<String, String>>();

    private static final String APP_NAME_PROPERTY = "appName";
    private static final String MODULE_CLASS_NAME_PROPERTY = "moduleClassName";
    private static final String SERVER_PRINCIPAL_PROPERTY = "serverPrincipal";

    public static final KerberosLoginModuleResources rtnKerberosResources = new KerberosLoginModuleResources(RTN_PROPERTY_FILE);
    public static final KerberosLoginModuleResources webKerberosResources = new KerberosLoginModuleResources(WEB_PROPERTY_FILE);

    public KerberosLoginModuleResources(String resourceFileName) {
        super(resourceFileName);
        init(resourceBundleName);
    }

    private static synchronized void init(String propertyFile) {
        if (loginParams.containsKey(propertyFile)) {
            return;
        }

        Enumeration propertiesEnum = getKeys(propertyFile);
        HashMap<String, String> initModuleParams = new HashMap<String, String>();
        while (propertiesEnum.hasMoreElements()) {
            String propertyName = (String) propertiesEnum.nextElement();
            String propertyValue = readProperty(propertyName, propertyFile);

            initModuleParams.put(propertyName, propertyValue);
        }
        Map<String, Map<String, String>> newloginParamsParams = new HashMap<String, Map<String, String>>();
        newloginParamsParams.putAll(loginParams);
        newloginParamsParams.put(propertyFile, initModuleParams);
        loginParams = newloginParamsParams;
    }

    public Map<String, String> getInitParameters() {
        return loginParams.get(resourceBundleName);
    }

    private String getParameter(String name) {
        return getInitParameters().get(name);
    }

    public String getServerPrincipal() {
        return getParameter(SERVER_PRINCIPAL_PROPERTY);
    }

    public String getAppName() {
        return getParameter(APP_NAME_PROPERTY);
    }

    public String getModuleClassName() {
        return getParameter(MODULE_CLASS_NAME_PROPERTY);
    }
}
