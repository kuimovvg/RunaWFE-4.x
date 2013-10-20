package ru.cg.runaex.database.util;

import ru.cg.runaex.database.cache.DbConnectionJndiNameCache;

/**
 * @author Kochetkov
 */
public class TxDataSourceProxyUtil {

  private static final String TX_DATASOURCE_PROXY_POSTFIX = "_TransactionProxy";

  public static String getTxDataSourceProxyBeanName(Long processDefinitionId) {
    String dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(processDefinitionId);
    return getTxDataSourceProxyBeanName(dbConnectionJndiName);
  }

  public static String getTxDataSourceProxyBeanName(String dbConnectionJndiName) {
    if (dbConnectionJndiName == null) {
      dbConnectionJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(null);
    }

    return dbConnectionJndiName + TX_DATASOURCE_PROXY_POSTFIX;
  }
}
