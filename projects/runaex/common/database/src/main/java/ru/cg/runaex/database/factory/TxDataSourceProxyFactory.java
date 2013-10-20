package ru.cg.runaex.database.factory;

import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

/**
 * @author Kochetkov
 */
public interface TxDataSourceProxyFactory {
  TransactionAwareDataSourceProxy createTxDataSourceProxy(Long processDefinitionId);

  TransactionAwareDataSourceProxy createTxDataSourceProxy(String dbConnectionJndiName);
}
