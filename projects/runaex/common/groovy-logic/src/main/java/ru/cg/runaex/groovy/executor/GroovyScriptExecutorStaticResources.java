package ru.cg.runaex.groovy.executor;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Петров А.
 */
public final class GroovyScriptExecutorStaticResources {

  private static Logger LOGGER = LoggerFactory.getLogger(GroovyScriptExecutorStaticResources.class);

  protected static final String TRANSACTION_BLOCK_TEMPLATE;
  protected static final String GROOVY_SCRIPT_TEMPLATE;

  //Load templates
  static {
    String str = null;

    try {
       str = IOUtils.toString(GroovyScriptExecutor.class.getResourceAsStream("groovy_script.template"));
    }
    catch (IOException ex) {
      LOGGER.error(ex.toString(), ex);
    }
    GROOVY_SCRIPT_TEMPLATE = str != null ? str : "";

    try {
      str = IOUtils.toString(GroovyScriptExecutor.class.getResourceAsStream("transactional_script.template"));
    }
    catch (IOException ex) {
      LOGGER.error(ex.toString(), ex);
    }
    TRANSACTION_BLOCK_TEMPLATE = str != null ? str : "";
  }

  private GroovyScriptExecutorStaticResources() {
  }
}
