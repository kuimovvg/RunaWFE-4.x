package ru.cg.runaex.components.parser;

import ru.cg.runaex.components.bean.component.part.FiasObjectLevel;

/**
 * @author urmancheev
 */
public class FiasObjectLevelParser {

  public static FiasObjectLevel convertObjectLevel(String levelStr) {
    if (levelStr == null)
      return null;

    try {
      int level = Integer.valueOf(levelStr);
      switch (level) {
        case 1:
          return FiasObjectLevel.REGION;
        case 2:
          return FiasObjectLevel.DISTRICT;
        case 3:
          return FiasObjectLevel.CITY;
        case 4:
          return FiasObjectLevel.STREET;
        default:
          return null;
      }
    }
    catch (NumberFormatException ex) {
      return null;
    }
  }
}
