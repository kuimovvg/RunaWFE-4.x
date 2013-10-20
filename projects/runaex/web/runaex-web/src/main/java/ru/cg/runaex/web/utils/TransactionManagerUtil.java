package ru.cg.runaex.web.utils;

import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author Kochetkov
 */
public class TransactionManagerUtil {

  private static final String TRANSACTION_MANAGER_NAME_POSTFIX = "_TransactionManager";

  public static String getTransactionManagerBeanName(Long processDefinitionId) {
    if (processDefinitionId == null) {
      return null;
    }
    String dbConnectionJndiName = DatabaseSpringContext.getMetadataDao().getDbConnectionJndiName(processDefinitionId);
    return getTransactionManagerBeanName(dbConnectionJndiName);
  }

  public static String getTransactionManagerBeanName(String dbConnectionJndiName) {
    if (dbConnectionJndiName == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(dbConnectionJndiName);
    sb.append(TRANSACTION_MANAGER_NAME_POSTFIX);

    return sb.toString();
  }
}
