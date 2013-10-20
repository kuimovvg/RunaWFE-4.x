package ru.cg.runaex.web.factory;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.web.context.support.XmlWebApplicationContext;

import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.factory.TransactionManagerFactory;
import ru.cg.runaex.web.utils.TransactionManagerUtil;

public class TransactionManagerFactoryImpl implements TransactionManagerFactory {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private MetadataDao metadataDao;

  public TransactionManagerFactoryImpl() {
  }

  @Override
  public DataSourceTransactionManager createTransactionManager(Long processDefinitionId) {
    String dbConnectionJndiName = metadataDao.getDbConnectionJndiName(processDefinitionId);
    String beanName = TransactionManagerUtil.getTransactionManagerBeanName(dbConnectionJndiName);
    JndiDataSourceLookup lookup = (JndiDataSourceLookup) context.getBean("jndiDataSourceLookup");
    DataSource dataSource = lookup.getDataSource(dbConnectionJndiName);
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    ConfigurableListableBeanFactory beanFactory = ((XmlWebApplicationContext) context).getBeanFactory();
    beanFactory.registerSingleton(beanName, transactionManager);
    return transactionManager;
  }
}