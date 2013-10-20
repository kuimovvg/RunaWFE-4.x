package ru.cg.runaex.components.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.bean.ColumnMask;
import ru.cg.runaex.components.bean.component.part.*;

/**
 * @author urmancheev
 */
public final class ComponentParser {

  private static final Pattern NOT_EMPTY_REFERENCE = Pattern.compile("[^.]");

  public static TableReference parseTableReference(String strReference, String defaultSchema) {
    TableReference reference = null;

    if (strReferenceIsntEmpty(strReference)) {
      String[] parts = strReference.split("\\.");

      String schema = null;
      String table = null;
      int termCount = parts.length;

      if (termCount > 1) {
        schema = StringUtils.trimToNull(parts[0]);
        table = StringUtils.trimToNull(parts[1]);
      }
      else {
        table = StringUtils.trimToNull(parts[0]);
      }

      if (schema == null)
        schema = defaultSchema;
      reference = new TableReference(schema, table, termCount);
    }

    return reference;
  }

  public static TableReference parseTableReferenceInitTerm(String strReference, String defaultSchema) {
    TableReference reference;
    if (strReferenceIsntEmpty(strReference))
      reference = parseTableReference(strReference, defaultSchema);
    else
      reference = new TableReference(null, null, 0);

    return reference;
  }

  public static DefaultValue parseDefaultValue(String strValue) {
    DefaultValue defaultValue = null;

    if (strValue != null) {
      String typeStr = strValue.substring(0, strValue.indexOf("."));
      String valueStr = strValue.substring(strValue.indexOf(".") + 1);
      String defaultValueTypeStr = StringUtils.trimToNull(typeStr);
      String defaultValueStr = StringUtils.trimToNull(valueStr);
      DefaultValueType defaultValueType = DefaultValueType.NONE;

      if (defaultValueTypeStr != null) {
        defaultValueType = DefaultValueType.valueOf(defaultValueTypeStr);
      }

      if (defaultValueStr != null && defaultValueType == DefaultValueType.EXECUTE_GROOVY && Base64.isBase64(defaultValueStr)) {
        defaultValueStr = parseBase64(defaultValueStr);
      }
      defaultValue = new DefaultValue(defaultValueType, defaultValueStr);
    }
    return defaultValue;
  }

  public static ColumnReference parseColumnReference(String strReference, String defaultSchema) {
    ColumnReference reference = null;

    if (strReferenceIsntEmpty(strReference)) {
      String[] parts = strReference.split("\\.");

      String schema = null;
      String table = null;
      String column = null;
      int termCount = parts.length;

      if (termCount > 2) {
        schema = StringUtils.trimToNull(parts[0]);
        table = StringUtils.trimToNull(parts[1]);
        column = StringUtils.trimToNull(parts[2]);
      }
      else if (termCount == 2) {
        table = StringUtils.trimToNull(parts[0]);
        column = StringUtils.trimToNull(parts[1]);
      }

      if (schema == null)
        schema = defaultSchema;
      reference = new ColumnReference(schema, table, column, termCount);
    }

    return reference;
  }

  public static ColumnReference parseColumnReferenceInitTerm(String strReference, String defaultSchema) {
    ColumnReference reference;
    if (strReferenceIsntEmpty(strReference))
      reference = parseColumnReference(strReference, defaultSchema);
    else
      reference = new ColumnReference(null, null, null, 0);

    return reference;
  }

  public static List<GridColumn> parseGridColumns(String columnsStr, String defaultSchema) {
    if (columnsStr == null)
      return Collections.emptyList();

    String[] columnsArray = columnsStr.split(";");
    List<GridColumn> columns = new ArrayList<GridColumn>(columnsArray.length);

    for (String column : columnsArray) {
      column = StringUtils.trimToNull(column);
      if (column != null) {
        String[] parts = column.split(",");
        int termCount = parts.length;

        String viewName = StringUtils.trimToNull(parts[0]);
        String databaseName = null;
        ColumnReference reference = null;
        String width = null;

        if (termCount > 1){
          databaseName = StringUtils.trimToNull(parts[1]);
        }

        if (termCount > 2) {
          String referenceStr = StringUtils.trimToNull(parts[2]);
          reference = parseColumnReference(referenceStr, defaultSchema);
        }

        if (termCount > 3) {
          width = StringUtils.trimToNull(parts[3]);
        }

        if (databaseName == null && viewName != null){
          databaseName = viewName;
        }

        columns.add(new GridColumn(viewName, databaseName, reference, termCount, width));
      }
    }

    return columns;
  }

