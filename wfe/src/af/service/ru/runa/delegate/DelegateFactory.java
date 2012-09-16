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
package ru.runa.delegate;

import java.util.Map;

import ru.runa.InternalApplicationException;
import ru.runa.af.service.AuthenticationService;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.BotsService;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ProfileService;
import ru.runa.af.service.RelationService;
import ru.runa.af.service.SubstitutionService;
import ru.runa.af.service.SystemService;
import ru.runa.delegate.impl.AuthenticationServiceDelegateImpl;
import ru.runa.delegate.impl.AuthorizationServiceDelegateImpl;
import ru.runa.delegate.impl.BotsServiceDelegateImpl;
import ru.runa.delegate.impl.DefinitionServiceDelegateImpl;
import ru.runa.delegate.impl.EJB3Delegate;
import ru.runa.delegate.impl.ExecutionServiceDelegateImpl;
import ru.runa.delegate.impl.ExecutorServiceDelegateImpl;
import ru.runa.delegate.impl.ProfileServiceDelegateImpl;
import ru.runa.delegate.impl.RelationServiceDelegateImpl;
import ru.runa.delegate.impl.SubstitutionServiceDelegateImpl;
import ru.runa.delegate.impl.SystemServiceDelegateImpl;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;

import com.google.common.collect.Maps;

/**
 * Provides delegates. Delegate's type can not be switched at run-time. Created on 18.10.2004
 */
public class DelegateFactory {

    private DelegateFactory() {
    }

    private static DelegateFactory instance = null;

    public static DelegateFactory getInstance() {
        if (instance == null) {
            instance = new DelegateFactory();
        }
        return instance;
    }

    private static Map<Class<?>, Object> delegates = Maps.newHashMap();

    protected <T extends EJB3Delegate> T getDelegate(Class<T> delegateClass) {
        if (!delegates.containsKey(delegateClass)) {
            Object delegate = createDelegate(delegateClass);
            delegates.put(delegateClass, delegate);
        }
        return (T) delegates.get(delegateClass);
    }

    private EJB3Delegate createDelegate(Class<? extends EJB3Delegate> delegateClass) {
        try {
            String strategyName = DelegateResources.getDelegateInterfaceType();
            boolean remote = "Remote".equals(strategyName);
            EJB3Delegate delegate = delegateClass.newInstance();
            delegate.setRemote(remote);
            return delegate;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public AuthenticationService getAuthenticationService() {
        return getDelegate(AuthenticationServiceDelegateImpl.class);
    }

    public AuthorizationService getAuthorizationService() {
        return getDelegate(AuthorizationServiceDelegateImpl.class);
    }

    public ExecutorService getExecutorService() {
        return getDelegate(ExecutorServiceDelegateImpl.class);
    }

    public RelationService getRelationService() {
        return getDelegate(RelationServiceDelegateImpl.class);
    }

    public SystemService getSystemService() {
        return getDelegate(SystemServiceDelegateImpl.class);
    }

    public ProfileService getProfileService() {
        return getDelegate(ProfileServiceDelegateImpl.class);
    }

    public SubstitutionService getSubstitutionService() {
        return getDelegate(SubstitutionServiceDelegateImpl.class);
    }

    public BotsService getBotsService() {
        return getDelegate(BotsServiceDelegateImpl.class);
    }

    public DefinitionService getDefinitionService() {
        return getDelegate(DefinitionServiceDelegateImpl.class);
    }

    public ExecutionService getExecutionService() {
        return getDelegate(ExecutionServiceDelegateImpl.class);
    }

}
