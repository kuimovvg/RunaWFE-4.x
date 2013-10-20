package ru.cg.runaex.database.util;

/**
 * @author urmancheev
 */
public interface UrlSource {

  String getHost();

  Integer getPort();

  String getPath();
}
