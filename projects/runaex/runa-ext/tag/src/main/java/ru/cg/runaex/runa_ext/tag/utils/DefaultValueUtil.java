package ru.cg.runaex.runa_ext.tag.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.var.IVariableProvider;

import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.runa_ext.tag.exception.DefaultValueGettingException;
import ru.cg.runaex.components.exception.InvalidGroovyResultException;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.core.DateFormat;

/**
 * @author Kochetkov
 */
public final class DefaultValueUtil {

  public static Object getDefaultValue(DefaultValue defaultValue, IVariableProvider variableProvider, Class... requiredTypes) throws DefaultValueGettingException {
    if (defaultValue == null) {
      return null;
    }

    Object value = null;
    try {
      switch (defaultValue.getType()) {
        case MANUAL:
          value = defaultValue.getValue();
          break;
        case FROM_DB:
          value = ComponentParser.parseColumnReference(defaultValue.getValue(), null);
          break;
        case EXECUTE_GROOVY:
          String groovyCode = defaultValue.getValue();
          GroovyScriptExecutor groovyScriptExecutor = new GroovyScriptExecutor();
          value = groovyScriptExecutor.evaluateScript(null, variableProvider, groovyCode);  //TODO: set the processDefinition
          boolean resultHasCorrectType = value == null || (requiredTypes != null && isRequiredType(value, requiredTypes) || isSimpleType(value));

          if (!resultHasCorrectType) {
            throw new InvalidGroovyResultException();
          }
          break;
        default:
          break;
      }
    }
    catch (Exception ex) {
      throw new DefaultValueGettingException(ex);
    }

    return value;
  }

  private static boolean isSimpleType(Object value) {
    return ((value instanceof String) || (value instanceof Number) || (value instanceof Date));
  }

  private static boolean isRequiredType(Object value, Class<?>... requiredTypes) {
    for (Class<?> requiredType : requiredTypes) {
      if (requiredType.isAssignableFrom(value.getClass())) {
        return true;
      }
      if (value instanceof String) {
        if (Date.class.isAssignableFrom(requiredType)) {
          boolean isStrDate = true;
          String valueStr = (String) value;
          try {
            DateFormat.getDateFormat().parse(valueStr);
          }
          catch (ParseException e) {
            try {
              DateFormat.getDateTimeFormat().parse(valueStr);
            }
            catch (ParseException e1) {
              isStrDate = false;
            }
          }
          if (isStrDate) {
            return true;
          }
        }
        else if (Number.class.isAssignableFrom(requiredType)) {
          String valueStr = (String) value;
          NumberFormat formatter = NumberFormat.getInstance();
          ParsePosition pos = new ParsePosition(0);
          formatter.parse(valueStr, pos);
          if (valueStr.length() == pos.getIndex()) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
