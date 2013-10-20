package ru.cg.runaex.database.util;

/**
 * @author Абдулин Ильдар
 */
public class NetworkUtil {

  public static boolean isLocalhost(String ip) {
    return "localhost".equalsIgnoreCase(ip) || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip);
  }
}
