package ru.cg.runaex.components.validation.helper;

import java.util.Collection;

/**
 * @author urmancheev
 */
public final class FiasValidationHelper {

  public static void validateLevelCombination(String minLevel, String maxLevel, Collection<String> errorCodes) {
    if (!FiasValidationHelper.isLevelCombinationValid(minLevel, maxLevel))
      errorCodes.add("fias.minLevelGreaterThanMaxLevel");
  }

  public static boolean isLevelCombinationValid(String minLevel, String maxLevel) {
    if (minLevel == null || maxLevel == null)
      return true;

    try {
      int minLevelNumber = Integer.valueOf(minLevel);
      int maxLevelNumber = Integer.valueOf(maxLevel);
      return minLevelNumber <= maxLevelNumber;
    }
    catch (NumberFormatException ex) {
      // Validating only combination, not parameters themselves
      return true;
    }
  }


}
