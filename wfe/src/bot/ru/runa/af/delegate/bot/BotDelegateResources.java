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
package ru.runa.af.delegate.bot;

import ru.runa.commons.ResourceCommons;

public final class BotDelegateResources extends ResourceCommons {
    private static final String PROPERTIES = "bot_delegate";

    private BotDelegateResources() {
        super(PROPERTIES);
    }

    /* delegate */
    private static final String DELEGATE_INTERFACE_TYPE = "ru.runa.bot.delegate.interface.type";

    private static final String DELEGATE_REMOTE_INITIAL_CONTEXT_FACTORY = "ru.runa.bot.delegate.remote.initial.context.factory";

    private static final String DELEGATE_REMOTE_URL_PKG_PREFIXES = "ru.runa.bot.delegate.remote.url.pkg.prefixes";

    private static final String DELEGATE_REMOTE_DEFAULT_PROVIDER_URL = "ru.runa.bot.delegate.remote.provider.url.default";

    public static String getDelegateInterfaceType() {
        return readProperty(DELEGATE_INTERFACE_TYPE, PROPERTIES);
    }

    public static String getDelegateRemoteInitialContextFactory() {
        return readProperty(DELEGATE_REMOTE_INITIAL_CONTEXT_FACTORY, PROPERTIES);
    }

    public static String getDelegateDefaultProviderUrl() {
        return readProperty(DELEGATE_REMOTE_DEFAULT_PROVIDER_URL, PROPERTIES);
    }

    public static String getDelegateRemoteUrlPkgPrefixes() {
        return readProperty(DELEGATE_REMOTE_URL_PKG_PREFIXES, PROPERTIES);
    }
}
