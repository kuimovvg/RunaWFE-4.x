package ru.cg.runaex.database.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.delegate.ExecutionServiceDelegate;

import ru.cg.fias.search.core.server.bean.AddressSphinx;
import ru.cg.fias.search.core.server.component.reader.SphinxBaseReader;
import ru.cg.fias.search.core.server.component.reader.SphinxReader;
import ru.cg.fias.search.core.server.datasource.SphinxDataSource;
import ru.cg.runaex.database.bean.DependencyAdapter;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.ComponentDbServices;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.dao.SaveDao;
import ru.cg.runaex.database.provider.TxDataSourceProxyProvider;
import ru.cg.runaex.database.service.RunaService;
import ru.cg.runaex.database.service.SphinxSearchService;
import ru.cg.runaex.database.service.UpdateDbService;
import ru.cg.runaex.database.service.UserDetailsService;
import ru.cg.runaex.database.util.SphinxSearchUtil;

/**
 * Provides access to Spring application context and beans
 *
 * @author Петров А.
 */
public final class DatabaseSpringContext implements ApplicationContextAware {

  private static final Object lock = new Object();
  private static ApplicationContext context;

  private static SphinxDataSource sphinxToolsSphinxDataSource;
  private static SphinxSearchUtil sphinxSearchUtil;
//  private static UrlSource esbHostSource;

  private static ExecutionServiceDelegate executionServiceDelegate;

  public static DependencyAdapter getChildrenSumDependencyAdapter() {
    return (DependencyAdapter) context.getBean("childrenSumDependencyAdapter");
  }


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static ComponentDbServices getComponentDbServices() {
    return (ComponentDbServices) context.getBean("componentDbServices");
  }

  public static UserDetailsService getUserDetailsService() {
    return (UserDetailsService) context.getBean("userDetailsService");
  }

  public static UpdateDbService getUpdateDbService() {
    return (UpdateDbService) context.getBean("updateDbService");
  }

  public static RunaService getRunaService() {
    return (RunaService) context.getBean("runaService");
  }

  public static SphinxReader<AddressSphinx> getAddressReader() {
    return (SphinxReader<AddressSphinx>) context.getBean("sphinxAddressReader");
  }

  public static SphinxReader<AddressSphinx> getSphinxStrAddressByGuidReader() {
    return (SphinxReader<AddressSphinx>) context.getBean("sphinxStrAddressByGuidReader");
  }

  public static BaseDao getBaseDao() {
    return (BaseDao) context.getBean("baseDaoService");
  }

  public static SaveDao getSaveDao() {
    return (SaveDao) context.getBean("saveDao");
  }

  public static MetadataDao getMetadataDao() {
    return (MetadataDao) context.getBean("metadataDao");
  }

  public static SphinxBaseReader createSphinxBaseReader(String indexName) {
    SphinxBaseReader reader = new SphinxBaseReader();
    if (sphinxToolsSphinxDataSource == null)
      sphinxToolsSphinxDataSource = (SphinxDataSource) context.getBean("sphinxToolsSphinxDataSource");
    reader.setDataSource(sphinxToolsSphinxDataSource);
    reader.setIndex(indexName);
    return reader;
  }

  public static SphinxSearchUtil getSphinxSearchUtil() {
    if (sphinxSearchUtil == null) {
      sphinxSearchUtil = (SphinxSearchUtil) context.getBean("sphinxSearchUtil");
    }
    return sphinxSearchUtil;
  }

  public static SphinxSearchService getSphinxSearchService() {
    return (SphinxSearchService) context.getBean("sphinxSearchService");
  }

  public static ExecutionServiceDelegate getExecutionServiceDelegate() {
    if (executionServiceDelegate == null) {
      executionServiceDelegate = (ExecutionServiceDelegate) Delegates.getExecutionService();
    }
    return executionServiceDelegate;
  }

//  public static UrlSource getEsbHostSource() {
//    ensureContextInitialized();
//    if (esbHostSource == null)
//      esbHostSource = (UrlSource) context.getBean("esbHostSource");
//    return esbHostSource;
//  }

  public static TxDataSourceProxyProvider getTxDataSourceProxyProvider() {
    return (TxDataSourceProxyProvider) context.getBean("txDataSourceProxyProvider");
  }

  public static String getDefaultDataSourceJndiName() {
    return ((String) context.getBean("defaultDataSourceJndiName"));
  }
}
