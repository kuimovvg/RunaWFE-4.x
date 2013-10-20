package ru.cg.runaex.web.security.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import ru.runa.wfe.user.Actor;

/**
 * @author Петров А.
 */
public class RunaWfeUser extends User {

  private ru.runa.wfe.user.User user;
  private Actor actor;

  public RunaWfeUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
  }

  public RunaWfeUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
  }

  public ru.runa.wfe.user.User getUser() {
    return user;
  }

  public void setUser(ru.runa.wfe.user.User user) {
    this.user = user;
  }

  public Actor getActor() {
    return actor;
  }

  public void setActor(Actor actor) {
    this.actor = actor;
  }

  public String getFullName(){
    if (actor != null && actor.getFullName() != null && !actor.getFullName().isEmpty())
      return actor.getFullName();
    return super.getUsername();
  }
}
