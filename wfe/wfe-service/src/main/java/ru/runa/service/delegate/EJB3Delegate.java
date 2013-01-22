package ru.runa.service.delegate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public abstract class EJB3Delegate {
    public static final String EJB_REMOTE = "remote";
    private static InitialContext initialContext;
    private static Map<String, Object> services = new HashMap<String, Object>();
    private String ejbType;
    private String ejbJndiNameFormat;
    private final String beanName;
    private final String localInterfaceClassName;
    private final String remoteInterfaceClassName;

    /**
     * Creates delegate only for remote usage.
     * 
     * @param beanName
     *            EJB bean name
     * @param remoteInterfaceClass
     *            EJB @Remote class
     */
    public EJB3Delegate(String beanName, Class<?> remoteInterfaceClass) {
        this.beanName = beanName;
        localInterfaceClassName = null;
        remoteInterfaceClassName = remoteInterfaceClass.getName();
        setEjbType(EJB_REMOTE);
    }

    /**
     * Creates delegate based on base interface class (implicit assumptions
     * about @Local, @Remote interface and EJB bean naming)
     * 
     * @param baseInterfaceClass
     */
    public EJB3Delegate(Class<?> baseInterfaceClass) {
        beanName = baseInterfaceClass.getSimpleName() + "Bean";
        localInterfaceClassName = baseInterfaceClass.getName() + "Local";
        remoteInterfaceClassName = baseInterfaceClass.getName() + "Remote";
    }

    public void setEjbType(String ejbType) {
        this.ejbType = ejbType;
    }

    public void setEjbJndiNameFormat(String ejbJndiNameFormat) {
        this.ejbJndiNameFormat = ejbJndiNameFormat;
    }

    protected String getCustomProviderUrl() {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getService() {
        if (!services.containsKey(beanName)) {
            Map<String, String> variables = Maps.newHashMap();
            variables.put("bean.name", beanName);
            variables.put("ejb.type", ejbType);
            String interfaceClassName = EJB_REMOTE.equals(ejbType) ? remoteInterfaceClassName : localInterfaceClassName;
            variables.put("interface.class.name", interfaceClassName);
            String jndiName = ExpressionEvaluator.substitute(ejbJndiNameFormat, variables);
            try {
                Object service = getInitialContext().lookup(jndiName);
                services.put(beanName, service);
            } catch (NamingException e) {
                throw new InternalApplicationException("Unable to locate bean by jndi name '" + jndiName + "'", e);
            }
        }
        return (T) services.get(beanName);
    }

    private InitialContext getInitialContext() {
        if (initialContext == null) {
            try {
                Properties env = new Properties();
                InputStream is = ClassLoaderUtil.getAsStream("jndi.properties", getClass());
                if (is != null) {
                    Preconditions.checkNotNull(is, "jndi.properties is not in classpath");
                    env.load(is);
                }
                String customProviderUrl = getCustomProviderUrl();
                if (!Strings.isNullOrEmpty(customProviderUrl)) {
                    // TODO check format without protocol (remote://, jnp://)
                    env.put(Context.PROVIDER_URL, customProviderUrl);
                }
                initialContext = new InitialContext(env);
            } catch (Exception e) {
                throw new InternalApplicationException(e);
            }
        }
        return initialContext;
    }

}
