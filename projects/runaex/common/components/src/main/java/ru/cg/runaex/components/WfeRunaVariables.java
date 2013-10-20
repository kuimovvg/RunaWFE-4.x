/*
 * Copyright (c) 2012.
 *
 * Class: Session
 * Last modified: 05.09.12 15:51
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.components;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class WfeRunaVariables {

  protected static Logger logger = org.slf4j.LoggerFactory.getLogger(WfeRunaVariables.class);

  public static final String NAVIGATOR_ACTION = "navigator_action";
  public static final String OBJECT_INFO = "object_info";
  public static final String CHANGE_OBJECT_INFO = "change_object_info";
  public static final String CREATE_HIDDEN_INPUT = "create_hidden_input";
  public static final String PROCESS_INSTANCE_ID = "process_instance_id";
  public static final String PROCESS_DEFINITION_ID = "process_definition_id";
  public static final String SELECTED_OBJECT_INFO = "selected_object_info";
  public static final String FILTER = "filter";
  public static final String LINK_TABLE_INFO = "link_table_info";
  public static final String DEFAULT_FILTER = "default_filter";
  public static final String SORT_INFO = "sort_info";
  public static final String CURRENT_USER_CODE = "ТЕКУЩИЙ_ПОЛЬЗОВАТЕЛЬ";
  public static final String DEFAULT_SCHEMA_VARIABLE_NAME;
  public static String NAVIGATE_VARIABLE_NAME;
  public static String SELECTED_ROW_ID_VARIABLE_NAME;
  public static final String PROJECT_NAME_VARIABLE = "projectName";
  public static final String LINK_COLUMN_PREFIX = "link_column_id_";
  public static final String UNDEFINED = "undefined";
  public static final String FUNCTION_PREFIX = "function_";
  public static final String RUNA_VARIABLES = "runa_variables";
  public static final String DEFAULT_FILTER_REPORT2 = "default_filter_report2";

  static {
    Properties properties = new Properties();
    try {
      properties.loadFromXML(WfeRunaVariables.class.getResourceAsStream("/variable_names.xml"));
    }
    catch (IOException ex) {
      logger.error(ex.toString(), ex);
      throw new RuntimeException(ex);
    }
    DEFAULT_SCHEMA_VARIABLE_NAME = properties.getProperty("defaultSchema", "Схема");
    NAVIGATE_VARIABLE_NAME = properties.getProperty("navigateVariable");
    SELECTED_ROW_ID_VARIABLE_NAME = properties.getProperty("rowIdVariable");
  }

  public static String getFilterKeyVariable(String key) {
    return FILTER + "_" + key;
  }

  public static String getDefaultFilterKeyVariable(String key) {
    return DEFAULT_FILTER + "_" + key;
  }

  public static String getSortInfoKeyVariable(String key) {
    return SORT_INFO + "_" + key;
  }

  public static Long getLong(String value) {
    if (value != null) {
      value = value.trim();
      return Long.valueOf(value);
    }
    return null;
  }

  public static String toString(Long value) {
    return String.valueOf(value);  //TODO: To delete
  }

  public static boolean isEmpty(String value) {
    return value == null || value.isEmpty() || "null".equals(value.trim());
  }
}
