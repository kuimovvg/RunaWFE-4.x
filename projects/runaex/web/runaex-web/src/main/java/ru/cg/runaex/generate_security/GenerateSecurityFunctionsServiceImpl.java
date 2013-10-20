package ru.cg.runaex.generate_security;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.ResourceTransactionManager;

import com.cg.security_manager.core.bean.RequestType;
import ru.cg.runaex.database.bean.FtlComponent;
import ru.cg.runaex.database.bean.ParFile;
import ru.cg.runaex.components.bean.component.ActionButton;
import ru.cg.runaex.components.bean.component.BaseFile;
import ru.cg.runaex.components.bean.component.NavigateButton;
import ru.cg.runaex.components.bean.component.PrintButton;
import ru.cg.runaex.components.bean.component.field.Autocomplete;
import ru.cg.runaex.components.bean.component.grid.BaseTree;
import ru.cg.runaex.components.bean.component.grid.GridComponent;
import ru.cg.runaex.exceptions.ProjectParseException;
import ru.cg.runaex.generate_security.dao.FunctionDao;
import ru.cg.runaex.generate_security.dao.ModuleDao;
import ru.cg.runaex.generate_security.model.Function;
import ru.cg.runaex.generate_security.model.Module;

/**
 * @author urmancheev
 */
