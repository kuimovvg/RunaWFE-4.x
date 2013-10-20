package ru.cg.runaex.database.cache;

import java.util.concurrent.ConcurrentHashMap;

import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author Kochetkov
 */
public class DbConnectionJndiNameCache {
  private static final ConcurrentHashMap<Long, String> jndiNamesByProcesses = new ConcurrentHashMap<Long, String>();

  public static String getDbConnectionJndiName(Long processDefinitionId) {
    String jndiName = jndiNamesByProcesses.get(processDefinitionId);

    if (jndiName == null) {
      if (processDefinitionId == null) {
        jndiName = DatabaseSpringContext.getDefaultDataSourceJndiName();
        jndiNamesByProcesses.put(null, jndiName);
      }
      else {
        jndiName = DatabaseSpringContext.getMetadataDao().getDbConnectionJndiName(processDefinitionId);
        if (jndiName == null) {
          jndiName = DatabaseSpringContext.getDefaultDataSourceJndiName();
        }
        jndiNamesByProcesses.put(processDefinitionId, jndiName);
      }
    }

    return jndiName;
  }
}
