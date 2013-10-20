package ru.cg.runaex.components.validation.helper;

import ru.cg.runaex.components.bean.component.part.ColumnReference;

/**
 * @author urmancheev
 */
public final class ColumnReferenceValidationHelper {

  public static boolean isValid(ColumnReference reference) {
    return reference.getTermCount() == 2 || reference.getTermCount() == 3;
  }
}
