package ru.cg.runaex.web.aspect;

import com.cg.jul.core.resources.properties.service.PropertiesSource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.model.ProcessDefinition;
import ru.cg.runaex.web.service.RunaWfeService;
import ru.cg.runaex.web.utils.SessionUtils;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

import java.io.IOException;
import java.util.*;

/**
 * @author Петров А.
 */
@Aspect
@Component
public class HandleRequestAspect implements InitializingBean {

  private Logger logger = LoggerFactory.getLogger(HandleRequestAspect.class);

  @Autowired
  private RunaWfeService runaWfeService;

  @Autowired
  @Qualifier("cachedPropertiesSource")
  private PropertiesSource propertiesSource;

  private Properties properties;

  @AfterReturning(
      pointcut = "execution(public org.springframework.web.servlet.ModelAndView ru.cg.runaex.web.controller..*(..)) " +
          "&& !execution(public org.springframework.web.servlet.ModelAndView *handleNavigateButton(..))" +
          "&& (within(ru.cg.runaex.web.controller.TasksController)" +
          "|| within(ru.cg.runaex.web.controller.ManageProcessDefinitionsController)" +
          "|| within(ru.cg.runaex.web.controller.ManageReportTemplateController)" +
          "|| within(ru.cg.runaex.web.controller.CreateUsersController)" +
          "|| within(ru.cg.runaex.web.controller.ChangeLogotypeController))", returning = "mv")
  public ModelAndView addCommonObjects(ModelAndView mv) throws IOException {
    addProjectName(mv);
    String showMenuAsSlider = properties.getProperty("showMenuAsSlider");
    if (showMenuAsSlider == null)
      showMenuAsSlider = "true";
    addUserInfo(mv);
    addAdminCapabilities(mv);
    choiceVariantMenu(mv, showMenuAsSlider);
    addProcessDefinitions(mv);
    return mv;
  }

  private void addProjectName(ModelAndView mv) throws IOException {
    mv.addObject("projectName", SessionUtils.getProjectName());
  }

  private void addUserInfo(ModelAndView mv) {
    mv.addObject("username", SecurityUtils.getCurrentUser().getFullName());
  }

  private void addAdminCapabilities(ModelAndView mv) {
    boolean adminCapabilities = false;
    Collection<GrantedAuthority> collection = SecurityUtils.getCurrentUser().getAuthorities();
    if (collection != null && !collection.isEmpty()) {
      for (GrantedAuthority grantedAuthority : collection)
        if (properties.getProperty("processDefinitionAdministrators").equals(grantedAuthority.getAuthority()))
          adminCapabilities = true;
    }
    mv.addObject("adminCapabilities", adminCapabilities);
  }

  private void choiceVariantMenu(ModelAndView mv, String showMenuAsSlider) {
    mv.addObject("showMenuAsSlider", showMenuAsSlider);
  }


  private void addProcessDefinitions(ModelAndView mv) {
    List<ProcessDefinition> processDefinitions = null;
    String errors = (String) mv.getModel().get("error");
    try {
      processDefinitions = runaWfeService.getProcessDefinitionsForStart(SecurityUtils.getCurrentUser().getUser());

      Collections.sort(processDefinitions, new Comparator<ProcessDefinition>() {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      });
    }
    catch (AuthenticationException ex) {
      logger.error(ex.toString(), ex);
      if (errors == null) {
        errors = ex.toString().concat("\n");
      }
      else {
        errors = errors.concat(ex.toString()).concat("\n");
      }

    }
    catch (AuthorizationException ex) {
      logger.error(ex.toString(), ex);
      if (errors == null) {
        errors = ex.toString().concat("\n");
      }
      else {
        errors = errors.concat(ex.toString()).concat("\n");
      }
    }
    if (errors != null) {
      mv.addObject("error", errors);
    }
    mv.addObject("processDefinitions", processDefinitions);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.properties = propertiesSource.getProperties("/ru/cg/runaex/web/messages/config.xml");
  }
}
