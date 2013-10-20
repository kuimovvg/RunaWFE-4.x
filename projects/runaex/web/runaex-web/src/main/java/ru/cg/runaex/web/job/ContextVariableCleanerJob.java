package ru.cg.runaex.web.job;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

import ru.cg.runaex.web.security.model.RunaWfeUser;
import ru.cg.runaex.web.service.ContextVariableService;
import ru.cg.runaex.web.service.RunaWfeService;
import ru.cg.runaex.web.service.RunaWfeUserDetailsService;

/**
 * @author Абдулин Ильдар
 */
public class ContextVariableCleanerJob implements InitializingBean {

  @Autowired
  private RunaWfeService runaWfeService;

  @Autowired
  private RunaWfeUserDetailsService runaWfeUserDetailsService;

  private RunaWfeUser runaWfeUser;

  @Autowired
  private ContextVariableService contextVariableService;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  @Qualifier("contextVariableCleanerJobProperties")
  private Properties contextVariableCleanerJobProperties;

  public void doWork() throws AuthenticationException, AuthorizationException {
    try {
      List<WfProcess> processInstanceStubs = runaWfeService.getProcessInstanceStubs(runaWfeUser.getUser());
      if (processInstanceStubs != null) {
        for (WfProcess wfProces : processInstanceStubs) {
          if (wfProces.getEndDate() != null) {
            contextVariableService.removeVariableFromDb(wfProces.getId());
          }
        }
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    runaWfeUser = runaWfeUserDetailsService.getUser(contextVariableCleanerJobProperties.getProperty("username"), contextVariableCleanerJobProperties.getProperty("password"));
    if (runaWfeUser == null) {
      throw new AccessDeniedException("Incorrect login or password");
    }
  }
}
