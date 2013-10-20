package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.sql.Types;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.reflect.TypeToken;
import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.database.structure.bean.DatabaseStructure;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.database.util.TypeConverter;
import ru.cg.runaex.exceptions.GroovyValidationException;
import ru.cg.runaex.groovy.cache.GroovyScriptExecutorCache;
import ru.cg.runaex.groovy.executor.GroovyScriptExecutor;
import ru.cg.runaex.web.bean.HandleActionRequestField;
import ru.cg.runaex.web.model.CategoryModel;
import ru.cg.runaex.web.model.ProcessDefinition;
import ru.cg.runaex.web.model.ProcessModel;
import ru.cg.runaex.web.model.Task;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.security.model.RunaWfeUser;
import ru.cg.runaex.web.service.RunaWfeService;
import ru.cg.runaex.web.service.TaskService;
import ru.cg.runaex.web.utils.InboxCreator;

/**
 * @author Петров А.
 */
@Controller
public class TasksController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(TasksController.class);

  public static final String CURRENT_TASK_ID = "current_task_id";

  @Autowired
  private RunaWfeService runaWfeService;
  @Autowired
  private BaseDao baseDao;
  @Autowired
  private MetadataDao metadataDao;
  @Autowired
  private TaskService taskService;
  @Autowired
  private GroovyScriptExecutorCache groovyScriptExecutorCache;

  @RequestMapping(value = "/get-tasks", method = RequestMethod.GET)
  @ResponseBody
  public List<Task> getTasksAfterFilter(Integer start, Integer count, String businessProcessName, String category, String taskFilter) throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    List<Task> tasks = Collections.emptyList();
    List<Task> taskList = new ArrayList<Task>();
    List<CategoryModel> categoryModelList = taskService.loadStructuredTasks(null);
    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    if (WfeRunaVariables.UNDEFINED.equals(category) && WfeRunaVariables.UNDEFINED.equals(businessProcessName))
      tasks = runaWfeService.getTasks(currentUser.getUser());
    else if (category != null && !WfeRunaVariables.UNDEFINED.equals(category)) {
      CategoryModel generalModel = new CategoryModel();
      generalModel.setId(0L);
      generalModel.setName("void");
      generalModel.setCategories(categoryModelList);
      Long categoryId = Long.valueOf(category);
      CategoryModel foundModel = findCategoryModel(generalModel, categoryId);
      CategoryModel parentModel = new CategoryModel();
      List<CategoryModel> categoryModels = new LinkedList<CategoryModel>();
      categoryModels.add(foundModel);
      parentModel.setCategories(categoryModels);
      tasks = pullTasks(parentModel);
    }
    else if (businessProcessName != null && !businessProcessName.isEmpty() && !WfeRunaVariables.UNDEFINED.equals(businessProcessName)) {
      ProcessDefinition processDefinition = runaWfeService.getProcessDefinitionByName(currentUser.getUser(), businessProcessName);
      tasks = processDefinition.getTasks();
    }

    if (taskFilter != null && !taskFilter.isEmpty() && !WfeRunaVariables.UNDEFINED.equals(taskFilter)) {
      for (Task task : tasks) {
        if (task.getName().toUpperCase().contains(taskFilter.toUpperCase()))
          taskList.add(task);
      }
    }
    else
      taskList.addAll(tasks);
    return taskList;
  }

  @RequestMapping(value = "/tasks", method = RequestMethod.GET)
  public ModelAndView getTasks() throws AuthenticationException, AuthorizationException {
    return getTasksModelAndView("");
  }

  @RequestMapping(value = "/buildInbox", method = RequestMethod.POST)
  @ResponseBody
  public String buildInbox(@RequestPart(value = "taskFilter") String taskFilter,
                           HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, AuthorizationException {
    List<Task> tasks = runaWfeService.getTasks(SecurityUtils.getCurrentUser().getUser());
    Set<String> businessProcessesByTasks = null;
    if (taskFilter != null && !taskFilter.isEmpty() && !WfeRunaVariables.UNDEFINED.equals(taskFilter)) {
      businessProcessesByTasks = new HashSet<String>(tasks.size());
      for (Task task : tasks) {
        if (task.getName().toUpperCase().contains(taskFilter.toUpperCase()))
          businessProcessesByTasks.add(task.getProcessDefName());
      }
    }
    return InboxCreator.printCategories(taskService.loadStructuredTasks(businessProcessesByTasks));
  }

  @RequestMapping(value = "startProcessInstance", method = RequestMethod.GET)
  public ModelAndView startProcessInstance(@RequestParam("n") String processName) {
    Long processInstanceId;
    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    try {
      processInstanceId = runaWfeService.startProcessInstance(currentUser.getUser(), processName);
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      return getTaskModelAndView(null, messages.getMessage("processError", null, Locale.ROOT) + " \n".concat(ex.toString()).concat("\n"));
    }
    try {
      WfTask task = runaWfeService.getNextTask(currentUser.getUser(), null, processInstanceId, null);
      if (task != null) {
        String name = URLEncoder.encode(task.getName(), "utf-8");
        //return getTask(task.getId(), task.getName(), task.getDefinitionName(), request, response);
        String pname = URLEncoder.encode(task.getDefinitionName(), "utf-8");
        String url = "task?id=" + task.getId() + "&name=" + name + "&pname=" + pname;
        RedirectView rv = new RedirectView(url);
        rv.setExposeModelAttributes(false);
        return new ModelAndView(rv);
      }
      else {
        RedirectView redirectView = new RedirectView("tasks?success=" + URLEncoder.encode(messages.getMessage("processInstanceStarted", null, Locale.ROOT), "utf-8"));
        return new ModelAndView(redirectView);
      }
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      return getTaskModelAndView(null, messages.getMessage("taskError", null, Locale.ROOT) + " \n".concat(ex.toString()).concat("\n"));
    }
  }

  @RequestMapping(value = "task", method = RequestMethod.GET)
  public ModelAndView getTask(@RequestParam("id") Long id,
                              @RequestParam(value = "name") String taskName,
                              @RequestParam(value = "pname") String processName,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException, AuthenticationException, AuthorizationException {
    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    String html = null;

    try {
      html = runaWfeService.getFormHtml(currentUser.getUser(), id, taskName, processName);
      request.getSession().setAttribute(CURRENT_TASK_ID, id);
    }
    catch (TaskDoesNotExistException ex) {
      responseErrorMessage(response, ex, "errorTaskNotFoundMsg");
    }
    catch (AuthenticationException ex) {
      responseErrorMessage(response, ex, "errorAuthenticationMsg");
    }
    catch (AuthorizationException ex) {
      responseErrorMessage(response, ex, "errorAuthorizationMsg");
    }

    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "task");
    mv.addObject("taskHtml", html);
    return mv;
  }

  @RequestMapping(value = "/handleGroovyScriptButton", method = RequestMethod.POST)
  public void handleGroovyScriptButton(@RequestParam(value = "script_id") Long scriptId,
                                       @RequestParam(value = "taskId") Long taskId,
                                       @RequestParam(value = "data", required = false) String data,
                                       HttpServletRequest request, HttpServletResponse response) throws IOException, TaskDoesNotExistException {
    try {
      RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
      WfTask task = runaWfeService.getTask(currentUser.getUser(), taskId);
      Long processDefinitionId = task.getDefinitionId();
      final HashMap<String, Object> variables = runaWfeService.getVariables(currentUser.getUser(), taskId);
      boolean validationRequired = data != null;

      if (validationRequired) {
        Type listType = new TypeToken<ArrayList<HandleActionRequestField>>() {
        }.getType();
        List<HandleActionRequestField> fields = GsonUtil.getGsonObject().fromJson(data, listType);
        addVariablesFromForm(processDefinitionId, variables, fields);
      }
      String groovyScript = metadataDao.getGroovyScript(scriptId);

      if (groovyScript != null && !groovyScript.isEmpty()) {
        WfDefinition definition = runaWfeService.getProcessDefinition(SecurityUtils.getCurrentRunaUser(), processDefinitionId);
        String projectName = definition.getCategories()[0];
        GroovyScriptExecutor executor = groovyScriptExecutorCache.getExecutor(projectName, processDefinitionId);
        Object result = executor.executeScriptWithResult(groovyScript, new Binding(variables), task.getProcessId());
        runaWfeService.updateVariables(currentUser.getUser(), taskId, variables);

        if (validationRequired && result != null && result instanceof String && !((String) result).isEmpty()) {
          throw new GroovyValidationException((String) result);
        }
      }
    }
    catch (GroovyValidationException ex) {
      responseErrorMessage(response, ex, ex.getMessage());
    }
  }

  @RequestMapping(value = "/navigate", method = RequestMethod.POST)
  public ModelAndView handleNavigateButton(HttpServletResponse response,
                                           @RequestParam("nextTask") String nextTask, @RequestParam("action") String action,
                                           @RequestParam("taskId") Long taskId, @RequestParam("taskName") String taskName,
                                           @RequestParam(value = "processName") String processName) throws Exception {
    WfTask task;

    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());
    DataSourceTransactionManager transactionManager = null;
    TransactionStatus status = null;

    try {
      User user = currentUser.getUser();
      task = runaWfeService.getTask(user, taskId);

      Long processDefinitionId = task.getDefinitionId();
      transactionManager = transactionManagerProvider.getTransactionManager(processDefinitionId);
      TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
      status = transactionManager.getTransaction(transactionDefinition);

      if (!task.getName().equals(taskName))
        throw new IllegalArgumentException("Task names don't match");
      if (!task.getDefinitionName().equals(processName))
        throw new IllegalArgumentException("Process names don't match");

      HashMap<String, Object> variables = runaWfeService.getVariables(user, taskId);
      ObjectInfo objectInfo = null;
      Long processInstanceId = task.getProcessId();
      String strSelectedObjectInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
      if (strSelectedObjectInfo != null && !strSelectedObjectInfo.isEmpty())
        objectInfo = GsonUtil.getObjectFromJson(strSelectedObjectInfo, ObjectInfo.class);

      /**
       * save data to DB from session
       */
      baseDao.addVariableToDb(processInstanceId, WfeRunaVariables.NAVIGATOR_ACTION,
          action != null ? action : GpdRunaConfigComponent.NAVIGATE_ACTION_OTHER);

      if (objectInfo != null) {
        Long selectedRowId = objectInfo.getId();

        if (selectedRowId != null)
          variables.put(WfeRunaVariables.SELECTED_ROW_ID_VARIABLE_NAME, selectedRowId);

        if (!(isEmptyObjectInfo(objectInfo) && !isEmptyObjectInfo(objectInfo.getBase()) &&
            !isEmptyObjectInfo(objectInfo.getAttachable()))) {
          baseDao.saveOrUpdateObjectInfo(task.getProcessId(), objectInfo);
        }

        if (!isEmptyObjectInfo(objectInfo.getAttachable())) {
          baseDao.saveOrUpdateObjectInfo(task.getProcessId(), objectInfo.getAttachable());
        }
      }
      /**
       * if current task is subproccess then find super process instance id
       * else super process instance id will be null
       */
      Long superProcessInstanceId = runaWfeService.getParentProcessId(user, task.getProcessId());

      variables.put(WfeRunaVariables.NAVIGATE_VARIABLE_NAME, nextTask);
      runaWfeService.completeTask(user, taskName, taskId, currentUser.getActor().getId(), variables);
      transactionManager.commit(status);

      WfTask nextTaskStub = runaWfeService.getNextTask(user, superProcessInstanceId, task.getProcessId(), nextTask);

      if (nextTaskStub != null) {
        mv.addObject("id", nextTaskStub.getId());
        mv.addObject("name", nextTaskStub.getName());
        mv.addObject("pname", nextTaskStub.getDefinitionName());
      }
      else
        mv.addObject("id", -1L);
    }
    catch (TaskDoesNotExistException ex) {
      transactionManager.rollback(status);
      responseErrorMessage(response, ex, "errorTaskNotFoundWithNumberMsg", nextTask);
    }
    catch (AuthenticationException ex) {
      transactionManager.rollback(status);
      responseErrorMessage(response, ex, "errorAuthenticationMsg");
    }
    catch (AuthorizationException ex) {
      transactionManager.rollback(status);
      responseErrorMessage(response, ex, "errorAuthorizationMsg");
    }
    catch (Exception ex) {
      transactionManager.rollback(status);
      throw ex;
    }
    return mv;
  }

  private void addVariablesFromForm(Long processDefinitionId, HashMap<String, Object> variables, List<HandleActionRequestField> fields) {
    Table table;
    for (HandleActionRequestField field : fields) {
      table = DatabaseStructure.getTable(processDefinitionId, field.getSchema(), field.getTable());
      Column column = table.findColumn(field.getColumn());
      Object value = TypeConverter.convertByType(column, field.getValue());
      variables.put(field.getSchema() + "_" + field.getTable() + "_" + field.getColumn(), value);
    }
  }

  private boolean isEmptyObjectInfo(ObjectInfo objectInfo) {
    return objectInfo == null || objectInfo.getId() == null && objectInfo.getParentId() == null &&
        WfeRunaVariables.isEmpty(objectInfo.getSchema()) && WfeRunaVariables.isEmpty(objectInfo.getTable());
  }

  private ModelAndView getTaskModelAndView(String html, String errors) {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "task");
    mv.addObject("error", errors);
    mv.addObject("taskHtml", html);
    return mv;
  }

  private ModelAndView getTasksModelAndView(String errors) {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "tasks");
    if (StringUtils.trimToNull(errors) != null) {
      mv.addObject("error", errors);
    }
    return mv;
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  private CategoryModel findCategoryModel(CategoryModel m, Long id) {
    if (m.getId().equals(id))
      return m;
    for (CategoryModel model : m.getCategories()) {
      CategoryModel categoryModel = findCategoryModel(model, id);
      if (categoryModel != null)
        return categoryModel;
    }
    return null;
  }

  private List<Task> pullTasks(CategoryModel m) {
    List<Task> tasks = new ArrayList<Task>();
    for (CategoryModel model : m.getCategories()) {
      tasks.addAll(pullTasksFromProcess(model.getProcesses()));
      tasks.addAll(pullTasks(model));
    }
    return tasks;
  }


  private List<Task> pullTasksFromProcess(List<ProcessModel> processModels) {
    List<Task> tasks = new LinkedList<Task>();
    for (ProcessModel processModel : processModels) {
      tasks.addAll(processModel.getTasks());
    }
    return tasks;
  }
}
