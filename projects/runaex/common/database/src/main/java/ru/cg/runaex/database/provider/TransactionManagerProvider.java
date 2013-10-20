package ru.cg.runaex.database.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import ru.cg.runaex.database.factory.TransactionManagerFactory;
import ru.cg.runaex.database.util.TransactionManagerUtil;

/**
 * @author Kochetkov
 */
public class TransactionManagerProvider {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private TransactionManagerFactory transactionManagerFactory;

  public DataSourceTransactionManager getTransactionManager(Long processDefinitionId) {
    String beanName = TransactionManagerUtil.getTransactionManagerBeanName(processDefinitionId);
    DataSourceTransactionManager transactionManager;
    if (context.containsBean(beanName)) {
      transactionManager = (DataSourceTransactionManager) context.getBean(beanName);
    }
    else {
      transactionManager = transactionManagerFactory.createTransactionManager(processDefinitionId);
    }
    return transactionManager;
  }
}
