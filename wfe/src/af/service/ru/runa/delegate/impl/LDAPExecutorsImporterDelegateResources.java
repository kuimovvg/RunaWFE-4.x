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
package ru.runa.delegate.impl;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 19.04.2006
 *
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
class LDAPExecutorsImporterDelegateResources extends ResourceCommons {
    private static final String PROPERTIES = "ldap-importer-delegate";

    private LDAPExecutorsImporterDelegateResources() {
        super(PROPERTIES);
    }

    private static final String DELEGATE_REMOTE_INITIAL_CONTEXT_FACTORY = "initial.context.factory";

    private static final String DELEGATE_REMOTE_URL_PKG_PREFIXES = "url.pkg.prefixes";

    private static final String DELEGATE_REMOTE_PROVIDER_URL = "provider.url";

    public static String getDelegateRemoteInitialContextFactory() {
        return readProperty(DELEGATE_REMOTE_INITIAL_CONTEXT_FACTORY, PROPERTIES);
    }

    public static String getDelegateRemoteProviderUrl() {
        return readProperty(DELEGATE_REMOTE_PROVIDER_URL, PROPERTIES);
    }

    public static String getDelegateRemoteUrlPkgPrefixes() {
        return readProperty(DELEGATE_REMOTE_URL_PKG_PREFIXES, PROPERTIES);
    }
}
