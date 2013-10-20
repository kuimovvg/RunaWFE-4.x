package ru.cg.runaex.database.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import ru.cg.runaex.database.cache.DbConnectionJndiNameCache;
import ru.cg.runaex.database.provider.TxDataSourceProxyProvider;
import ru.cg.runaex.database.util.TransactionManagerUtil;

public class TransactionManagerFactoryImpl implements TransactionManagerFactory {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private TxDataSourceProxyProvider txDataSourceProxyProvider;

  public TransactionManagerFactoryImpl() {
  }

  @Override
  public synchronized DataSourceTransactionManager createTransactionManager(Long processDefinitionId) {
    DataSourceTransactionManager transactionManager;
    String dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(processDefinitionId);
    String beanName = TransactionManagerUtil.getTransactionManagerBeanName(dbConnectionJndiName);

    if (!context.containsBean(beanName)) {
      ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
      TransactionAwareDataSourceProxy dataSource = txDataSourceProxyProvider.getTxDataSourceProxy(dbConnectionJndiName);
      transactionManager = new DataSourceTransactionManager(dataSource);
      beanFactory.registerSingleton(beanName, transactionManager);
    }
    else {
      transactionManager = (DataSourceTransactionManager) context.getBean(beanName);
    }

    return transactionManager;
  }
}