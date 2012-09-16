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

import ru.runa.commons.ResourceCommons;

/**
 * 
 * Created on 30.06.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
class ADResources extends ResourceCommons {

    private final static String PROPERTIES = "ad_password_login_module";

    private ADResources() {
        super(PROPERTIES);
    }

    public static final String SERVER_URL = "ru.runa.af.active.directory.server.url";

    public static final String DOMAIN_NAME = "ru.runa.af.active.directory.damain.name";

    public static final String AUTHENTICATION_NAME = "ru.runa.af.active.directory.authentication";

    public static final String CREDENTIAL_FORMAT_NAME = "ru.runa.af.active.directory.userNameFormat";

    public static String getServerUrl() {
        return readProperty(SERVER_URL, PROPERTIES);
    }

    public static String getDomainName() {
        return readProperty(DOMAIN_NAME, PROPERTIES);
    }

    public static String getAuthenticationType() {
        return readPropertyIfExist(AUTHENTICATION_NAME, PROPERTIES, "simple");
    }

    public static String getCredential(String username) {
        String domain = getDomainName();
        String template = readPropertyIfExist(CREDENTIAL_FORMAT_NAME, PROPERTIES, "%domain\\%username");
        return template.replaceAll("%domain", domain).replaceAll("%username", username);
    }
}
