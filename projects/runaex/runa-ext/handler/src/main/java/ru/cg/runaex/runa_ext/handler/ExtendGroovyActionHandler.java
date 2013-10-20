package ru.cg.runaex.runa_ext.handler;

import java.util.Map;

import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.GroovyActionHandler;
import ru.runa.wfe.lang.ProcessDefinition;

import ru.cg.runaex.runa_ext.handler.groovy.ExtendGroovyScriptExecutor;

/**
 * @author Абдулин Ильдар
 */
public class ExtendGroovyActionHandler extends GroovyActionHandler {

  private String configuration;

  @Override
  public void setConfiguration(String configuration) {
    super.setConfiguration(configuration);
    this.configuration = configuration;
  }

  protected IScriptExecutor getScriptExecutor(ExecutionContext executionContext) {
    return new ExtendGroovyScriptExecutor(executionContext);
  }

  @Override
  public void execute(ExecutionContext executionContext) {
    ProcessDefinition processDefinition = executionContext.getProcessDefinition();
    Map<String, Object> outVariables = getScriptExecutor(executionContext).executeScript(processDefinition, executionContext.getVariableProvider(), configuration);
    executionContext.setVariables(outVariables);
  }

}
