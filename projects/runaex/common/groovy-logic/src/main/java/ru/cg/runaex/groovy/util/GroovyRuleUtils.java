package ru.cg.runaex.groovy.util;

import java.util.Map;

import groovy.lang.Binding;

import ru.cg.runaex.components.exception.InvalidGroovyResultException;
import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;

/**
 * @author Петров А.
 */
public final class GroovyRuleUtils {

  private static Object executeGroovyScript(String groovyScript, Map<String, Object> variables, String projectName, Long processDefinitionId, Long processId) {
    GroovyScriptExecutor executor = GroovySpringContext.getGroovyExecutorCache().getExecutor(projectName, processDefinitionId);
    Binding binding = new Binding(variables);
    return executor.executeScriptWithResult(groovyScript, binding, processId);
  }

  public static boolean executeGroovyRule(String groovyRule, Map<String, Object> variables, String projectName, Long processDefinitionId, Long processId) {
    Object objResult = executeGroovyScript(groovyRule, variables, projectName, processDefinitionId, processId);

    if (objResult != null && objResult instanceof Boolean) {
      return (Boolean) objResult;
    }

    throw new InvalidGroovyResultException();
  }
}
