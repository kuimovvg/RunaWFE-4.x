package ru.cg.runaex.database.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import org.springframework.stereotype.Service;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

import ru.cg.runaex.components.WfeRunaVariables;

/**
 * @author Петров А.
 */
@Service("runaService")
public class RunaServiceImpl implements RunaService {

  private ExecutionService executionService = Delegates.getExecutionService();

  @Override
  public String getDefaultSchema(User user, long processInstanceId) throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    Object value = executionService.getVariable(user, processInstanceId, WfeRunaVariables.DEFAULT_SCHEMA_VARIABLE_NAME).getValue();
    return value != null ? value.toString() : "public";
  }

  @Override
  public Map<String, Object> getVariables(User user, long processInstanceId) {
    List<WfVariable> wfVariables = executionService.getVariables(user, processInstanceId);

    Map<String, Object> variablesMap = new HashMap<String, Object>();

    for (WfVariable wfVariable : wfVariables) {
      variablesMap.put(wfVariable.getDefinition().getName(), wfVariable.getValue());
    }

    return variablesMap;
  }
}
