package ru.cg.runaex.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

import ru.cg.runaex.database.bean.ParFile;
import ru.cg.runaex.exceptions.ProjectParseException;
import ru.cg.runaex.web.model.DeployedBusinessApplication;
import ru.cg.runaex.web.model.ProcessDefinition;
import ru.cg.runaex.web.model.Task;

/**
 * @author Петров А.
 */
public interface RunaWfeService {

  List<Task> getTasks(User user);

  List<ProcessDefinition> getProcessDefinitionsForStart(User user);

  List<WfProcess> getProcessInstanceStubs(User user) throws AuthenticationException, AuthorizationException;

  ProcessDefinition getProcessDefinitionByName(User user, String defName) throws DefinitionDoesNotExistException;

  WfTask getTask(User user, Long taskId) throws TaskDoesNotExistException;

  WfTask getNextTask(User user, Long superProcessInstanceId, Long processId, String nextTask);

  String getFormHtml(User user, Long taskId, String taskName, String processName) throws ProcessDoesNotExistException, TaskDoesNotExistException;

  Long startProcessInstance(User user, String processName) throws DefinitionDoesNotExistException, ru.runa.wfe.validation.ValidationException;

  void completeTask(User user, String name, Long taskId, Long actorId, Map<String, Object> variables) throws ru.runa.wfe.validation.ValidationException, TaskDoesNotExistException;

  DeployedBusinessApplication redeployParFiles(User user, List<ParFile> files, String projectName) throws ProjectParseException, DefinitionDoesNotExistException, DefinitionArchiveFormatException, DefinitionAlreadyExistException;

  boolean processDefinitionExists(User user, String processDefinitionName) throws DefinitionDoesNotExistException;

  HashMap<String, Object> getVariables(User user, Long taskId) throws ProcessDoesNotExistException, TaskDoesNotExistException;

  List<WfTask> getTaskStubs(User user);

  void removeVariable(User user, Long processInstanceId, String variableName) throws ProcessDoesNotExistException;

  Long getParentProcessId(User user, Long processId);

  void updateVariables(User user, Long taskId, HashMap<String, Object> variables) throws TaskDoesNotExistException, ProcessDoesNotExistException;
  
  WfDefinition getProcessDefinition(User user, Long processDefinitionId);

  List<ProcessDefinition> getProcessDefinitionsWithTasks(User user);
}
