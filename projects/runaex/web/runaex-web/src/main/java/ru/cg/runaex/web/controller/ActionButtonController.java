package ru.cg.runaex.web.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.ActionButton;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.components.util.FileUploadComponentHelper;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.web.bean.HandleActionRequestField;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.security.model.RunaWfeUser;
import ru.cg.runaex.web.service.ActionButtonService;
import ru.cg.runaex.web.service.RunaWfeService;

/**
 * @author urmancheev
 */
@Controller
public class ActionButtonController extends BaseController {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private RunaWfeService runaWfeService;
  @Autowired
  private ActionButtonService actionButtonService;
  @Autowired
  private BaseDao baseDao;

  @RequestMapping(value = "/handleActionButton", method = RequestMethod.POST)
  public ModelAndView handleActionButton(
      @RequestPart(value = "action") String action,
      @RequestPart(value = "data") String data,
      @RequestPart(value = "taskId") Long taskId,
      @RequestPart(value = "taskName") String taskName,
      @RequestParam(value = "processName") String processName,
      @RequestPart(value = "processInstanceId") Long processInstanceId,
      @RequestPart(value = "filesAndFileSignForDeletion", required = false) String filesAndFileSignForDeletion,
      @RequestPart(value = "nextTask", required = false) String nextTask,
      MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());

