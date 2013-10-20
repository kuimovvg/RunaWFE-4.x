//package ru.cg.runaex.components.handler;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import javax.security.auth.Subject;
//import javax.xml.namespace.QName;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import ru.runa.af.AuthenticationException;
//import ru.runa.af.AuthorizationException;
//import ru.runa.wf.TaskDoesNotExistException;
//import ru.runa.wf.TaskStub;
//import ru.runa.wf.delegate.ExecutionServiceDelegate;
//import ru.runa.wf.logic.bot.TaskHandlerException;
//
//import ru.cg.runaex.database.bean.transport.Data;
//import ru.cg.runaex.database.bean.transport.TransportData;
//import ru.cg.runaex.components.bean.component.part.ColumnReference;
//import ru.cg.runaex.components.handler.dao.TaskHandlerDatabaseDaoHelper;
//import ru.cg.runaex.components.handler.start_remote_process.DbParameter;
//import ru.cg.runaex.components.handler.start_remote_process.StartRemoteProcessConfiguration;
//import ru.cg.runaex.components.handler.start_remote_process.StartRemoteProcessConfigurationParser;
//import ru.cg.runaex.components.handler.util.WebserviceHelper;
//import ru.cg.runaex.database.context.DatabaseSpringContext;
//import ru.cg.runaex.esb.bean.StartProcessInstanceRequest;
//import ru.cg.runaex.esb.bean.StartProcessInstanceResult;
//import ru.cg.runaex.esb.bean.Variable;
//import ru.cg.runaex.esb.client.StartProcessInstanceServiceClient;
//import ru.cg.runaex.esb.util.VariableHelper;
//
///**
// * @author urmancheev
// */
//public class StartRemoteProcessHandler extends TaskHandlerBase {
//  private final Logger logger = LoggerFactory.getLogger(getClass());
//
//  private StartRemoteProcessConfiguration configuration;
//
//  @Override
//  public void setConfiguration(String configurationStr) throws TaskHandlerException {
//    try {
//      configuration = StartRemoteProcessConfigurationParser.parse(configurationStr);
//    }
//    catch (IllegalArgumentException ex) {
//      String message = "Unparsable configuration " + configurationStr;
//      logger.error(message, ex);
//      throw new TaskHandlerException(message, ex);
//    }
//    catch (Exception ex) {
//      logger.error("Could not initialize handler.", ex);
//      throw new TaskHandlerException("Could not initialize handler.", ex);
//    }
//  }
//
//  @Override
//  public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
//    try {
//      List<Variable> parameters = loadDbParameters(taskStub);
//      parameters.addAll(getVariableParameters(subject, taskStub));
//
//      StartProcessInstanceResult result = sendRequest(parameters);
//
//      List<Variable> results = result.getResult();
//      saveResults(subject, taskStub, results);
//
//      completeTask(subject, taskStub);
//    }
//    catch (MalformedURLException e) {
//      logger.error(e.getMessage(), e);
//      completeTaskWithError(subject, taskStub, e.getMessage());
//    }
//    catch (TaskHandlerException e) {
//      logger.error(e.getMessage(), e);
//      completeTaskWithError(subject, taskStub, e.getMessage());
//    }
//    catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      completeTaskWithError(subject, taskStub, e.getMessage());
//    }
//  }
//
//  private List<Variable> loadDbParameters(TaskStub taskStub) throws TaskHandlerException {
//    if (configuration.getRequestDbParameters().isEmpty()) {
//      return new ArrayList<Variable>();
//    }
//
//    Map<String, String> parametersByColumns = new HashMap<String, String>(configuration.getRequestDbParameters().size());
//    List<String> columns = new ArrayList<String>(configuration.getRequestDbParameters().size());
//    for (DbParameter parameter : configuration.getRequestDbParameters()) {
//      String column = parameter.getReference().getColumn();
//      columns.add(column);
//      parametersByColumns.put(column, parameter.getName());
//    }
//
//    Long processInstanceId = taskStub.getProcessInstanceId();
//    Long processDefinitionId = taskStub.getProcessDefinitionId();
//    TransportData transportData = TaskHandlerDatabaseDaoHelper.loadData(processInstanceId, processDefinitionId, columns);
//
//    List<Variable> variables = new ArrayList<Variable>(transportData.getData().size());
//    for (Data data : transportData.getData()) {
//      String parameterName = parametersByColumns.get(data.getField());
//
//      Object value = data.getValue();
//      if (value != null)
//        variables.add(VariableHelper.createVariable(parameterName, value));
//      else
//        logger.warn("Database request parameter " + parameterName + " is null.");
//    }
//
//    return variables;
//  }
//
//  private List<Variable> getVariableParameters(Subject subject, TaskStub taskStub) throws TaskHandlerException {
//    Map<String, Object> runaVariables = getRunaVariables(subject, taskStub);
//
//    List<Variable> requestVariables = new ArrayList<Variable>(configuration.getRequestVariableParameters().size());
//    for (Map.Entry<String, String> parameter : configuration.getRequestVariableParameters().entrySet()) {
//      String parameterName = parameter.getKey();
//      Object value = runaVariables.get(parameter.getValue());
//      if (value != null)
//        requestVariables.add(VariableHelper.createVariable(parameterName, value));
//      else
//        logger.warn("Runa variable " + parameter.getValue() + " used as request parameter " + parameterName + " does not exist, is null or bot does not have permissions for it.");
//    }
//
//    return requestVariables;
//  }
//
//  private StartProcessInstanceResult sendRequest(List<Variable> parameters) throws MalformedURLException {
//    String webserviceName = "StartProcessInstanceService"; //todo
//    URL wsdlUrl = new URL(WebserviceHelper.getUrl(webserviceName));
//    StartProcessInstanceServiceClient service = new StartProcessInstanceServiceClient(wsdlUrl, new QName("http://runaex", "StartProcessInstanceServiceService"));
//
//    StartProcessInstanceRequest request = new StartProcessInstanceRequest();
//    if (configuration.getEndpointName() != null)
//      request.setEndpointName(configuration.getEndpointName());
//    request.setProcessName(configuration.getProcessName());
//    request.setVariables(parameters);
//    return service.getStartProcessInstancePort().startProcessInstance(request);
//  }
//
//  private void saveResults(Subject subject, TaskStub taskStub, List<Variable> results) throws TaskHandlerException {
//    Long processInstanceId = taskStub.getProcessInstanceId();
//    Long processDefinitionId = taskStub.getProcessDefinitionId();
//
//    List<Data> dataList = new ArrayList<Data>(configuration.getResponseDbParameters().size());
//    Map<String, Object> runaVariables = new HashMap<String, Object>();
//
//    for (Variable result : results) {
//      ColumnReference reference = configuration.getResponseDbParameters().get(result.getName());
//
//      if (reference != null) {
//        Data data = new Data();
//        data.setTable(reference.getTable());
//        data.setField(reference.getColumn());
//        data.setValue(VariableHelper.getValue(result));
//
//        dataList.add(data);
//      }
//      else {
//        String runaVariableName = configuration.getResponseVariableParameters().get(result.getName());
//        if (runaVariableName != null) {
//          runaVariables.put(runaVariableName, VariableHelper.getValue(result));
//        }
//        else {
//          logger.warn("Unmapped response parameter " + result.getName());
//        }
//      }
//    }
//
//    if (dataList.size() < configuration.getResponseDbParameters().size()) {
//      logger.warn("Some expected response parameters were not found");
//    }
//
//    if (!dataList.isEmpty()) {
//      TaskHandlerDatabaseDaoHelper.saveResultsToDatabase(processInstanceId, processDefinitionId, dataList);
//    }
//
//    saveResultsToVariables(subject, taskStub, runaVariables);
//  }
//
//  private void saveResultsToVariables(Subject subject, TaskStub taskStub, Map<String, Object> results) throws TaskHandlerException {
//    Map<String, Object> variables = new HashMap<String, Object>();
//
//    for (Map.Entry<String, String> parameter : configuration.getResponseVariableParameters().entrySet()) {
//      Object result = results.get(parameter.getKey());
//      if (result == null) {
//        logger.warn("Expected response parameter \"" + parameter.getKey() + "\" not found.");
//      }
//      variables.put(parameter.getValue(), result);
//    }
//
////    TODO: Use CORRECT version of RunaWFE!
////    ExecutionServiceDelegate executionService = DatabaseSpringContext.getExecutionServiceDelegate();
////    try {
////      executionService.updateVariables(subject, taskStub.getId(), variables);
////    }
////    catch (AuthorizationException e) {
////      logger.error("Could not save response to variables.", e);
////      throw new TaskHandlerException("Could not save response to variables.", e);
////    }
////    catch (AuthenticationException e) {
////      logger.error("Could not save response to variables.", e);
////      throw new TaskHandlerException("Could not save response to variables.", e);
////    }
////    catch (TaskDoesNotExistException e) {
////      logger.error("Could not save response to variables.", e);
////      throw new TaskHandlerException("Could not save response to variables.", e);
////    }
//  }
//
//}
