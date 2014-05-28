package ru.runa.wfe.service.delegate;

import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public abstract class EJB3Delegate {
    private final static Log log = LogFactory.getLog(EJB3Delegate.class);
    private static final PropertyResources EJB_PROPERTIES = new PropertyResources("ejb.properties");
    public static final String EJB_REMOTE = "remote";
    private static final String EJB_LOCAL = "";
    private static final String WFE_SERVICE_JAR_NAME = "wfe-service";
    private static Map<String, InitialContext> initialContexts = Maps.newHashMap();
    private static Map<String, Map<String, Object>> services = Maps.newHashMap();
    private static boolean useJbossEjbClientForRemoting = EJB_PROPERTIES.getBooleanProperty("jboss.ejbclient.enabled", false);
    private final String ejbType;
    private final String ejbJndiNameFormat = EJB_PROPERTIES.getStringPropertyNotNull("ejb.jndiName.format");
    private final String jarName;
    private final String beanName;
    private final String localInterfaceClassName;
    private final String remoteInterfaceClassName;
    private String customProviderUrl;

    /**
     * Creates delegate only for remote usage.
     * 
     * @param beanName
     *            EJB bean name
     * @param remoteInterfaceClass
     *            EJB @Remote class
     */
    public EJB3Delegate(String beanName, Class<?> remoteInterfaceClass, String jarName) {
        this.beanName = beanName;
        localInterfaceClassName = null;
        remoteInterfaceClassName = remoteInterfaceClass.getName();
        this.jarName = jarName;
        this.ejbType = EJB_REMOTE;
    }

    /**
     * Creates delegate only for remote usage.
     * 
     * @param beanName
     *            EJB bean name
     * @param remoteInterfaceClass
     *            EJB @Remote class
     */
    public EJB3Delegate(String beanName, Class<?> remoteInterfaceClass) {
        this(beanName, remoteInterfaceClass, WFE_SERVICE_JAR_NAME);
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
        jarName = WFE_SERVICE_JAR_NAME;
        this.ejbType = EJB_PROPERTIES.getStringPropertyNotNull("ejb.type");
    }

    protected String getCustomProviderUrl() {
        return customProviderUrl;
    }

    public void setCustomProviderUrl(String customProviderUrl) {
        this.customProviderUrl = customProviderUrl;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getService() {
        String providerUrl = Objects.firstNonNull(getCustomProviderUrl(), EJB_LOCAL);
        Map<String, Object> providerServices = services.get(providerUrl);
        if (providerServices == null) {
            providerServices = Maps.newHashMap();
            services.put(providerUrl, providerServices);
        }
        if (!providerServices.containsKey(beanName)) {
            Map<String, String> variables = Maps.newHashMap();
            variables.put("jar.name", jarName);
            variables.put("jar.version", SystemProperties.getVersion());
            variables.put("bean.name", beanName);
            variables.put("ejb.type", ejbType);
            String interfaceClassName = EJB_REMOTE.equals(ejbType) ? remoteInterfaceClassName : localInterfaceClassName;
            variables.put("interface.class.name", interfaceClassName);
            String jndiName;
            if (!Strings.isNullOrEmpty(providerUrl) && useJbossEjbClientForRemoting) {
                String ejbclientJndiNameFormat = EJB_PROPERTIES.getStringPropertyNotNull("jboss.ejbclient.url.format");
                jndiName = ExpressionEvaluator.substitute(ejbclientJndiNameFormat, variables);
            } else {
                jndiName = ExpressionEvaluator.substitute(ejbJndiNameFormat, variables);
            }
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
                Properties properties;
                if (!Objects.equal(EJB_LOCAL, providerUrl) || EJB_PROPERTIES.getBooleanProperty("jboss.ejbclient.static.enabled", false)) {
                    properties = ClassLoaderUtil.getProperties("jndi.properties", false);
                    if (useJbossEjbClientForRemoting) {
                        String port = EJB_PROPERTIES.getStringProperty("jboss.ejbclient.port", "4447");
                        String hostname;
                        if (providerUrl.contains(":")) {
                            int colonIndex = providerUrl.indexOf(":");
                            port = providerUrl.substring(colonIndex + 1);
                            hostname = providerUrl.substring(0, colonIndex);
                        } else {
                            hostname = providerUrl;
                        }
                        String name = "n_" + hostname;
                        properties.put("remote.connections", name);
                        properties.put("remote.connection." + name + ".host", hostname);
                        properties.put("remote.connection." + name + ".port", port);
                        properties
                                .put("remote.connection." + name + ".username", EJB_PROPERTIES.getStringPropertyNotNull("jboss.ejbclient.username"));
                        properties
                                .put("remote.connection." + name + ".password", EJB_PROPERTIES.getStringPropertyNotNull("jboss.ejbclient.password"));
                    } else {
                        properties.put(Context.PROVIDER_URL, providerUrl);
                    }
                    log.debug("Trying to obtain remote connection for '" + providerUrl + "' using " + properties);
                } else {
                    properties = new Properties();
                }
                initialContexts.put(providerUrl, new InitialContext(properties));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return initialContexts.get(providerUrl);
    }

    protected RuntimeException handleException(Exception e) {
        if (e instanceof EJBException && e.getCause() != null) {
            return Throwables.propagate(e.getCause());
        }
        return Throwables.propagate(e);
    }
}
