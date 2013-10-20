package ru.cg.runaex.database.util;

import ru.cg.runaex.database.cache.DbConnectionJndiNameCache;

/**
 * @author Kochetkov
 */
public class TransactionManagerUtil {

  private static final String TRANSACTION_MANAGER_NAME_POSTFIX = "_TransactionManager";

  public static String getTransactionManagerBeanName(Long processDefinitionId) {
    String dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(processDefinitionId);
    return getTransactionManagerBeanName(dbConnectionJndiName);
  }

  public static String getTransactionManagerBeanName(String dbConnectionJndiName) {
    if (dbConnectionJndiName == null) {
      dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(null);
    }

    return dbConnectionJndiName + TRANSACTION_MANAGER_NAME_POSTFIX;
  }
}
