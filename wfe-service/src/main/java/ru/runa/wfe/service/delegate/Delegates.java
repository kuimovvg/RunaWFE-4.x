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
package ru.runa.wfe.service.delegate;

import java.util.Map;
import java.util.Properties;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.service.AdminScriptService;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.InitializerService;
import ru.runa.wfe.service.LDAPSynchronizerService;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.SystemService;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Provides delegates. Delegate's type can not be switched at run-time. Created
 * 
 * @since 4.0
 */
public class Delegates {
    private static final Properties PROPERTIES = ClassLoaderUtil.getPropertiesNotNull("wfe.delegate.properties");
    private static Map<Class<?>, Object> delegates = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    protected static <T extends EJB3Delegate> T getDelegate(Class<T> delegateClass) {
        if (!delegates.containsKey(delegateClass)) {
            Object delegate = createDelegate(delegateClass);
            delegates.put(delegateClass, delegate);
        }
        return (T) delegates.get(delegateClass);
    }

    private static EJB3Delegate createDelegate(Class<? extends EJB3Delegate> delegateClass) {
        EJB3Delegate delegate = ClassLoaderUtil.instantiate(delegateClass);
        delegate.setEjbType(PROPERTIES.getProperty("ejb.type"));
        delegate.setEjbJndiNameFormat(PROPERTIES.getProperty("ejb.jndiName.format"));
        return delegate;
    }

    public static AuthenticationService getAuthenticationService() {
        return getDelegate(AuthenticationServiceDelegate.class);
    }

    public static AuthorizationService getAuthorizationService() {
        return getDelegate(AuthorizationServiceDelegate.class);
    }

    public static ExecutorService getExecutorService() {
        return getDelegate(ExecutorServiceDelegate.class);
    }

    public static RelationService getRelationService() {
        return getDelegate(RelationServiceDelegate.class);
    }

    public static SystemService getSystemService() {
        return getDelegate(SystemServiceDelegate.class);
    }

    public static ProfileService getProfileService() {
        return getDelegate(ProfileServiceDelegate.class);
    }

    public static SubstitutionService getSubstitutionService() {
        return getDelegate(SubstitutionServiceDelegate.class);
    }

    public static BotService getBotService() {
        return getDelegate(BotServiceDelegate.class);
    }

    public static DefinitionService getDefinitionService() {
        return getDelegate(DefinitionServiceDelegate.class);
    }

    public static ExecutionService getExecutionService() {
        return getDelegate(ExecutionServiceDelegate.class);
    }

    public static InitializerService getInitializerService() {
        return getDelegate(InitializerServiceDelegate.class);
    }

    public static AdminScriptService getAdminScriptService() {
        return getDelegate(AdminScriptServiceDelegate.class);
    }

    public static BotInvokerService getBotInvokerService(BotStation botStation) {
        BotInvokerServiceDelegate botInvokerService = getDelegate(BotInvokerServiceDelegate.class);
        if (botStation != null && !Strings.isNullOrEmpty(botStation.getAddress())) {
            botInvokerService.setCustomProviderUrl(botStation.getAddress());
        }
        return botInvokerService;
    }

    public static LDAPSynchronizerService getLDAPSynchronizerService() {
        return getDelegate(LDAPSynchronizerServiceDelegate.class);
    }

}
