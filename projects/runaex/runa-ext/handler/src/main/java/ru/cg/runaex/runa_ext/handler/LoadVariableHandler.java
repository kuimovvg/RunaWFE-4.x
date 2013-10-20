package ru.cg.runaex.runa_ext.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.runa_ext.handler.dao.TaskHandlerDatabaseDaoHelper;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfiguration;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfigurationHelper;

/**
 * @author Петров А.
 */
public class LoadVariableHandler implements ActionHandler {
  protected static Logger log = LoggerFactory.getLogger(LoadVariableHandler.class);

  private DbVariableHandlerConfiguration configuration;

  @Override
  public void setConfiguration(String configuration) {
    this.configuration = DbVariableHandlerConfigurationHelper.parseConfiguration(configuration);
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Exception {
    Long processInstanceId = executionContext.getProcess().getId();
    Long processDefinitionId = executionContext.getProcessDefinition().getId();

    if (configuration.getParameters().isEmpty()) {
      log.warn("Configuration is empty. Nothing was done.");
      return;
    }

    Map<String, String> columnToVariableMapping = DbVariableHandlerConfigurationHelper.asColumnToVariableMapping(configuration);

    TransportData dataList = TaskHandlerDatabaseDaoHelper.loadData(processInstanceId, processDefinitionId, columnToVariableMapping.keySet());

    for (Data data : dataList.getData()) {
      executionContext.setVariable(columnToVariableMapping.get(data.getField()), data.getValue());
    }
  }
}