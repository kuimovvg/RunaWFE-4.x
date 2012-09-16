package ru.runa.delegate.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.runa.InternalApplicationException;
import ru.runa.delegate.DelegateResources;

public abstract class EJB3Delegate {
    private static InitialContext initialContext;
    private static Map<String, Object> services = new HashMap<String, Object>();
    private boolean remote;

    public void setRemote(boolean remote) {
        this.remote = remote;
    }
    
    protected abstract String getBeanName();

    protected <T> T getService() {
        String jndiName = getBeanName() + "/" + (remote ? "remote" : "local");
        if (!services.containsKey(jndiName)) {
            try {
                Object service = getInitialContext().lookup(jndiName);
                services.put(jndiName, service);
            } catch (NamingException e) {
                throw new InternalApplicationException(e);
            }
        }
        return (T) services.get(jndiName);
    }

    private InitialContext getInitialContext() {
        if (initialContext == null) {
            try {
                Hashtable<String, String> environment = new Hashtable<String, String>();
                if (remote) {
                environment.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
                environment.put(Context.URL_PKG_PREFIXES, getUrlPkgPrefixes());
                environment.put(Context.PROVIDER_URL, getProviderUrl());
                }
                initialContext = new InitialContext(environment);
            } catch (NamingException e) {
                throw new InternalApplicationException(e);
            }
        }
        return initialContext;
    }

    protected String getProviderUrl() {
        return DelegateResources.getDelegateRemoteProviderUrl();
    }

    protected String getUrlPkgPrefixes() {
        return DelegateResources.getDelegateRemoteUrlPkgPrefixes();
    }

    protected String getInitialContextFactory() {
        return DelegateResources.getDelegateRemoteInitialContextFactory();
    }

}
