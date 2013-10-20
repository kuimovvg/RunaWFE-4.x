package ru.cg.runaex.runa_ext.handler.db_variable_handler;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

/**
 * @author Петров А.
 */
public class DbVariableHandlerConfigurationHelper {

  public static DbVariableHandlerConfiguration parseConfiguration(String configuration) {
    return new Gson().fromJson(configuration, DbVariableHandlerConfiguration.class);
  }

  public static String serializeConfiguration(DbVariableHandlerConfiguration configuration) {
    return new Gson().toJson(configuration);
  }

  public static Map<String, String> asVariableToColumnMapping(DbVariableHandlerConfiguration configuration) {
    if (configuration.getParameters() == null) {
      return null;
    }

    Map<String, String> mapping = new HashMap<String, String>();

    for (DbVariableHandlerParameter parameter : configuration.getParameters()) {
      mapping.put(parameter.getVariableName(), parameter.getColumnName());
    }

    return mapping;
  }

  public static Map<String, String> asColumnToVariableMapping(DbVariableHandlerConfiguration configuration) {
    if (configuration.getParameters() == null) {
      return null;
    }

    Map<String, String> mapping = new HashMap<String, String>();

    for (DbVariableHandlerParameter parameter : configuration.getParameters()) {
      mapping.put(parameter.getColumnName(), parameter.getVariableName());
    }

    return mapping;
  }
}
