package ru.cg.runaex.database.dao.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.stereotype.Component;

import ru.cg.runaex.database.cache.DbConnectionJndiNameCache;
import ru.cg.runaex.database.provider.TxDataSourceProxyProvider;

/**
 * @author urmancheev
 */
@Component
public class JdbcTemplateProviderJndiImpl implements JdbcTemplateProvider {
  private static Map<Long, JdbcTemplate> templatesByProcesses = new ConcurrentHashMap<Long, JdbcTemplate>();

  private JndiDataSourceLookup jndiDataSourceLookup;

  @Autowired
  private TxDataSourceProxyProvider txDataSourceProxyProvider;

  private JdbcTemplate defaultJdbcTemplate;

  @Autowired
  @Qualifier("metadataJdbcTemplate")
  private JdbcTemplate metadataJdbcTemplate;

  public void setDefaultDataSource(DataSource defaultDataSource) {
    this.defaultJdbcTemplate = new JdbcTemplate(defaultDataSource);
  }

  public void setMetadataDataSource(DataSource metadataDataSource) {
//    this.metadataJdbcTemplate = new JdbcTemplate(metadataDataSource);
  }

  public void setJndiDataSourceLookup(JndiDataSourceLookup jndiDataSourceLookup) {
    this.jndiDataSourceLookup = jndiDataSourceLookup;
  }

  @Override
  public JdbcTemplate getMetadataTemplate() {
    return getTemplate(null);
  }

  @Override
  public JdbcTemplate getTemplate(Long processDefinitionId) {
    if (processDefinitionId == null)
      return metadataJdbcTemplate;

    JdbcTemplate template = templatesByProcesses.get(processDefinitionId);
    if (template == null) {
      template = initTemplate(processDefinitionId);
    }
    return template;
  }

  private JdbcTemplate initTemplate(Long processDefinitionId) {
    String dataSourceJndiName = DbConnectionJndiNameCache.getDbConnectionJndiName(processDefinitionId);
    JdbcTemplate jdbcTemplate;

    if (dataSourceJndiName != null) {
      TransactionAwareDataSourceProxy txDataSourceProxy = txDataSourceProxyProvider.getTxDataSourceProxy(dataSourceJndiName);
      jdbcTemplate = new JdbcTemplate(txDataSourceProxy);
    }
    else {
      jdbcTemplate = defaultJdbcTemplate;
    }
    templatesByProcesses.put(processDefinitionId, jdbcTemplate);

    return jdbcTemplate;
  }
}
