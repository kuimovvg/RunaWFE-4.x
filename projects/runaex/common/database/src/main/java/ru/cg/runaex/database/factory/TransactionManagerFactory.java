package ru.cg.runaex.database.factory;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * @author Kochetkov
 */
public interface TransactionManagerFactory {
  public DataSourceTransactionManager createTransactionManager(Long processDefinitionId);
}
