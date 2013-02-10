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
package ru.runa.af.web;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * Created on 2007
 * 
 */
public class KrbLoginConfiguration extends Configuration {

    static {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }

    public KrbLoginConfiguration() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
        if (appName.equals(KrbSupportResources.getAppName())) {
            AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry(KrbSupportResources.getModuleClassName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, KrbSupportResources.getInitParameters());
            return new AppConfigurationEntry[] { appConfigurationEntry };
        }
        return null;
    }
}