    DataSourceTransactionManager transactionManager = null;
    TransactionStatus status = null;
    try {
      WfTask task = runaWfeService.getTask(currentUser.getUser(), taskId);
      if (!task.getName().equals(taskName))
        throw new IllegalArgumentException("Task names don't match");
      if (!task.getDefinitionName().equals(processName))
        throw new IllegalArgumentException("Process names don't match");

      Long processDefinitionId = task.getDefinitionId();
      transactionManager = transactionManagerProvider.getTransactionManager(processDefinitionId);
      TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
      status = transactionManager.getTransaction(transactionDefinition);

      Type listType = new TypeToken<ArrayList<HandleActionRequestField>>() {
      }.getType();
      List<HandleActionRequestField> fields = GsonUtil.getGsonObject().fromJson(data, listType);
      Iterator<HandleActionRequestField> it = fields.iterator();
      while (it.hasNext()) {
        HandleActionRequestField field = it.next();
        if (field.getSchema() == null && field.getTable() == null) {
          it.remove();
        }
      }

      HashMap<String, Object> changedRunaVariables = null;
      String successMessageCode = null;

      ActionButton.Action actionValue = ActionButton.getActionByCode(action);
      switch (actionValue) {
        case FIND:
          Map<String, List<Data>> filterDataByTableId = getFilterData(fields, processDefinitionId, processInstanceId);
          actionButtonService.find(filterDataByTableId, processInstanceId);
          break;
        case CANCEL:
          break;
        case SAVE:
          List<Data> columnsData = getData(fields, filesAndFileSignForDeletion, processDefinitionId, processInstanceId, request.getFileMap());
          if (!columnsData.isEmpty()) {
            changedRunaVariables = actionButtonService.save(columnsData, processDefinitionId, processInstanceId);
            successMessageCode = "successSaveMessage";
          }
          break;
        case SAVE_AND_LINK:
          columnsData = getData(fields, filesAndFileSignForDeletion, processDefinitionId, processInstanceId, request.getFileMap());
          if (!columnsData.isEmpty()) {
            changedRunaVariables = actionButtonService.saveAndLink(columnsData, processDefinitionId, processInstanceId);
            successMessageCode = "successSaveMessage";
          }
          break;
        case LINK:
          actionButtonService.link(processDefinitionId, processInstanceId);
          break;
        case UNLINK:
          actionButtonService.unlink(processDefinitionId, processInstanceId);
          break;
        case DELETE:
          actionButtonService.delete(processDefinitionId, processInstanceId);
          successMessageCode = "successDeleteMessage";
          break;
      }

      User user = currentUser.getUser();
      Long superProcessInstanceId = runaWfeService.getParentProcessId(user, task.getProcessId());

      /**
       * done the task
       */
      baseDao.addVariableToDb(processInstanceId, WfeRunaVariables.NAVIGATOR_ACTION, GpdRunaConfigComponent.NAVIGATE_ACTION_OTHER);
      if (changedRunaVariables == null)
        changedRunaVariables = new HashMap<String, Object>(1);
      changedRunaVariables.put(WfeRunaVariables.NAVIGATE_VARIABLE_NAME, nextTask);
      runaWfeService.completeTask(currentUser.getUser(), taskName, taskId, currentUser.getActor().getId(), changedRunaVariables);
      transactionManager.commit(status);

      WfTask nextTaskStub = runaWfeService.getNextTask(user, superProcessInstanceId, task.getProcessId(), nextTask);

      if (nextTaskStub != null) {
        mv.addObject("id", nextTaskStub.getId());
        mv.addObject("name", nextTaskStub.getName());
        mv.addObject("pname", nextTaskStub.getDefinitionName());
      }
      else {
        mv.addObject("noTasksAvailableInProcess", true);
      }

      if (successMessageCode != null)
        mv.addObject("success", messages.getMessage(successMessageCode, null, Locale.ROOT));

    }
    catch (TaskDoesNotExistException ex) {
      if (transactionManager != null) {
        transactionManager.rollback(status);
      }
      responseErrorMessage(response, ex, "errorTaskNotFoundWithNumberMsg", taskName);
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
      if (transactionManager != null) {
        transactionManager.rollback(status);
      }
      throw ex;
    }
    return mv;
  }

  private Map<String, List<Data>> getFilterData(List<HandleActionRequestField> fields, Long processDefinitionId, Long processInstanceId) {
    Map<String, List<Data>> filterDataByTableId = new HashMap<String, List<Data>>();

    for (HandleActionRequestField field : fields) {
      String tableId = field.getTableId();
      if (tableId == null)
        continue;

      List<Data> columnsData = filterDataByTableId.get(tableId);
      if (columnsData == null) {
        columnsData = new LinkedList<Data>();
        filterDataByTableId.put(tableId, columnsData);
      }

      columnsData.add(getFieldData(field, processDefinitionId, processInstanceId));
    }

    return filterDataByTableId;
  }

  private List<Data> getData(List<HandleActionRequestField> fields, String filesAndFileSignForDeletion, Long processDefinitionId, Long processInstanceId, Map<String, MultipartFile> fileMap) throws IOException {
    List<Data> columnsData = new ArrayList<Data>();

    addFieldsData(fields, processDefinitionId, processInstanceId, columnsData);
    addFilesData(fileMap, columnsData);
    setFilesForDeletion(filesAndFileSignForDeletion, columnsData);
    addSelectTreeInfo(processInstanceId, columnsData);

    return columnsData;
  }

  private void addFieldsData(List<HandleActionRequestField> fields, Long processDefinitionId, Long processInstanceId, List<Data> columnsData) {
    for (HandleActionRequestField field : fields) {
      columnsData.add(getFieldData(field, processDefinitionId, processInstanceId));
    }
  }

  private Data getFieldData(HandleActionRequestField field, Long processDefinitionId, Long processInstanceId) {
    Data data = new Data();
    data.setSchema(field.getSchema());
    data.setTable(field.getTable());
    data.setField(field.getColumn());

    Object value = null;
    if (field.getValue() != null)
      value = field.getValue();

    if (field.getCurrentUserAsDefaultValue() != null && field.getCurrentUserAsDefaultValue())
      value = SecurityUtils.getCurrentUser().getFullName();
    else if (field.getAutoGeneratePattern() != null) {
      Long generatedValue = baseDao.getSequenceNextValue(processDefinitionId, field.getSchema(), field.getAutoGenerateSequence());
      value = String.format(field.getAutoGeneratePattern(), generatedValue);
    }
    else if (field.getDefaultFileFromDb() != null && field.getDefaultFileFromDb()) {
      String schemaReference = field.getSchemaReference();
      String tableReference = field.getTableReference();
      String fieldReference = field.getFieldReference();
      ObjectInfo objectInfo = getObjectInfo(schemaReference, tableReference, processInstanceId);

      if (objectInfo != null) {
        TransportData td = baseDao.getDataById(processDefinitionId, objectInfo.getId(), schemaReference, tableReference, FileUploadComponentHelper.getDataColumn(fieldReference), null);

        data.setField(FileUploadComponentHelper.getDataColumn(field.getColumn()));
        if (td != null && !td.getData().isEmpty()) {
          value = td.getData().get(0).getValue();
        }
      }
    }
    else if (field.getCurrentTimeDefaultValue() != null) {
      value = field.getCurrentTimeDefaultValue();
    }

    data.setValue(value);
    return data;
  }

  private void addFilesData(Map<String, MultipartFile> fileMap, List<Data> columnsData) throws IOException {
    for (String key : fileMap.keySet()) {
      MultipartFile file = fileMap.get(key);
      String[] tmp = key.trim().split("[\\\\]");
      String schema = null;
      String table = null;
      String column = null;
      if (!tmp[0].isEmpty())
        schema = tmp[0].trim();
      if (!tmp[1].isEmpty())
        table = tmp[1].trim();
      if (!tmp[2].isEmpty()) {
        column = tmp[2].trim();
      }
      Data value = new Data();
      value.setSchema(schema);
      value.setTable(table);
      value.setField(FileUploadComponentHelper.getDataColumn(column));
      value.setValueClass(ClassType.BYTEA.getSimpleName());
      value.setValue(file.getBytes());
      columnsData.add(value);

      value = new Data();
      value.setTable(table);
      value.setField(FileUploadComponentHelper.getNameColumn(column));
      value.setValue(file.getOriginalFilename());
      columnsData.add(value);
    }
  }

  private void setFilesForDeletion(String filesAndFileSignForDeletion, List<Data> columnsData) {
    filesAndFileSignForDeletion = StringUtils.trimToNull(filesAndFileSignForDeletion);

    if (filesAndFileSignForDeletion != null) {
      String[] filesForDeletionSplited = filesAndFileSignForDeletion.split(",");
      for (String fileForDeletion : filesForDeletionSplited) {
        String[] tmp = fileForDeletion.trim().split("[\\\\]");
        String schema = null;
        String table = null;
        String column = null;
        if (!tmp[0].isEmpty())
          schema = tmp[0].trim();
        if (!tmp[1].isEmpty())
          table = tmp[1].trim();
        if (!tmp[2].isEmpty()) {
          column = tmp[2].trim();
        }

        String signFileColumn = null;
        if (tmp.length >= 5 && !tmp[4].isEmpty() && !"undefined".equals(tmp[4].trim()) && !"null".equals(tmp[4].trim())) {
          signFileColumn = tmp[4].trim();
        }

        Data value = new Data();
        value.setSchema(schema);
        value.setTable(table);
        value.setField(FileUploadComponentHelper.getDataColumn(column));
        value.setValue(null);
        value.setValueClass(ClassType.BYTEA.getSimpleName());
        columnsData.add(value);

        value = new Data();
        value.setSchema(schema);
        value.setTable(table);
        value.setField(FileUploadComponentHelper.getNameColumn(column));
        value.setValue(null);
        value.setValueClass(ClassType.STRING.getSimpleName());
        columnsData.add(value);

        if (signFileColumn != null) {
          value = new Data();
          value.setSchema(schema);
          value.setTable(table);
          value.setField(signFileColumn);
          value.setValue(null);
          value.setValueClass(ClassType.STRING.getSimpleName());
          columnsData.add(value);
        }
      }
    }
  }

  private void addSelectTreeInfo(Long processInstanceId, List<Data> columnsData) {
    String strSelectTreeInfo = baseDao.getVariableFromDb(processInstanceId, WfeRunaVariables.SELECTED_OBJECT_INFO);
    ObjectInfo selectedTreeObjectInfo = GsonUtil.getObjectFromJson(strSelectTreeInfo, ObjectInfo.class);
    if (selectedTreeObjectInfo != null && selectedTreeObjectInfo.getSelectTreeGridfield() != null) {
      Data value = new Data();
      value.setSchema(selectedTreeObjectInfo.getSchema());
      value.setTable(selectedTreeObjectInfo.getTable());
      value.setValue(selectedTreeObjectInfo.getId());
      value.setField(selectedTreeObjectInfo.getSelectTreeGridfield());
      columnsData.add(value);
    }
  }

  private ObjectInfo getObjectInfo(String schema, String table, Long processInstanceId) {
    ObjectInfo objectInfo = null;

    Map<Long, String> objectInfoMap = baseDao.getObjectInfoFromDb(processInstanceId);
    for (Map.Entry<Long, String> objectInfoEntry : objectInfoMap.entrySet()) {
      ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(objectInfoEntry.getValue(), ObjectInfo.class);
      if ((schema + "." + table).equals(tmpObjectInfo.toString())) {
        objectInfo = tmpObjectInfo;
        break;
      }
    }

    return objectInfo;
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
