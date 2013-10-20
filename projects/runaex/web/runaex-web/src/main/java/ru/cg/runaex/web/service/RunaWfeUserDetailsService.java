package ru.cg.runaex.web.service;

import ru.runa.wfe.security.AuthenticationException;

import ru.cg.runaex.web.security.model.RunaWfeUser;

/**
 * @author Петров А.
 */
public interface RunaWfeUserDetailsService {

  RunaWfeUser getUser(String username, String password) throws AuthenticationException;
}