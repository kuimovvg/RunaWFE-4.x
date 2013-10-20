package ru.cg.runaex.runa_ext.handler;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.extension.ActionHandler;

/**
 * @author Kochetkov
 */
public class CompleteProcessTasksHandler implements ActionHandler {
  @Override
  public void execute(ExecutionContext executionContext) {
    Process process = executionContext.getProcess();
    Token rootToken = process.getRootToken();
    while (rootToken != null) {
      if (!process.equals(rootToken.getProcess())) {
        process = rootToken.getProcess();
        rootToken = process.getRootToken();
      }
      else {
        break;
      }
    }
    process.end(executionContext, null);
  }

  @Override
  public void setConfiguration(String s) {

  }
}