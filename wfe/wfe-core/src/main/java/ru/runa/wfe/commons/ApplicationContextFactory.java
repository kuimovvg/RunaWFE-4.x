package ru.runa.wfe.commons;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import ru.runa.wfe.commons.calendar.BusinessCalendar;
import ru.runa.wfe.commons.hibernate.Converters;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Throwables;

public class ApplicationContextFactory {
    private static ApplicationContext applicationContext = null;
    private static DBType dbType;

    public static boolean isContextInitialized() {
        return applicationContext != null;
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

    public static JobDAO getJobDAO() {
        return getContext().getBean(JobDAO.class);
    }

    public static TaskDAO getTaskDAO() {
        return getContext().getBean(TaskDAO.class);
    }

    public static Converters getConverters() {
        return getContext().getBean(Converters.class);
    }

    public static BusinessCalendar getBusinessCalendar() {
        return getContext().getBean(BusinessCalendar.class);
    }

    public static IProcessDefinitionLoader getProcessDefinitionLoader() {
        return getContext().getBean(IProcessDefinitionLoader.class);
    }

    // TODO avoid static methods, inject
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

    public static DataSource getDataSource() throws NamingException {
        String dsName = getConfiguration().getProperty("hibernate.connection.datasource");
        return (DataSource) new InitialContext().lookup(dsName);
    }

    // TODO Environment

    public static Dialect getDialect() {
        return Dialect.getDialect(getConfiguration().getProperties());
    }

    public static DBType getDBType() {
        if (dbType == null) {
            String hibernateDialect = getConfiguration().getProperty("hibernate.dialect");
            if (hibernateDialect.contains("HSQL")) {
                dbType = DBType.HSQL;
            } else if (hibernateDialect.contains("Oracle")) {
                dbType = DBType.ORACLE;
            } else if (hibernateDialect.contains("Postgre")) {
                dbType = DBType.POSTGRESQL;
            } else if (hibernateDialect.contains("MySQL")) {
                dbType = DBType.MYSQL;
            } else if (hibernateDialect.contains("SQLServer")) {
                dbType = DBType.MSSQL;
            } else {
                dbType = DBType.GENERIC;
            }
        }
        return dbType;
    }

    public static ExecutorDAO getExecutorDAO() {
        return getContext().getBean(ExecutorDAO.class);
    }

    public static DeploymentDAO getDeploymentDAO() {
        return getContext().getBean(DeploymentDAO.class);
    }

    public static PermissionDAO getPermissionDAO() {
        return getContext().getBean(PermissionDAO.class);
    }

    public static RelationDAO getRelationDAO() {
        return getContext().getBean(RelationDAO.class);
    }

    public static RelationPairDAO getRelationPairDAO() {
        return getContext().getBean(RelationPairDAO.class);
    }

    public static SubstitutionDAO getSubstitutionDAO() {
        return getContext().getBean(SubstitutionDAO.class);
    }

    public static AssignmentHelper getAssignmentHelper() {
        return getContext().getBean(AssignmentHelper.class);
    }

    public static <T extends Object> T createAutowiredBean(String className) {
        return (T) createAutowiredBean(ClassLoaderUtil.loadClass(className));
    }

    public static <T extends Object> T createAutowiredBean(Class<T> clazz) {
        try {
            T object = clazz.newInstance();
            autowireBean(object);
            return object;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T extends Object> T autowireBean(T bean) {
        getContext().getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
    }
}
