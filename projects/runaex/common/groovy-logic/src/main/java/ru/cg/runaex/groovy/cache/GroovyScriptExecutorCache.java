package ru.cg.runaex.groovy.cache;

import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;

/**
 * @author Петров А.
 */
public interface GroovyScriptExecutorCache {

  GroovyScriptExecutor getExecutor(String projectName, Long processDefinitionId);

  void removeExecutor(String projectName);
}
