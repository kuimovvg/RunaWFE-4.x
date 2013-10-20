package ru.cg.runaex.web.service;

import org.apache.ecs.html.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.ParFile;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.exceptions.ProjectParseException;
import ru.cg.runaex.web.model.DeployedBusinessApplication;
import ru.cg.runaex.web.model.DeployedProcess;
import ru.cg.runaex.web.model.ProcessDefinition;
import ru.cg.runaex.web.model.Task;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

import javax.ejb.EJBException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Петров А.
 */
@Service
public class RunaWfeServiceImpl implements RunaWfeService {
  private final Logger logger = LoggerFactory.getLogger(RunaWfeServiceImpl.class);

  private final ExecutionService executionService;
  private final BatchPresentation defaultTaskClassBatchPresentation;

  private final DefinitionService definitionService;
  private final BatchPresentation defaultProcessDefinitionBatchPresentation;
  private final BatchPresentation defaultProcessInstanceClassPresentation;

  private final AuthorizationService authorizationService;

  @Autowired
  private BaseDao baseDao;

  public RunaWfeServiceImpl() {
    executionService = Delegates.getExecutionService();
    defaultTaskClassBatchPresentation = new BatchPresentationFactory(ClassPresentationType.TASK).createDefault();

    definitionService = Delegates.getDefinitionService();
    defaultProcessDefinitionBatchPresentation = new BatchPresentationFactory(ClassPresentationType.DEFINITION).createNonPaged();

    defaultProcessInstanceClassPresentation = new BatchPresentationFactory(ClassPresentationType.PROCESS).createDefault();
    //Количество получаемых экземпляром,последние 100 процессов
    defaultProcessInstanceClassPresentation.setRangeSize(100);
    defaultProcessInstanceClassPresentation.setFirstFieldToSort(3);

    authorizationService = Delegates.getAuthorizationService();
  }

  public List<Task> getTasks(User user) {
    List<WfTask> tasks = executionService.getTasks(user, defaultTaskClassBatchPresentation);
    if (tasks == null) {
      tasks = new ArrayList<WfTask>();
    }
    List<Task> list = new LinkedList<Task>();
    int number = 1;
    for (WfTask wfeTask : tasks) {
      Task task = new Task();
      task.setId(wfeTask.getId());
      task.setName(wfeTask.getName());
//      task.setVersion(wfeTask.get);
      task.setDescription(wfeTask.getDescription());
      task.setProcessDefName(wfeTask.getDefinitionName());
      task.setNumber(number++);

      //Дата создания для отображения в меню.
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      task.setCreationDate(dateFormat.format(wfeTask.getCreationDate()));
      task.setDeadlineDate(dateFormat.format(wfeTask.getDeadlineDate()));
      list.add(task);
    }

    return list;
  }

  public List<WfProcess> getProcessInstanceStubs(User user) throws AuthenticationException, AuthorizationException {
    return executionService.getProcesses(user, defaultProcessInstanceClassPresentation);
  }

  @Override
  public List<ProcessDefinition> getProcessDefinitionsForStart(User user) {
    List<WfDefinition> definitions = definitionService.getLatestProcessDefinitions(user, defaultProcessDefinitionBatchPresentation);
    boolean[] permissions = authorizationService.isAllowed(user, DefinitionPermission.START_PROCESS, definitions);

    List<WfDefinition> filtered = new ArrayList<WfDefinition>();
    int i = 0;
    for (WfDefinition definition : definitions) {
      if (permissions[i])
        filtered.add(definition);
      i++;
    }

    return convertProcessDefinitions(filtered);
  }

