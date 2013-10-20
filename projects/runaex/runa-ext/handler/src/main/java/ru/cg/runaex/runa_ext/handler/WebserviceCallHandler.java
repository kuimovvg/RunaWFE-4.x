//package ru.cg.runaex.components.handler;
//
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.JDOMException;
//import org.jdom2.Text;
//import org.jdom2.filter.Filters;
//import org.jdom2.xpath.XPathExpression;
//import org.jdom2.xpath.XPathFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import ru.cg.runaex.database.bean.transport.Data;
//import ru.cg.runaex.database.bean.transport.TransportData;
//import ru.cg.runaex.components.bean.component.part.ColumnReference;
//import ru.cg.runaex.components.handler.dao.TaskHandlerDatabaseDaoHelper;
//import ru.cg.runaex.components.handler.util.WebserviceHelper;
//import ru.cg.runaex.components.handler.webservice_call_handler.*;
//import ru.cg.runaex.database.context.DatabaseSpringContext;
//import ru.cg.runaex.wsdl_analyzer.WebServiceClient;
//import ru.cg.runaex.wsdl_analyzer.WebServiceRequestException;
//import ru.cg.runaex.wsdl_analyzer.XMLSupport;
//import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;
//import ru.cg.runaex.wsdl_analyzer.bean.ServiceInfo;
//import ru.cg.runaex.wsdl_analyzer.builder.ComponentBuilder;
//import ru.runa.af.AuthenticationException;
//import ru.runa.af.AuthorizationException;
//import ru.runa.wf.TaskDoesNotExistException;
//import ru.runa.wf.TaskStub;
//import ru.runa.wf.delegate.ExecutionServiceDelegate;
//import ru.runa.wf.logic.bot.TaskHandlerException;
//
//import javax.security.auth.Subject;
//import javax.wsdl.WSDLException;
//import java.util.*;
//
///**
// * @author urmancheev
// */
//public class WebserviceCallHandler extends TaskHandlerBase {
//  private final Logger logger = LoggerFactory.getLogger(getClass());
//
//  private WebserviceCallConfiguration configuration;
//  private OperationInfo webserviceOperation;
//
//
//  @Override
//  public void setConfiguration(String configurationStr) throws TaskHandlerException {
//    String wsdlUrl = null;
//    try {
//      configuration = WebserviceCallConfigurationHelper.parse(configurationStr);
//
//      ComponentBuilder builder = new ComponentBuilder();
//      wsdlUrl = WebserviceHelper.getUrl(configuration.getEsbRelativeUrl());
//
//      Map<String, ServiceInfo> services = builder.buildComponents(wsdlUrl);
//      ServiceInfo service = services.get(configuration.getService());
//      if (service == null)
//        throw new TaskHandlerException("Could not find service " + configuration.getService() + " ");
//
//      webserviceOperation = service.getOperationsByName().get(configuration.getOperation());
//      if (webserviceOperation == null)
//        throw new TaskHandlerException("Could not find operation " + configuration.getOperation() + " for wsdl " + wsdlUrl);
//    }
//    catch (WSDLException e) {
//      logger.error("Could not parse WSDL " + wsdlUrl, e);
//      throw new TaskHandlerException("Could not parse webservice WSDL.", e);
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
//      Map<WebserviceCallParameter, Object> parameters = loadDbParameters(taskStub);
//      parameters.putAll(getVariableParameters(subject, taskStub));
//
//      Document response = sendRequest(parameters);
//
//      List<ResponseDbParameter> dbResults = getResponseDbParameters(response);
//      saveResultsToDatabase(taskStub, dbResults);
//
//      Map<String, Object> variableResults = getResponseVariableParameters(response);
//      saveResultsToVariables(subject, taskStub, variableResults);
//
//      completeTask(subject, taskStub);
//    }
//    catch (WebServiceRequestException ex) {
//      logger.error("Handler execution failed.", ex);
//      completeTaskWithError(subject, taskStub, ex.getMessage());
//    }
//    catch (TaskHandlerException e) {
//      logger.error(e.getMessage(), e);
//      completeTaskWithError(subject, taskStub, e.getMessage());
//    }
//    catch (Exception ex) {
//      logger.error("Handler execution failed.", ex);
//      completeTaskWithError(subject, taskStub, ex.getMessage());
//    }
//  }
//
//  private Map<WebserviceCallParameter, Object> loadDbParameters(TaskStub taskStub) throws TaskHandlerException {
//    if (configuration.getRequestDbParameters().isEmpty()) {
//      return new HashMap<WebserviceCallParameter, Object>();
//    }
//
//    Map<String, DbParameter> parametersByColumns = new HashMap<String, DbParameter>(configuration.getRequestDbParameters().size());
//    List<String> columns = new ArrayList<String>(configuration.getRequestDbParameters().size());
//    for (DbParameter parameter : configuration.getRequestDbParameters()) {
//      String column = parameter.getColumnReference().getColumn();
//      columns.add(column);
//      parametersByColumns.put(column, parameter);
//    }
//    Long processInstanceId = taskStub.getProcessInstanceId();
//    Long processDefinitionId = taskStub.getProcessDefinitionId();
//    TransportData transportData = TaskHandlerDatabaseDaoHelper.loadData(processInstanceId, processDefinitionId, columns);
//
//    Map<WebserviceCallParameter, Object> parameters = new HashMap<WebserviceCallParameter, Object>();
//    for (Data data : transportData.getData()) {
//      DbParameter dbParameter = parametersByColumns.get(data.getField());
//      parameters.put(dbParameter, data.getValue());
//    }
//
//    return parameters;
//  }
//
//  private Map<WebserviceCallParameter, Object> getVariableParameters(Subject subject, TaskStub taskStub) throws TaskHandlerException {
//    Map<String, Object> runaVariables = getRunaVariables(subject, taskStub);
//
//    Map<WebserviceCallParameter, Object> parameters = new HashMap<WebserviceCallParameter, Object>(configuration.getRequestVariableParameters().size());
//    for (VariableParameter parameter : configuration.getRequestVariableParameters()) {
//      Object value = runaVariables.get(parameter.getVariableName());
//      if (value != null)
//        parameters.put(parameter, value);
//      else
//        logger.warn("Runa variable " + parameter.getVariableName() + " does not exist, is null or bot does not have permissions for it.");
//    }
//
//    return parameters;
//  }
//
//  private Document sendRequest(Map<WebserviceCallParameter, Object> parameters) throws WebServiceRequestException, TaskHandlerException {
//    Document request = prepareRequest(parameters);
//    return WebServiceClient.sendRequest(webserviceOperation, request);
//  }
//
//  private Document prepareRequest(Map<WebserviceCallParameter, Object> parameters) throws TaskHandlerException {
//    Document document;
//    try {
//      document = XMLSupport.buildDocumentFromXml(configuration.getRequest());
//    }
//    catch (JDOMException ex) {
//      logger.error("Invalid request xml.", ex);
//      throw new TaskHandlerException("Invalid request xml.", ex);
//    }
//
//    for (Map.Entry<WebserviceCallParameter, Object> parameter : parameters.entrySet()) {
//      XPathExpression<Element> parameterElementExpression = XPathFactory.instance().compile(parameter.getKey().getParentElementXpath(), Filters.element());
//      Element element = parameterElementExpression.evaluateFirst(document);
//      element.removeContent(Filters.cdata());
//      String convertedValue = WebserviceCallConfigurationHelper.convertParameter(parameter.getValue());
//      element.setText(convertedValue);
//    }
//
//    return document;
//  }
//
//  private List<ResponseDbParameter> getResponseDbParameters(Document response) throws WebServiceRequestException {
//    List<ResponseDbParameter> results = new LinkedList<ResponseDbParameter>();
//
//    for (DbParameter parameter : configuration.getResponseDbParameters()) {
//      String stringValue = getResponseParameter(response, parameter.getParentElementXpath());
//      Object parameterValue = WebserviceCallConfigurationHelper.convertParameterValue(stringValue);
//      results.add(new ResponseDbParameter(parameter.getColumnReference(), parameterValue));
//    }
//
//    return results;
//  }
//
//  private Map<String, Object> getResponseVariableParameters(Document response) throws WebServiceRequestException {
//    Map<String, Object> results = new HashMap<String, Object>();
//
//    for (VariableParameter parameter : configuration.getResponseVariableParameters()) {
//      String stringValue = getResponseParameter(response, parameter.getParentElementXpath());
//      Object parameterValue = WebserviceCallConfigurationHelper.convertParameterValue(stringValue);
//      results.put(parameter.getVariableName(), parameterValue);
//    }
//
//    return results;
//  }
//
//  private String getResponseParameter(Document response, String parameterParentElementXpath) throws WebServiceRequestException {
//    XPathExpression<Text> parameterValueExpression = XPathFactory.instance().compile(parameterParentElementXpath + "/text()", Filters.textOnly());
//    Text parameterText;
//
//    try {
//      parameterText = parameterValueExpression.evaluateFirst(response);
//    }
//    catch (NullPointerException ex) {
//      logger.error("Could not parse webservice response.", ex);
//      throw new WebServiceRequestException("Could not parse webservice response.", ex);
//    }
//    catch (IllegalStateException ex) {
//      logger.error("Could not parse webservice response.", ex);
//      throw new WebServiceRequestException("Could not parse webservice response.", ex);
//    }
//
//    if (parameterText != null) {
//      return parameterText.getText();
//    }
//    else {
//      logger.warn("Could not find parameter in response with xpath \"" + parameterParentElementXpath + "\".");
//      return null;
//    }
//  }
//
//  private void saveResultsToDatabase(TaskStub taskStub, List<ResponseDbParameter> results) throws TaskHandlerException {
//    List<Data> dataList = new ArrayList<Data>(results.size());
//    for (ResponseDbParameter parameter : results) {
//      Data data = new Data();
//      data.setTable(parameter.getColumnReference().getTable());
//      data.setField(parameter.getColumnReference().getColumn());
//
//      if (parameter.getValue() == null) {
//        logger.warn("Expected response parameter \"" + parameter.getValue() + "\" not found.");
//      }
//      data.setValue(parameter.getValue());
//
//      dataList.add(data);
//    }
//
//    if (!dataList.isEmpty()) {
//      Long processInstanceId = taskStub.getProcessInstanceId();
//      Long processDefinitionId = taskStub.getProcessDefinitionId();
//      TaskHandlerDatabaseDaoHelper.saveResultsToDatabase(processInstanceId, processDefinitionId, dataList);
//    }
//  }
//
//  private void saveResultsToVariables(Subject subject, TaskStub taskStub, Map<String, Object> results) throws TaskHandlerException {
////    TODO: Use CORRECT version of RunaWFE!
////    ExecutionServiceDelegate executionService = DatabaseSpringContext.getExecutionServiceDelegate();
////    try {
////      executionService.updateVariables(subject, taskStub.getId(), results);
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
//  private static class ResponseDbParameter {
//    private ColumnReference columnReference;
//    private Object value;
//
//    private ResponseDbParameter(ColumnReference columnReference, Object value) {
//      this.columnReference = columnReference;
//      this.value = value;
//    }
//
//    public ColumnReference getColumnReference() {
//      return columnReference;
//    }
//
//    public Object getValue() {
//      return value;
//    }
//  }
//
//}
