package ru.cg.runaex.web.job;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ru.cg.runaex.database.dao.MetadataDao;

/**
 * @author Kochetkov
 */
public class CleanGroovyScriptTableJob {
  private static final Logger logger = LoggerFactory.getLogger(CleanGroovyScriptTableJob.class);

  @Autowired
  private MetadataDao metadataDao;

  @Autowired
  @Qualifier("groovyScriptCleanerJobProperties")
  private Properties groovyScriptCleanerJobProperties;

  public void doWork() {
    try {
      String expiryDate = groovyScriptCleanerJobProperties.getProperty("expiryDate");
      metadataDao.deleteExpiredGroovyScripts(expiryDate);
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }
}
