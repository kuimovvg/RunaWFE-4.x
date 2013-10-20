package ru.cg.runaex.runa_ext.handler.start_remote_process;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.helper.ColumnReferenceValidationHelper;

/**
 * @author urmancheev
 */
public class StartRemoteProcessConfigurationParser {

  public static StartRemoteProcessConfiguration parse(String configurationStr) throws IllegalArgumentException {
    StartRemoteProcessConfiguration configuration = new Gson().fromJson(configurationStr, StartRemoteProcessConfiguration.class); //JsonSyntaxException

    List<DbParameter> requestDbParameters = new LinkedList<DbParameter>();
    Map<String, String> requestVariableParameters = new HashMap<String, String>();

    Map<String, ColumnReference> responseDbParameters = new HashMap<String, ColumnReference>();
    Map<String, String> responseVariableParameters = new HashMap<String, String>();

    for (StartRemoteProcessParameter parameter : configuration.getRequestParameters()) {
      ColumnReference columnReference = ComponentParser.parseColumnReference(parameter.getSource(), null);

      boolean isDatabaseParameter = ColumnReferenceValidationHelper.isValid(columnReference);
      if (isDatabaseParameter)
        requestDbParameters.add(new DbParameter(parameter.getName(), columnReference));
      else {
        requestVariableParameters.put(parameter.getName(), parameter.getSource());
      }
    }

    for (StartRemoteProcessParameter parameter : configuration.getResponseParameters()) {
      ColumnReference columnReference = ComponentParser.parseColumnReference(parameter.getSource(), null);

      boolean isDatabaseParameter = ColumnReferenceValidationHelper.isValid(columnReference);
      if (isDatabaseParameter)
        responseDbParameters.put(parameter.getName(), columnReference);
      else {
        responseVariableParameters.put(parameter.getName(), parameter.getSource());
      }
    }

    configuration.setRequestDbParameters(requestDbParameters);
    configuration.setRequestVariableParameters(requestVariableParameters);
    configuration.setResponseDbParameters(responseDbParameters);
    configuration.setResponseVariableParameters(responseVariableParameters);

    return configuration;
  }

  public static String serializeToString(StartRemoteProcessConfiguration configuration) {
    return new Gson().toJson(configuration);
  }
}
