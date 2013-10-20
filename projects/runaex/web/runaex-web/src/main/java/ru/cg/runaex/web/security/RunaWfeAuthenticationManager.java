package ru.cg.runaex.web.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import ru.cg.runaex.web.security.model.RunaWfeUser;
import ru.cg.runaex.web.service.RunaWfeUserDetailsService;

/**
 * @author Петров А.
 */
public class RunaWfeAuthenticationManager implements AuthenticationManager {

  private RunaWfeUserDetailsService userDetailsService;

  public RunaWfeAuthenticationManager(RunaWfeUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (StringUtils.isBlank((String) authentication.getPrincipal()) || StringUtils.isBlank((String) authentication.getCredentials())) {
      throw new BadCredentialsException("Invalid username/password");
    }

    RunaWfeUser user;

    try {
      user = userDetailsService.getUser((String) authentication.getPrincipal(), (String) authentication.getCredentials());
    }
    catch (ru.runa.wfe.security.AuthenticationException ex) {
      throw new BadCredentialsException(ex.getMessage());
    }

    return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), user.getAuthorities());
  }
}
