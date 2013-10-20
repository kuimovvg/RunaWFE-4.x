package ru.cg.runaex.database.factory;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import ru.cg.runaex.database.cache.DbConnectionJndiNameCache;
import ru.cg.runaex.database.util.TxDataSourceProxyUtil;

/**
 * @author Kochetkov
 */
public class TxDataSourceProxyFactoryImpl implements TxDataSourceProxyFactory {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private JndiDataSourceLookup lookup;

  public TxDataSourceProxyFactoryImpl() {
  }

  @Override
  public synchronized TransactionAwareDataSourceProxy createTxDataSourceProxy(Long processDefinitionId) {
    String dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(processDefinitionId);
    return createTxDataSourceProxy(dbConnectionJndiName);
  }

  @Override
  public synchronized TransactionAwareDataSourceProxy createTxDataSourceProxy(String dbConnectionJndiName) {
    TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;
    String beanName = TxDataSourceProxyUtil.getTxDataSourceProxyBeanName(dbConnectionJndiName);

    if (!context.containsBean(beanName)) {
      ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
      DataSource dataSource = lookup.getDataSource(dbConnectionJndiName);
      transactionAwareDataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);
      beanFactory.registerSingleton(beanName, transactionAwareDataSourceProxy);
    }
    else {
      transactionAwareDataSourceProxy = (TransactionAwareDataSourceProxy) context.getBean(beanName);
    }
    return transactionAwareDataSourceProxy;
  }
}
