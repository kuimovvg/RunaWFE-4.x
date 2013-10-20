package ru.cg.runaex.runa_ext.handler.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.runa.wfe.commons.GroovyScriptExecutor;

/**
 * @author Абдулин Ильдар
 */
public class ResourceLoader {
  private static final Logger logger = LoggerFactory.getLogger(GroovyScriptExecutor.class);

  public static String loadByClass(Class c, String resourceName) throws IOException {
    InputStream groovyScriptStream = c.getResourceAsStream(resourceName);
    ByteArrayOutputStream byteArrayOutputStream = null;
    try {
      byteArrayOutputStream = new ByteArrayOutputStream();
      IOUtils.copy(groovyScriptStream, byteArrayOutputStream);
      return new String(byteArrayOutputStream.toByteArray(), "utf-8");
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw e;
    }
    finally {
      IOUtils.closeQuietly(groovyScriptStream);
      IOUtils.closeQuietly(byteArrayOutputStream);
    }
  }
}
