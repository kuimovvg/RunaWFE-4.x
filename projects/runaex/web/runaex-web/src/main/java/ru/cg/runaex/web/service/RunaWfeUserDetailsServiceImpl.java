package ru.cg.runaex.web.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.EJBException;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.delegate.AuthenticationServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.delegate.ExecutorServiceDelegate;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import ru.cg.runaex.web.security.model.RunaWfeUser;

/**
 * @author Петров А.
 */
public class RunaWfeUserDetailsServiceImpl implements RunaWfeUserDetailsService {

  private AuthenticationServiceDelegate authenticationServiceDelegate;
  private ExecutorServiceDelegate executorServiceDelegate;
  private BatchPresentation defaultBatchPresentation;

  public RunaWfeUserDetailsServiceImpl() {

    authenticationServiceDelegate = (AuthenticationServiceDelegate) Delegates.getAuthenticationService();
    executorServiceDelegate = (ExecutorServiceDelegate) Delegates.getExecutorService();
    defaultBatchPresentation = new BatchPresentationFactory(ClassPresentationType.EXECUTOR).createDefault();
  }

  @Override
  public RunaWfeUser getUser(String username, String password) throws AuthenticationException {
    RunaWfeUser user;
    try {
      User runaUser = authenticationServiceDelegate.authenticateByLoginPassword(username, password);
      if (runaUser == null) {
        return null;
      }

      Actor actor = executorServiceDelegate.getActorCaseInsensitive(username);

      Collection<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
      List<Group> groups = new LinkedList<Group>(getExecutorGroups(runaUser, actor));
      while (!groups.isEmpty()) {
        Group group = groups.get(0);
        groups.remove(0);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(group.getName());
        if (grantedAuthorities.add(grantedAuthority)) {
          groups.addAll(getExecutorGroups(runaUser, group));
        }
      }

      user = new RunaWfeUser(username, password, grantedAuthorities);
      user.setUser(runaUser);
      user.setActor(actor);
    }
    catch (EJBException ex) {
      if (ex.getCause() instanceof AuthenticationException) {
        throw (AuthenticationException) ex.getCause();
      }
      else {
        throw ex;
      }
    }
    return user;
  }

  private List<Group> getExecutorGroups(User user, Executor executor) {
    return executorServiceDelegate.getExecutorGroups(user, executor, defaultBatchPresentation, false);
  }
}
