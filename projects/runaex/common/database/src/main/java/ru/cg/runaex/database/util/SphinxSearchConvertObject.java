package ru.cg.runaex.database.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class SphinxSearchConvertObject {
  private String value;
  private Map<String, String> idMap = new LinkedHashMap<String, String>();
  private Map<String, String> objectMap = new LinkedHashMap<String, String>();

  public SphinxSearchConvertObject() {

  }

  public SphinxSearchConvertObject(String value, Map<String, String> idMap, Map<String, String> objectMap) {
    this.value = value;
    this.idMap = idMap;
    this.objectMap = objectMap;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Map<String, String> getIdMap() {
    return idMap;
  }

  public void setIdMap(Map<String, String> idMap) {
    this.idMap = idMap;
  }

  public Map<String, String> getObjectMap() {
    return objectMap;
  }

  public void setObjectMap(Map<String, String> objectMap) {
    this.objectMap = objectMap;
  }
}