package ru.cg.runaex.database.util;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Петров А.
 */
public class Closeable {

  private static Logger logger = LoggerFactory.getLogger(Closeable.class);

  public static void close(Object obj) {
    if (obj != null) {
      try {
        Method close = obj.getClass().getMethod("close", new Class[] {});
        try {
          close.invoke(obj);
        }
        catch (Exception e) {
          logger.error(e.toString(), e);
        }
      }
      catch (NoSuchMethodException e) {
        logger.error(e.toString(), e);
      }
    }
  }
}
