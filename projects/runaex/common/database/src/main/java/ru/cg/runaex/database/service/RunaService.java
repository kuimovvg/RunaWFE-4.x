package ru.cg.runaex.database.service;

import java.util.Map;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * @author Петров А.
 */
public interface RunaService {

  String getDefaultSchema(User user, long processInstanceId) throws AuthorizationException, AuthenticationException, TaskDoesNotExistException;

  Map<String, Object> getVariables(User user, long processInstanceId);
}
