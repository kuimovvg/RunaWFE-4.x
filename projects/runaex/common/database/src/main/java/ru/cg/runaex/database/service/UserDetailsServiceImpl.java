package ru.cg.runaex.database.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.runa.wfe.presentation.BatchPresentationFactory;
import org.springframework.stereotype.Service;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;
import ru.runa.wfe.user.logic.ExecutorLogic;

/**
 * @author Петров А.
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  private BatchPresentationFactory batchPresentationFactory = new BatchPresentationFactory(ClassPresentationType.EXECUTOR);
  private ExecutorLogic executorLogic = new ExecutorLogic();

  /**
   * Get user group names
   *
   * @param username - user name
   * @param password - user password
   * @return List of group names
   * @throws AuthenticationException
   * @throws AuthorizationException
   */
  public List<String> getUserGroups(String username, String password) throws AuthenticationException, AuthorizationException {
    User user = Delegates.getAuthenticationService().authenticateByLoginPassword(username, password);
    Actor actor = executorLogic.getActor(user, username);
    List<Group> groups = new LinkedList<Group>(getExecutorGroups(user, actor));
    List<String> strGroups = new ArrayList<String>();
    while (!groups.isEmpty()) {
      Group group = groups.get(0);
      groups.remove(0);
      if (!strGroups.contains(group.getName())) {
        strGroups.add(group.getName());
        groups.addAll(getExecutorGroups(user, group));
      }
    }
    return strGroups;
  }

  /**
   * User belongs to specified group
   *
   * @param username  - user name
   * @param password  - user password
   * @param groupName - group name
   * @return True if user belongs to specified group
   * @throws AuthorizationException
   * @throws AuthenticationException
   */
  public boolean userBelongsToGroup(String username, String password, String groupName) throws AuthorizationException, AuthenticationException {
    return getUserGroups(username, password).contains(groupName);
  }

  private List<Group> getExecutorGroups(User user, Executor executor) throws AuthorizationException, AuthenticationException {
    return Delegates.getExecutorService().getExecutorGroups(user, executor, batchPresentationFactory.createDefault(), false);
  }
}
