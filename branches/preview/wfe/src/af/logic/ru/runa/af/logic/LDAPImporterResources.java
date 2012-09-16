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
 * Created on 19.04.2006
 *
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
class LDAPImporterResources extends ResourceCommons {
    private final static String PROPERTY_FILE = "ldap-importer";

    private final static String SERVER_URL = "server.url";

    private final static String OU = "ou";

    private final static String DC = "dc";

    private final static String SECURITY_PRINCIPAL = "principal";

    private final static String PASSWORD = "password";

    private LDAPImporterResources() {
        super(PROPERTY_FILE);
    }

    public static String getServerURL() {
        return readProperty(SERVER_URL, PROPERTY_FILE);
    }

    public static String[] getOU() {
        String ouStrings = readProperty(OU, PROPERTY_FILE);
        return ouStrings.split(";");
    }

    public static String getDC() {
        return readProperty(DC, PROPERTY_FILE);
    }

    public static String getSECURITY_PRINCIPAL() {
        return readProperty(SECURITY_PRINCIPAL, PROPERTY_FILE);
    }

    public static String getLdapPassword() {
        return readProperty(PASSWORD, PROPERTY_FILE);
    }
}
