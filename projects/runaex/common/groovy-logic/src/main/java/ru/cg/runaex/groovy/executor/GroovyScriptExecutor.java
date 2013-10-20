package ru.cg.runaex.groovy.executor;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author Петров А.
 */
public class GroovyScriptExecutor {

  protected static final Pattern TRANSACTIONAL_BLOCK_PATTERN = Pattern.compile("открыть транзакцию(.+?)закрыть транзакцию", Pattern.DOTALL);

  private CompilerConfiguration compilerConfiguration;

  public GroovyScriptExecutor(CompilerConfiguration compilerConfiguration) {
    this.compilerConfiguration = compilerConfiguration;
  }

  private GroovyShell getGroovyShell(Binding binding) {
    ClassLoader classLoader = GroovyScriptExecutor.this.getClass().getClassLoader();
    return new GroovyShell(classLoader, binding, compilerConfiguration);
  }

  public void executeScript(String script, Binding binding, Long processId) {
    addUtilityVariables(binding, processId);
    getGroovyShell(binding).evaluate(prepareExecutableScript(script));
  }

  public <T> T executeScriptWithResult(String script, Binding binding, Long processId) {
    addUtilityVariables(binding, processId);
    return (T) getGroovyShell(binding).evaluate(prepareExecutableScript(script));
  }

  protected void addUtilityVariables(Binding binding, Long processId) {
    //TODO Это должно быть в переменных руны.
    binding.setVariable("selectedRowId", getSelectedRowId(processId));
  }

  protected void removeUtilityVariables(Set variables, Binding binding) {
    Set removeVariables = new HashSet();
    for (Object variableKey : binding.getVariables().keySet()) {
      if (!variables.contains(variableKey)) {
        removeVariables.add(variableKey);
      }
    }
    for (Object removeVariableKey : removeVariables) {
      binding.getVariables().remove(removeVariableKey);
    }
  }

  private String prepareExecutableScript(String script) {
    Matcher transactionalBlockMatcher = TRANSACTIONAL_BLOCK_PATTERN.matcher(script);
    while (transactionalBlockMatcher.find()) {
      script = transactionalBlockMatcher.replaceFirst(MessageFormat.format(GroovyScriptExecutorStaticResources.TRANSACTION_BLOCK_TEMPLATE, transactionalBlockMatcher.group(1)));

      transactionalBlockMatcher = TRANSACTIONAL_BLOCK_PATTERN.matcher(script);
    }

    return MessageFormat.format(GroovyScriptExecutorStaticResources.GROOVY_SCRIPT_TEMPLATE, script);
  }

  protected Long getSelectedRowId(Long processId) {
    String jsonData = DatabaseSpringContext.getBaseDao().getVariableFromDb(processId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    return jsonData != null && !jsonData.isEmpty() ? new Gson().fromJson(jsonData, ObjectInfo.class).getId() : null;
  }
}
