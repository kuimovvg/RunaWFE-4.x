package ru.cg.runaex.groovy.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.cg.runaex.groovy.cache.GroovyScriptExecutorCache;

/**
 * @author urmancheev
 */
public final class GroovySpringContext implements ApplicationContextAware {
  private static ApplicationContext context;
  private static final Object lock = new Object();

  private static void ensureContextInitialized() {
    if (context != null) {
      return;
    }

    synchronized (lock) {
      //Double check. If two threads are trying to initialize context
      if (context != null) {
        return;
      }
      context = new ClassPathXmlApplicationContext("/ru/cg/runaex/groovy/spring/spring-groovy.xml");
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  public static GroovyScriptExecutorCache getGroovyExecutorCache() {
    ensureContextInitialized();
    return context.getBean(GroovyScriptExecutorCache.class);
  }
}
