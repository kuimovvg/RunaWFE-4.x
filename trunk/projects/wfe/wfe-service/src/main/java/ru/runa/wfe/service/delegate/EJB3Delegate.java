package ru.runa.wfe.service.delegate;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public abstract class EJB3Delegate {
    public static final String EJB_REMOTE = "remote";
    private static final String EJB_LOCAL = "";
    private static Map<String, InitialContext> initialContexts = Maps.newHashMap();
    private static Map<String, Map<String, Object>> services = Maps.newHashMap();
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
        localInterfaceClassName = "ru.runa.wfe.service.decl." + baseInterfaceClass.getSimpleName() + "Local";
        remoteInterfaceClassName = "ru.runa.wfe.service.decl." + baseInterfaceClass.getSimpleName() + "Remote";
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
        String providerUrl = Objects.firstNonNull(getCustomProviderUrl(), EJB_LOCAL);
        Map<String, Object> providerServices = services.get(providerUrl);
        if (providerServices == null) {
            providerServices = Maps.newHashMap();
            providerServices.put(providerUrl, providerServices);
        }
        if (!providerServices.containsKey(beanName)) {
            Map<String, String> variables = Maps.newHashMap();
            variables.put("bean.name", beanName);
            variables.put("ejb.type", ejbType);
            String interfaceClassName = EJB_REMOTE.equals(ejbType) ? remoteInterfaceClassName : localInterfaceClassName;
            variables.put("interface.class.name", interfaceClassName);
            String jndiName = ExpressionEvaluator.substitute(ejbJndiNameFormat, variables);
            try {
                Object service = getInitialContext().lookup(jndiName);
                providerServices.put(beanName, service);
            } catch (NamingException e) {
                throw new InternalApplicationException("Unable to locate bean by jndi name '" + jndiName + "'", e);
            }
        }
        return (T) providerServices.get(beanName);
    }

    private InitialContext getInitialContext() {
        String providerUrl = Objects.firstNonNull(getCustomProviderUrl(), EJB_LOCAL);
        if (!initialContexts.containsKey(providerUrl)) {
            try {
                Properties env = new Properties();
                InputStream is = ClassLoaderUtil.getAsStream("jndi.properties", getClass());
                if (is != null) {
                    Preconditions.checkNotNull(is, "jndi.properties is not in classpath");
                    env.load(is);
                }
                if (!Objects.equal(EJB_LOCAL, providerUrl)) {
                    env.put(Context.PROVIDER_URL, providerUrl);
                }
                initialContexts.put(providerUrl, new InitialContext(env));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return initialContexts.get(providerUrl);
    }

}
