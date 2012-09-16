package ru.runa.commons;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import ru.runa.bpm.calendar.BusinessCalendar;
import ru.runa.bpm.context.VariableInstanceCreator;
import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.db.hibernate.Converters;
import ru.runa.bpm.graph.node.SubProcessResolver;

public class ApplicationContextFactory {
    private static ApplicationContext applicationContext = null;

    public static boolean isContextInitialized() {
        return applicationContext != null;
    }

    public static void init(ApplicationContext applicationContext) {
        ApplicationContextFactory.applicationContext = applicationContext;
    }

    public static ApplicationContext getContext() {
        if (!isContextInitialized()) {
            BeanFactoryReference ref = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(null);
            applicationContext = (ApplicationContext) ref.getFactory();
            if (applicationContext == null) {
                throw new RuntimeException("Context is not initialized");
            }
        }
        return applicationContext;
    }

    // TODO may be inject
    public static JobDAO getJobSession() {
        return getContext().getBean(JobDAO.class);
    }

    public static VariableInstanceCreator getVariableInstanceCreator() {
        return getContext().getBean(VariableInstanceCreator.class);
    }

    public static Converters getConverters() {
        return getContext().getBean(Converters.class);
    }

    public static BusinessCalendar getBusinessCalendar() {
        return getContext().getBean(BusinessCalendar.class);
    }

    public static SubProcessResolver getSubProcessResolver() {
        return getContext().getBean(SubProcessResolver.class);
    }

    // TODO avoid this
    public static SessionFactory getSessionFactory() {
        return getContext().getBean(SessionFactory.class);
    }

    public static Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public static Configuration getConfiguration() {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) getContext().getBean("&sessionFactory");
        return factoryBean.getConfiguration();
    }

    public static Dialect getDialect() {
        return getConfiguration().buildSettings().getDialect();
    }

    public static String getDialectClassName() {
        return getConfiguration().getProperty("hibernate.dialect");
    }
}
