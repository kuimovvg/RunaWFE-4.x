package ru.cg.runaex.web.security;

import org.springframework.security.core.context.SecurityContextHolder;
import ru.runa.wfe.user.User;

import ru.cg.runaex.web.security.model.RunaWfeUser;

/**
 * @author Петров А.
 */
public class SecurityUtils {

  public static RunaWfeUser getCurrentUser() {
    Object objPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (objPrincipal instanceof RunaWfeUser) {
      return (RunaWfeUser) objPrincipal;
    }
    return null;
  }

  public static User getCurrentRunaUser() {
    RunaWfeUser user = getCurrentUser();
    if (user != null) {
      return user.getUser();
    }
    return null;
  }
}
