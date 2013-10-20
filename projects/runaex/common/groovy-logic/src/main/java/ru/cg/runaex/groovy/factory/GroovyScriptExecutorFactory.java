package ru.cg.runaex.groovy.factory;

import java.io.IOException;

import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;

/**
 * @author Петров А.
 */
public interface GroovyScriptExecutorFactory {

  GroovyScriptExecutor create(String predefinedProjectFunctionsScript, String projectName, Long processDefinitionId) throws IOException, ClassNotFoundException;
}