@Service
public class GenerateSecurityFunctionsServiceImpl implements GenerateSecurityFunctionsService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ModuleDao moduleDao;
  @Autowired
  private FunctionDao functionDao;

  @Autowired
  @Qualifier("securityManagerTransactionManager")
  private ResourceTransactionManager transactionManager;
  @Autowired
  @Qualifier("generateSecurityFunctionNamesMessageSource")
  private ResourceBundleMessageSource messageSource;
  @Autowired
  @Qualifier("pluginMessageSource")
  private ResourceBundleMessageSource gpdMessageSource;

  @Override
  public void generateSecurityFunctions(List<ParFile> parFiles) {
    for (ParFile parFile : parFiles) {
      generateSecurityFunctions(parFile);
    }
  }

  private void generateSecurityFunctions(ParFile parFile) {
    try {
      generateSecurityFunctionsUnchecked(parFile);
    }
    catch (ProjectParseException e) {
      logger.warn("Can not create functions for process", e);
    }
  }

  private void generateSecurityFunctionsUnchecked(ParFile parFile) throws ProjectParseException {
    String processName = parFile.getProcessName();

    Long moduleId = moduleDao.getIdByName(processName);
    boolean notNewProcess = moduleId != null;
    if (notNewProcess)
      return;

    List<String> taskNames = parFile.getTaskNames();
    List<FtlComponent> components = parFile.getFtlComponents();

    Function startProcessFunction = createStartProcessFunction(processName);
    List<Function> taskFunctions = createTaskFunctions(taskNames, processName);
    List<Function> componentFunctions = createComponentFunctions(components, processName);

    moduleId = saveModule(processName);
    saveFunction(startProcessFunction, moduleId);
    saveFunctions(taskFunctions, moduleId);
    saveFunctions(componentFunctions, moduleId);
  }

  private Long saveModule(String processName) {
    Module module = new Module();
    module.setName(processName);
    return moduleDao.save(module);
  }

  private void saveFunctions(List<Function> functions, Long moduleId) {
    for (Function function : functions) {
      saveFunction(function, moduleId);
    }
  }

  private void saveFunction(Function function, Long moduleId) {
    function.setModuleId(moduleId);

    TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
    try {
      Long functionId = functionDao.save(function);
      functionDao.saveParameters(functionId, function.getParameters());
      transactionManager.commit(status);
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      transactionManager.rollback(status);
    }
  }

  private List<Function> createTaskFunctions(List<String> taskNames, String processName) {
    List<Function> functions = new LinkedList<Function>();
    for (String taskName : taskNames) {
      functions.add(createTaskFunction(taskName, processName));
    }
    return functions;
  }

  private Function createStartProcessFunction(String processName) {
    Function function = new Function();
    function.setName(messageSource.getMessage("startProcessFunctionName", new Object[] {processName}, Locale.ROOT));
    function.setUrl("startProcessInstance");
    function.setRequestType(RequestType.GET);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(1);
    parameters.add(new Function.Parameter("n", processName));
    function.setParameters(parameters);

    return function;
  }

  private Function createTaskFunction(String taskName, String processName) {
    Function function = new Function();
    function.setName(taskName);
    function.setUrl("task");
    function.setRequestType(RequestType.GET);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(2);
    parameters.add(new Function.Parameter("name", taskName));
    parameters.add(new Function.Parameter("pname", processName));
    function.setParameters(parameters);

    return function;
  }

  private List<Function> createComponentFunctions(List<FtlComponent> ftlComponents, String processName) {
    List<Function> functions = new LinkedList<Function>();
    for (FtlComponent ftlComponent : ftlComponents) {
      switch (ftlComponent.getComponentType()) {
        case FLEXI_GRID:
          functions.addAll(createGridFunctions(ftlComponent));
          break;
        case DEPENDENT_FLEXI_GRID:
          functions.addAll(createGridFunctions(ftlComponent));
          break;
        case LINK_FLEXI_GRID:
          functions.addAll(createGridFunctions(ftlComponent));
          break;
        case SELECT_FLEXI_GRID:
          functions.addAll(createGridFunctions(ftlComponent));
          break;
        case SELECT_TREE_GRID:
        case TREE_GRID:
          functions.add(createTreeGridFunction(ftlComponent));
          break;
        case AUTOCOMPLETE:
          functions.add(createAutocompleteFunction(ftlComponent));
          break;
        case FILE_UPLOAD:
        case FILE_VIEW:
          functions.add(createDownloadFileFunction(ftlComponent));
          break;
        case PRINT_BUTTON:
          functions.add(createPrintFunction(ftlComponent));
          break;
        case ACTION_BUTTON:
          functions.addAll(createActionFunctions(ftlComponent, processName));
          break;
        case NAVIGATE_BUTTON:
          functions.addAll(createNavigationFunctions(ftlComponent, processName));
          break;
      }
    }
    return functions;
  }

  private List<Function> createGridFunctions(FtlComponent ftlComponent) {
    List<Function> functions = new ArrayList<Function>(2);
    functions.add(createLoadGridFunction(ftlComponent));
    functions.add(createSaveSelectedRowFunction(ftlComponent));
    return functions;
  }

  private Function createLoadGridFunction(FtlComponent ftlComponent) {
    GridComponent grid = ftlComponent.getComponent();
    String table = grid.getTable();

    Function function = new Function();
    function.setName(messageSource.getMessage("loadGridFunctionName", new Object[] {grid.getSchema(), table}, Locale.ROOT));
    function.setUrl("flexigrid");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(2);
    parameters.add(new Function.Parameter("s", grid.getSchema()));
    parameters.add(new Function.Parameter("t", table));
    function.setParameters(parameters);

    return function;
  }

  private Function createSaveSelectedRowFunction(FtlComponent ftlComponent) {
    GridComponent grid = ftlComponent.getComponent();
    String table = grid.getTable();

    Function function = new Function();
    function.setName(messageSource.getMessage("saveSelectedRowFunctionName", new Object[] {grid.getSchema(), table}, Locale.ROOT));
    function.setUrl("saveSelectedRow");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(2);
    parameters.add(new Function.Parameter("schema", grid.getSchema()));
    parameters.add(new Function.Parameter("table", table));
    function.setParameters(parameters);

    return function;
  }

  private Function createTreeGridFunction(FtlComponent ftlComponent) {
    BaseTree grid = ftlComponent.getComponent();

    Function function = new Function();
    function.setName(messageSource.getMessage("treeGridFunctionName", new Object[] {grid.getSchema(), grid.getTable()}, Locale.ROOT));
    function.setUrl("treegrid");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(2);
    parameters.add(new Function.Parameter("schema", grid.getSchema()));
    parameters.add(new Function.Parameter("table", grid.getTable()));
    function.setParameters(parameters);

    return function;
  }

  private Function createAutocompleteFunction(FtlComponent ftlComponent) {
    Autocomplete autocomplete = ftlComponent.getComponent();

    Function function = new Function();
    function.setName(messageSource.getMessage("autocompleteFunctionName",
        new Object[] {autocomplete.getSchema(), autocomplete.getTable(), autocomplete.getField()}, Locale.ROOT));
    function.setUrl("autocomplete");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(1);
    String references = autocomplete.getSchema().concat(".").concat(autocomplete.getTable()).concat(".").concat(autocomplete.getField());
    parameters.add(new Function.Parameter("references", references));
    function.setParameters(parameters);

    return function;
  }

  private Function createDownloadFileFunction(FtlComponent ftlComponent) {
    BaseFile componentFile = ftlComponent.getComponent();

    Function function = new Function();
    function.setName(messageSource.getMessage("downloadFileFunctionName", new Object[] {componentFile.getSchema(), componentFile.getTable()}, Locale.ROOT));
    function.setUrl("downloadfile");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(2);
    parameters.add(new Function.Parameter("schema", componentFile.getSchema()));
    parameters.add(new Function.Parameter("table", componentFile.getTable()));
    function.setParameters(parameters);

    return function;
  }

  private Function createPrintFunction(FtlComponent ftlComponent) {
    PrintButton printButton = ftlComponent.getComponent();

    Function function = new Function();
    function.setName(messageSource.getMessage("printFunctionName", new Object[] {printButton.getTableId()}, Locale.ROOT));
    function.setUrl("print");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(1);
    parameters.add(new Function.Parameter("tableId", printButton.getTableId()));
    function.setParameters(parameters);

    return function;
  }

  private List<Function> createActionFunctions(FtlComponent ftlComponent, String processName) {
    ActionButton actionButton = ftlComponent.getComponent();

    String actionPropertyPrefix = "Param.ActionButtonValue.Value.";
    String actionPropertyName = actionPropertyPrefix + actionButton.getAction().name();
    String actionName = gpdMessageSource.getMessage(actionPropertyName, null, Locale.ROOT);
    if (actionName.equals(actionPropertyName))
      actionName = actionButton.getAction().name();

    List<String> tasks = ftlComponent.getSourceForms();
    List<Function> functions = new ArrayList<Function>(tasks.size());
    for (String task : tasks) {
      functions.add(createActionFunction(actionButton, actionName, task, processName));
    }

    return functions;
  }

  private Function createActionFunction(ActionButton actionButton, String actionName, String taskName, String processName) {
    Function function = new Function();
    function.setName(messageSource.getMessage("actionFunctionName", new Object[] {actionName, taskName}, Locale.ROOT));
    function.setUrl("handleActionButton");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(3);
    parameters.add(new Function.Parameter("processName", processName));
    parameters.add(new Function.Parameter("taskName", taskName));
    parameters.add(new Function.Parameter("action", actionButton.getActionStr()));
    function.setParameters(parameters);

    return function;
  }

  private List<Function> createNavigationFunctions(FtlComponent ftlComponent, String processName) {
    NavigateButton navigateButton = ftlComponent.getComponent();

    String actionPropertyPrefix = "Param.NavigateButtonValue.Value.";
    String actionPropertyName = actionPropertyPrefix + navigateButton.getAction().name();
    String actionName = gpdMessageSource.getMessage(actionPropertyName, null, Locale.ROOT);
    if (actionName.equals(actionPropertyName))
      actionName = navigateButton.getAction().name();

    List<String> tasks = ftlComponent.getSourceForms();
    List<Function> functions = new ArrayList<Function>(tasks.size());
    for (String task : tasks) {
      functions.add(createNavigationFunction(navigateButton, actionName, task, processName));
    }

    return functions;
  }

  private Function createNavigationFunction(NavigateButton navigateButton, String actionName, String taskName, String processName) {
    Function function = new Function();
    function.setName(messageSource.getMessage("navigationFunctionName", new Object[] {actionName, taskName, navigateButton.getNextTask()}, Locale.ROOT));
    function.setUrl("navigate");
    function.setRequestType(RequestType.ANY);
    function.setIgnoreUrlEnding(false);

    List<Function.Parameter> parameters = new ArrayList<Function.Parameter>(4);
    parameters.add(new Function.Parameter("processName", processName));
    parameters.add(new Function.Parameter("taskName", taskName));
    parameters.add(new Function.Parameter("action", navigateButton.getActionStr()));
    parameters.add(new Function.Parameter("nextTask", navigateButton.getNextTask()));
    function.setParameters(parameters);

    return function;
  }
}
