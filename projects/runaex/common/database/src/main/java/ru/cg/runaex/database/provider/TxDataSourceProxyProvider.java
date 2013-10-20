package ru.cg.runaex.database.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import ru.cg.runaex.database.factory.TxDataSourceProxyFactory;
import ru.cg.runaex.database.util.TxDataSourceProxyUtil;

/**
 * @author Kochetkov
 */
public class TxDataSourceProxyProvider {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private TxDataSourceProxyFactory txDataSourceProxyFactory;

  public TransactionAwareDataSourceProxy getTxDataSourceProxy(Long processDefinitionId) {
    String beanName = TxDataSourceProxyUtil.getTxDataSourceProxyBeanName(processDefinitionId);
    TransactionAwareDataSourceProxy dataSourceProxy;
    if (context.containsBean(beanName)) {
      dataSourceProxy = (TransactionAwareDataSourceProxy) context.getBean(beanName);
    }
    else {
      dataSourceProxy = txDataSourceProxyFactory.createTxDataSourceProxy(processDefinitionId);
    }
    return dataSourceProxy;
  }

  public TransactionAwareDataSourceProxy getTxDataSourceProxy(String dbConnectionJndiName) {
    String beanName = TxDataSourceProxyUtil.getTxDataSourceProxyBeanName(dbConnectionJndiName);
    TransactionAwareDataSourceProxy dataSourceProxy;
    if (context.containsBean(beanName)) {
      dataSourceProxy = (TransactionAwareDataSourceProxy) context.getBean(beanName);
    }
    else {
      dataSourceProxy = txDataSourceProxyFactory.createTxDataSourceProxy(dbConnectionJndiName);
    }
    return dataSourceProxy;
  }
}
