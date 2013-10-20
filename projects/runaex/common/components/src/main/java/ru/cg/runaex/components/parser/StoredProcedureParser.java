package ru.cg.runaex.components.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.bean.component.part.Column;
import ru.cg.runaex.components.bean.component.part.StoredProcedure;

/**
 * @author Kochetkov
 */
public final class StoredProcedureParser {

  public static List<StoredProcedure> parseStoredProcedures(String procedures, String defaultSchema) {
    String schema;
    String storedProcedureName;
    List<StoredProcedure> storedProcedures = null;
    if (procedures != null) {
      String[] proceduresNames = procedures.split(";");
      storedProcedures = new ArrayList<StoredProcedure>(proceduresNames.length);
      for (String procedureName : proceduresNames) {
        List<Column> parameters = new ArrayList<Column>();
        if (procedureName.contains(".")) {
          String[] strings = procedureName.split("\\.");
          schema = StringUtils.trimToNull(strings[0]);
          if (schema == null) {
            schema = defaultSchema;
          }
          storedProcedureName = StringUtils.trimToNull(strings[1]);
        }
        else {
          schema = defaultSchema;
          storedProcedureName = procedureName;
        }

        int beginIndex = storedProcedureName.indexOf("(");
        int endIndex = storedProcedureName.lastIndexOf(")");

        if (beginIndex > 0 && endIndex > 0 && endIndex > beginIndex) {
          String contentInBrackets = storedProcedureName.substring(beginIndex + 1, endIndex);
          List<String> stringList = Arrays.asList(contentInBrackets.split(","));
          for (String p : stringList) {
            String parameter = StringUtils.trimToNull(p);
            if (parameter != null) {
              parameters.add(new Column(parameter));
            }
          }
          storedProcedureName = storedProcedureName.substring(0, beginIndex);
        }

        storedProcedures.add(new StoredProcedure(schema, storedProcedureName, parameters));
      }
    }
    return storedProcedures;
  }
}
