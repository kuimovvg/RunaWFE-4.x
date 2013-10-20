package ru.cg.runaex.groovy.cache;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;
import ru.cg.runaex.groovy.factory.GroovyScriptExecutorFactory;

/**
 * @author Петров А.
 */
@Component
public class GroovyScriptExecutorCacheImpl implements GroovyScriptExecutorCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptExecutorCacheImpl.class);

  private final Map<String, GroovyScriptExecutor> cache = new ConcurrentHashMap<String, GroovyScriptExecutor>();

  @Autowired
  private MetadataDao metadataDao;

  @Autowired
  private GroovyScriptExecutorFactory groovyScriptExecutorFactory;

  @Override
  public GroovyScriptExecutor getExecutor(String projectName, Long processDefinitionId) {
    //TODO Костыль. Сделать очиску кеша через RMI
    cache.clear();

    GroovyScriptExecutor executor = cache.get(projectName);

    if (executor == null) {
      executor = loadExecutor(projectName, processDefinitionId);
    }

    return executor;
  }

  @Override
  public void removeExecutor(String projectName) {
    cache.remove(projectName);
  }

  private GroovyScriptExecutor loadExecutor(String projectName, Long processDefinitionId) {
    GroovyScriptExecutor executor = createExecutor(projectName, processDefinitionId);

    cache.put(projectName, executor);

    return executor;
  }

  private GroovyScriptExecutor createExecutor(String projectName, Long processDefinitionId) {
    String predefinedFunctionsScript = metadataDao.loadProjectPredefinedGroovyFunctions(projectName);

    GroovyScriptExecutor executor = null;
    try {
      executor = groovyScriptExecutorFactory.create(predefinedFunctionsScript, projectName.replaceAll("\\s", ""), processDefinitionId);
    }
    catch (IOException ex) {
      LOGGER.error(ex.toString(), ex);
    }
    catch (ClassNotFoundException ex) {
      LOGGER.error(ex.toString(), ex);
    }

    return executor;
  }
}