  public static ComparisonType convertComparisonType(String code) {
    if ("eq".equals(code))
      return ComparisonType.EQUALS;
    if ("ge".equals(code))
      return ComparisonType.GREATER_EQUALS;
    if ("gt".equals(code))
      return ComparisonType.GREATER;
    if ("le".equals(code))
      return ComparisonType.LESS_EQUALS;
    if ("lt".equals(code))
      return ComparisonType.LESS;
    return null;
  }

  public static DateType convertDateType(String valueTypeStr) {
    if (GpdRunaConfigComponent.DATE_TYPE.equals(valueTypeStr))
      return DateType.DATE;
    if (GpdRunaConfigComponent.DATE_TIME_TYPE.equals(valueTypeStr))
      return DateType.DATE_TIME;
    return null;
  }

  public static TextType convertTextType(String valueTypeStr) {
    if (GpdRunaConfigComponent.TEXT2_TYPE.equals(valueTypeStr))
      return TextType.TEXT;
    if (GpdRunaConfigComponent.TEXT_AREA_TYPE.equals(valueTypeStr))
      return TextType.TEXT_AREA;
    if (GpdRunaConfigComponent.NUMBER_TYPE.equals(valueTypeStr))
      return TextType.NUMBER;
    if (GpdRunaConfigComponent.EMAIL.equals(valueTypeStr))
      return TextType.EMAIL;
    return null;
  }

  public static MaskType convertMaskType(String maskType) {
    if (GpdRunaConfigComponent.MASK_MONETARY.equals(maskType))
      return MaskType.MONETARY;
    if (GpdRunaConfigComponent.MASK_MONETARY_EXTENDED.equals(maskType))
      return MaskType.MONETARY_EXTENDED;
    if (GpdRunaConfigComponent.MASK_MANUAL.equals(maskType))
      return MaskType.MANUAL;
    if (GpdRunaConfigComponent.MASK_NONE.equals(maskType))
      return MaskType.NONE;
    return null;
  }

  public static RequireRuleComponentPart parseRequireRule(String value) {
    if (value != null) {
      if (GpdRunaConfigComponent.REQUIRED.equals(value)) {
        return new RequireRuleComponentPart(true);
      }
      else if (GpdRunaConfigComponent.NOT_REQUIRED.equals(value)) {
        return new RequireRuleComponentPart(false);
      }
      else {
        //It's Groovy script
        if (Base64.isBase64(value)) {
          value = parseBase64(value);
        }
        return new RequireRuleComponentPart(value);
      }
    }

    return new RequireRuleComponentPart(false);
  }

  public static GroovyRuleComponentPart parseGroovyRule(String value) {
    if (value != null) {
      return new GroovyRuleComponentPart(parseBase64(value));
    }

    return null;
  }

  public static VisibilityRuleComponentPart parseVisibilityRule(String value) {
    if (value != null) {
      return new VisibilityRuleComponentPart(parseBase64(value));
    }

    return null;
  }

  public static EditabilityRuleComponentPart parseEditabilityRule(String value) {
    if (value != null) {
      return new EditabilityRuleComponentPart(parseBase64(value));
    }

    return null;
  }

  public static String parseBase64(String base64String) {
    if (base64String == null || base64String.isEmpty())
      return null;
    return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.decodeBase64(base64String));
  }

  private static boolean strReferenceIsntEmpty(String strReference) {
    return strReference != null && NOT_EMPTY_REFERENCE.matcher(strReference).find();
  }

  public static List<EditableTreeGridColumn> parseEditableTreeGridColumns(String gridColumnsStr, String defaultSchema) {
    if (gridColumnsStr == null)
      return Collections.emptyList();

    String[] columnsArray = gridColumnsStr.split(";");
    List<EditableTreeGridColumn> columns = new ArrayList<EditableTreeGridColumn>(columnsArray.length);

    for (String column : columnsArray) {
      column = StringUtils.trimToNull(column);
      if (column != null) {
        String[] parts = column.split(",");
        int termCount = parts.length;

        String viewName = StringUtils.trimToNull(parts[0]);
        String databaseName = null;
        ColumnReference reference = null;
        String columnFormatStr = String.valueOf(ColumnMask.EMPTY);
        String width = null;

        if (termCount > 1){
          databaseName = StringUtils.trimToNull(parts[1]);
        }
        if (termCount > 2) {
          String referenceStr = StringUtils.trimToNull(parts[2]);
          reference = parseColumnReference(referenceStr, defaultSchema);
        }
        if (termCount > 3){
          width = StringUtils.trimToNull(parts[3]);
        }
        if (termCount > 4) {
          columnFormatStr = StringUtils.trimToNull(parts[4]);
        }
        if (databaseName == null && viewName != null){
          databaseName = viewName;
        }

        columns.add(new EditableTreeGridColumn(viewName, databaseName, reference, termCount, Integer.valueOf(columnFormatStr), width));
      }
    }

    return columns;
  }
}