  @Override
  public List<ProcessDefinition> getProcessDefinitionsWithTasks(User user) {
    List<WfDefinition> processDefinitionStubs = definitionService.getLatestProcessDefinitions(user, defaultProcessDefinitionBatchPresentation);
    List<ProcessDefinition> processDefinitions = convertProcessDefinitions(processDefinitionStubs);

    List<Task> tasks = getTasks(user);
    if (tasks != null) {
      for (Task task : tasks) {
        for (ProcessDefinition procDef : processDefinitions) {
          if (task.getProcessDefName().equals(procDef.getName())) {
            procDef.getTasks().add(task);
          }
        }
      }
    }
    return processDefinitions;
  }

  private List<ProcessDefinition> convertProcessDefinitions(List<WfDefinition> processDefinitions) {
    List<ProcessDefinition> converted = new ArrayList<ProcessDefinition>(processDefinitions.size());
    for (WfDefinition definition : processDefinitions) {
      ProcessDefinition dto = new ProcessDefinition();
      dto.setId(definition.getId());
      dto.setName(definition.getName());
      dto.setType(definition.getCategories());
      dto.setTasks(new ArrayList<Task>());
      converted.add(dto);
    }
    return converted;
  }

  @Override
  public ProcessDefinition getProcessDefinitionByName(User user, String defName) throws DefinitionDoesNotExistException {
    ProcessDefinition processDefinition;
    try {
      WfDefinition processDefinitionStubs = definitionService.getLatestProcessDefinition(user, defName);
      processDefinition = new ProcessDefinition();
      processDefinition.setId(processDefinitionStubs.getId());
      processDefinition.setName(processDefinitionStubs.getName());
      processDefinition.setTasks(new ArrayList<Task>());
      processDefinition.setType(processDefinitionStubs.getCategories());
      List<Task> tasks = getTasks(user);
      if (tasks != null) {
        for (Task task : tasks) {
          if (task.getProcessDefName().equals(processDefinitionStubs.getName())) {
            processDefinition.getTasks().add(task);
          }
        }
      }
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof DefinitionDoesNotExistException) {
        throw (DefinitionDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    return processDefinition;
  }

  public WfTask getTask(User user, Long taskId) throws TaskDoesNotExistException {
    WfTask task;
    try {
      task = executionService.getTask(user, taskId);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof TaskDoesNotExistException) {
        throw (TaskDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    return task;
  }

  @Override
  public WfTask getNextTask(User user, Long superProcessInstanceId, Long processId, String nextTask) {
    WfTask nextTaskStub = getNextTaskByProcessId(user, processId);

    /**
     * if not found next task then find in parent process (exit from subprocess)
     */
    if (nextTaskStub == null && superProcessInstanceId != null) {
      nextTaskStub = getNextTaskByProcessId(user, superProcessInstanceId);
    }

    if (nextTaskStub == null && nextTask != null) {
      List<WfTask> activeTasks = getTaskStubs(user);
      if (activeTasks != null) {
        for (WfTask wfTask : activeTasks) {
          Long parentProcessId = getParentProcessId(user, wfTask.getProcessId());
          if (parentProcessId != null &&
              wfTask.getName().equals(nextTask)) {
            nextTaskStub = wfTask;
            break;
          }
        }
      }
    }
    return nextTaskStub;
  }

  public WfTask getNextTaskByProcessId(User user, Long processInstanceId) throws ProcessDoesNotExistException {
    try {
      BatchPresentation processTasks = BatchPresentationFactory.TASKS.createNonPaged();
      int processIdIndex = processTasks.getClassPresentation().getFieldIndex(TaskClassPresentation.PROCESS_ID);
      FilterCriteria criteria = FilterCriteriaFactory.createFilterCriteria(processTasks, processIdIndex);
      criteria.getFilterTemplates()[0] = String.valueOf(processInstanceId);
      processTasks.getFilteredFields().put(processIdIndex, criteria);

      List<WfTask> taskInstances = executionService.getTasks(user, processTasks);
      for (WfTask taskInstance : taskInstances) {
        if (taskInstance.isFirstOpen()) {
          return taskInstance;
        }
      }
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ProcessDoesNotExistException) {
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getFormHtml(User user, Long taskId, String taskName, String processName) throws ProcessDoesNotExistException, TaskDoesNotExistException {
    StringBuilder html;
    try {
      WfTask task = getTask(user, taskId);
      if (!task.getName().equals(taskName))
        throw new IllegalArgumentException("Task names don't match");
      if (!task.getDefinitionName().equals(processName))
        throw new IllegalArgumentException("Process names don't match");

      byte[] ftlBytes = definitionService.getTaskInteraction(user, taskId).getFormData();
      if (ftlBytes == null) {
        ftlBytes = new byte[0];
      }
      String ftlForm = new String(ftlBytes, Charset.forName("utf-8"));

      Map<String, Object> formVariables = prepareVariablesForForm(user, task, processName);

//      FormHashModel model = new FormHashModel(user, new MapDelegableVariableProvider(formVariables, new DelegateProcessVariableProvider(user, task.getProcessId())), null);
      FormHashModel model = new FormHashModel(user, new MapDelegableVariableProvider(formVariables, null), null);
      String build = build(ftlForm, model);
      html = new StringBuilder(build);

      /**
       * save change object info
       */
      logger.debug("form variables after getting form - " + formVariables);
      Map<Long, String> changeObjectInfo = (Map<Long, String>) formVariables.get(WfeRunaVariables.CHANGE_OBJECT_INFO);
      if (!changeObjectInfo.isEmpty())
        baseDao.chgObjectInfo(changeObjectInfo);
      TransportData createHiddenInput = (TransportData) formVariables.get(WfeRunaVariables.CREATE_HIDDEN_INPUT);
      if (!createHiddenInput.getData().isEmpty()) {
        for (Data data : createHiddenInput.getData()) {
          logger.debug("data - " + data);
          if (data.getValue() != null) {
            logger.debug("data.getField() - " + data.getField());
            logger.debug("data.getValue() - " + data.getValue());
            Input htmlHiddenId = new Input(Input.hidden, data.getField(), String.valueOf(data.getValue()));
            htmlHiddenId.setClass("runaex save-hidden-input");
            htmlHiddenId.addAttribute("data-schema", data.getSchema());
            htmlHiddenId.addAttribute("data-table", data.getTable());
            htmlHiddenId.addAttribute("no_sign", "");
            html = html.append(htmlHiddenId.toString());
          }
        }
      }

      Input htmlTaskId = new Input(Input.hidden, "id", String.valueOf(taskId));
      Input htmlTaskName = new Input(Input.hidden, "taskName", task.getName());
      Input htmlProcessInstanceId = new Input(Input.hidden, "processInstanceId", String.valueOf(task.getProcessId()));
      Input htmlProcessName = new Input(Input.hidden, "processName", String.valueOf(task.getDefinitionName()));
      Input htmlProcessDefinitionId = new Input(Input.hidden, "processDefinitionId", String.valueOf(task.getDefinitionId()));
      htmlProcessDefinitionId.setID("processDefinitionId");
      html = html.append(htmlTaskId.toString());
      html = html.append(htmlTaskName.toString());
      html = html.append(htmlProcessInstanceId.toString());
      html = html.append(htmlProcessName.toString());
      html = html.append(htmlProcessDefinitionId.toString());
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof TaskDoesNotExistException) {
        logger.error("Couldn't get form for task " + taskId + " " + taskName, ex);
        throw (TaskDoesNotExistException) ex.getCause();
      }
      else if (ex.getCause() instanceof ProcessDoesNotExistException) {
        logger.error("Couldn't get form for task " + taskId + " " + taskName, ex);
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    catch (RuntimeException ex) {
      logger.error("Couldn't get form for task " + taskId + " " + taskName, ex);
      throw ex;
    }
    return html.toString();
  }

  private Map<String, Object> prepareVariablesForForm(User user, WfTask task, String processName) {
    Map<String, Object> variables = new HashMap<String, Object>();

    List<WfVariable> wfVariables = executionService.getVariables(user, task.getProcessId());
    Map<String, Object> runaVariables = new HashMap<String, Object>(wfVariables.size());
    for (WfVariable wfVariable : wfVariables) {
      String variableName = wfVariable.getDefinition().getName();
      variables.put(variableName, wfVariable.getValue());
      runaVariables.put(variableName, wfVariable.getValue());
    }
    variables.put(WfeRunaVariables.RUNA_VARIABLES, runaVariables);

    variables.put(WfeRunaVariables.PROCESS_INSTANCE_ID, task.getProcessId());

    String strNavigatorAction = baseDao.getVariableFromDb(task.getProcessId(), WfeRunaVariables.NAVIGATOR_ACTION);
    variables.put(WfeRunaVariables.NAVIGATOR_ACTION, strNavigatorAction);

    Map<Long, ObjectInfo> objectInfoMap = new HashMap<Long, ObjectInfo>();
    Map<Long, String> objectInfoJson = baseDao.getObjectInfoFromDb(task.getProcessId());
    for (Map.Entry<Long, String> entry : objectInfoJson.entrySet()) {
      Long contextVariableId = entry.getKey();
      ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(entry.getValue(), ObjectInfo.class);
      objectInfoMap.put(contextVariableId, tmpObjectInfo);
    }
    variables.put(WfeRunaVariables.OBJECT_INFO, objectInfoMap);

    WfDefinition definition = definitionService.getLatestProcessDefinition(SecurityUtils.getCurrentRunaUser(), processName);
    variables.put(WfeRunaVariables.PROJECT_NAME_VARIABLE, definition.getCategories()[0]);
    variables.put(WfeRunaVariables.PROCESS_DEFINITION_ID, definition.getId());

    variables.put(WfeRunaVariables.CREATE_HIDDEN_INPUT, new TransportData());
    variables.put(WfeRunaVariables.CHANGE_OBJECT_INFO, new HashMap<Long, String>());

    logger.debug("form variables - " + variables);

    return variables;
  }

  private String build(String ftlForm, FormHashModel model) {
    try {
      return FreemarkerProcessor.process(ftlForm, model);
    }
    catch (RuntimeException ex) {
      logger.error("ex.toString() - " + ex.toString(), ex);
      throw ex;
    }
  }

  @Override
  public Long startProcessInstance(User user, String processName) throws DefinitionDoesNotExistException, ValidationException {
    try {
      return executionService.startProcess(user, processName, new HashMap<String, Object>());  //TODO: there was two parameters new HashMap<String, Object>() - is a cap!
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ValidationException) {
        throw (ValidationException) ex.getCause();
      }
      else if (ex.getCause() instanceof DefinitionDoesNotExistException) {
        throw (DefinitionDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
  }

  @Override
  public void completeTask(User user, String name, Long taskId, Long actorId, Map<String, Object> variables) throws ValidationException, TaskDoesNotExistException {
    try {
      executionService.completeTask(user, taskId, variables, null);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof TaskDoesNotExistException) {
        throw (TaskDoesNotExistException) ex.getCause();
      }
      else if (ex.getCause() instanceof ValidationException) {
        throw (ValidationException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
  }

  @Override
  public DeployedBusinessApplication redeployParFiles(User user, List<ParFile> files, String projectName) throws ProjectParseException, DefinitionDoesNotExistException, DefinitionArchiveFormatException, DefinitionAlreadyExistException {
    List<Long> oldProcessDefinitionIds = new LinkedList<Long>();
    List<DeployedProcess> processes = new LinkedList<DeployedProcess>();

    try {
      for (ParFile parFile : files) {
        WfDefinition processDefinition = null;
        byte[] bytes = parFile.getParFileWithValidation();
        try {
          processDefinition = definitionService.getLatestProcessDefinition(user, parFile.getProcessName());
        }
        catch (DefinitionDoesNotExistException ex) {
          logger.debug("Process definition \"".concat(parFile.getProcessName().concat("\" doesn't exists.")));
        }
        catch (EJBException ex) {
          if (!(ex.getCause() instanceof DefinitionDoesNotExistException))
            throw ex;
          logger.debug("Process definition \"".concat(parFile.getProcessName().concat("\" doesn't exists.")));
        }
        if (processDefinition != null) {
          WfDefinition wfDefinition = definitionService.redeployProcessDefinition(user, processDefinition.getId(), bytes, Arrays.asList(projectName));
          oldProcessDefinitionIds.add(processDefinition.getId());
          processes.add(new DeployedProcess(wfDefinition.getId(), parFile));
        }
        else {
          WfDefinition wfDefinition = definitionService.deployProcessDefinition(user, bytes, Arrays.asList(projectName));
          processes.add(new DeployedProcess(wfDefinition.getId(), parFile));
        }
      }
      return new DeployedBusinessApplication(oldProcessDefinitionIds, processes);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof DefinitionDoesNotExistException) {
        throw (DefinitionDoesNotExistException) ex.getCause();
      }
      else if (ex.getCause() instanceof DefinitionArchiveFormatException) {
        throw (DefinitionArchiveFormatException) ex.getCause();
      }
      else if (ex.getCause() instanceof DefinitionAlreadyExistException) {
        throw (DefinitionAlreadyExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
  }

  @Override
  public boolean processDefinitionExists(User user, String processDefinitionName) throws DefinitionDoesNotExistException {
    try {
      definitionService.getLatestProcessDefinition(user, processDefinitionName).hasStartImage();
      return true;
    }
    catch (DefinitionDoesNotExistException ex) {
      return false;
    }
    catch (EJBException ex) {
      if (!(ex.getCause() instanceof DefinitionDoesNotExistException))
        throw ex;
      return false;
    }
  }

  @Override
  public HashMap<String, Object> getVariables(User user, Long taskId) throws ProcessDoesNotExistException, TaskDoesNotExistException {
    HashMap<String, Object> vars;
    try {
      WfTask task = getTask(user, taskId);
      List<WfVariable> wfVariables = executionService.getVariables(user, task.getProcessId());
      vars = new HashMap<String, Object>();
      for (WfVariable wfVariable : wfVariables)
        vars.put(wfVariable.getDefinition().getName(), wfVariable.getValue());
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ProcessDoesNotExistException) {
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    return vars;
  }

  @Override
  public List<WfTask> getTaskStubs(User user) {
    return executionService.getTasks(user, defaultTaskClassBatchPresentation);
  }

  @Override
  public void removeVariable(User user, Long processInstanceId, String variableName) throws ProcessDoesNotExistException {
    try {
      HashMap<String, Object> variables = new HashMap<String, Object>();
      variables.put(variableName, null);
      executionService.updateVariables(user, processInstanceId, variables);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ProcessDoesNotExistException) {
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
  }

  @Override
  public Long getParentProcessId(User user, Long processId) throws ProcessDoesNotExistException {
    Long parentProcessId = null;
    try {
      WfProcess parentProcess = executionService.getParentProcess(user, processId);
      if (parentProcess != null)
        parentProcessId = parentProcess.getId();
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ProcessDoesNotExistException) {
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    return parentProcessId;
  }

  @Override
  public void updateVariables(User user, Long taskId, HashMap<String, Object> variables) throws TaskDoesNotExistException, ProcessDoesNotExistException {
    try {
      Long processId = getTask(user, taskId).getProcessId();
      executionService.updateVariables(user, processId, variables);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof ProcessDoesNotExistException) {
        throw (ProcessDoesNotExistException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
  }

  @Override
  public WfDefinition getProcessDefinition(User user, Long processDefinitionId) {
    return definitionService.getProcessDefinition(user, processDefinitionId);
  }
}
