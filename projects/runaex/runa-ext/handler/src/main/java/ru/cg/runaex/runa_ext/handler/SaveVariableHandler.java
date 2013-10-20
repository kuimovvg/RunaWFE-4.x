package ru.cg.runaex.runa_ext.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.runa_ext.handler.dao.TaskHandlerDatabaseDaoHelper;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfiguration;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfigurationHelper;

/**
 * @author Петров А.
 */
public class SaveVariableHandler implements ActionHandler {


  private DbVariableHandlerConfiguration configuration;

  @Override
  public void setConfiguration(String configuration) {
    this.configuration = DbVariableHandlerConfigurationHelper.parseConfiguration(configuration);
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Exception {
    Long processInstanceId = executionContext.getProcess().getId();
    Long processDefinitionId = executionContext.getProcessDefinition().getId();

    if (configuration.getParameters() == null || configuration.getParameters().isEmpty()) {
      return;
    }

    Map<String, String> variableToColumnMapping = DbVariableHandlerConfigurationHelper.asVariableToColumnMapping(configuration);

    List<Data> dataList = new ArrayList<Data>(variableToColumnMapping.size());

    for (Map.Entry<String, String> entry : variableToColumnMapping.entrySet()) {
      Object variableValue = executionContext.getVariable(entry.getKey());
      Data data = new Data();
      data.setField(entry.getValue());
      data.setValue(variableValue);
      dataList.add(data);
    }

    TaskHandlerDatabaseDaoHelper.saveResultsToDatabase(processInstanceId, processDefinitionId, dataList);
  }
}
