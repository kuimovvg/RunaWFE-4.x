package ru.cg.runaex.components.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.bean.component.part.*;

/**
 * @author Абдулин Ильдар
 */
public class SphinxSearchParser {
  public static List<SphinxViewColumn> parseSphinxSearchColumn(String columnsStr, String defaultSchema) {
    if (columnsStr == null)
      return Collections.emptyList();

    String[] columnsArray = columnsStr.split(";");
    List<SphinxViewColumn> columns = new ArrayList<SphinxViewColumn>(columnsArray.length);

    for (String column : columnsArray) {
      column = StringUtils.trimToNull(column);
      if (column != null) {
        String[] parts = column.split(",");
        int termCount = parts.length;

        String viewName = StringUtils.trimToNull(parts[0]);
        ColumnReference reference = null;

        if (termCount > 1) {
          String referenceStr = StringUtils.trimToNull(parts[1]);
          reference = ComponentParser.parseColumnReference(referenceStr, defaultSchema);
        }

        columns.add(new SphinxViewColumn(viewName, reference));
      }
    }

    return columns;
  }

  public static List<SphinxIndexingColumn> parseSphinxIndexingColumns(String columnsStr, String defaultSchema) {
    if (columnsStr == null)
      return Collections.emptyList();

    String[] columnsArray = columnsStr.split(";");
    List<SphinxIndexingColumn> columns = new ArrayList<SphinxIndexingColumn>(columnsArray.length);

    for (String column : columnsArray) {
      column = StringUtils.trimToNull(column);
      if (column != null) {
        String referenceStr = StringUtils.trimToNull(column);
        ColumnReference reference = ComponentParser.parseColumnReference(referenceStr, defaultSchema);
        columns.add(new SphinxIndexingColumn(reference));
      }
    }

    return columns;
  }

  public static List<SphinxSaveIdColumn> parseSphinxColumnsId(String saveIdColumns, String defaultSchema) {

    if (saveIdColumns == null)
      return null;

    String[] columnsArray = saveIdColumns.split(";");

    List<SphinxSaveIdColumn> columns = new ArrayList<SphinxSaveIdColumn>(columnsArray.length);

    for (String column : columnsArray) {
      column = StringUtils.trimToNull(column);
      if (column != null) {
        String[] parts = column.split(",");
        String strReference = StringUtils.trimToNull(parts[0]);
        TableReference reference = ComponentParser.parseTableReference(strReference, defaultSchema);

        String columnName = null;
        if (parts.length > 1)
          columnName = StringUtils.trimToNull(parts[1]);
        columns.add(new SphinxSaveIdColumn(reference, columnName));
      }
    }

    return columns;
  }
}
