package ru.cg.runaex.components.validation.helper;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.parser.ComponentParser;

/**
 * @author Kochetkov
 */
public final class DefaultValueValidationHelper {

  public static boolean isValid(DefaultValue defaultValue) {
    boolean valid = true;
    switch (defaultValue.getType()) {
      case FROM_DB:
        ColumnReference columnReference = ComponentParser.parseColumnReference(defaultValue.getValue(), null);
        valid = ColumnReferenceValidationHelper.isValid(columnReference);
        break;
      case EXECUTE_GROOVY:
        String groovyCode = defaultValue.getValue();
        valid = GroovyScriptValidationHelper.isValid(groovyCode);
        break;
      default:
        break;
    }

    return valid;
  }

}
