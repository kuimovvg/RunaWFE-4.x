package ru.cg.runaex.database.service;

import java.util.List;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

/**
 * @author Петров А.
 */
public interface UserDetailsService {

  List<String> getUserGroups(String username, String password) throws AuthenticationException, AuthorizationException;

  boolean userBelongsToGroup(String username, String password, String groupName) throws AuthorizationException, AuthenticationException;
}
