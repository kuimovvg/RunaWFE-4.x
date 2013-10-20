package ru.cg.runaex.components;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Петров А.
 */
public final class FilterExpressions {

  public static final String CURDATE = "curdate";

  /**
   * Parses filter expressions
   *
   * @param exp string expression
   * @return Parsed expression if it's correct or unparsed string if it's not correct
   */
  public static Object parseExpression(String exp) {
    if (exp == null) {
      return null;
    }

    if (exp.contains(CURDATE)) {
      return parseCurdateExpression(exp);
    }

    return exp;
  }

  private static Object parseCurdateExpression(String exp) {
    if (exp.equals(CURDATE)) {
      return new Date();
    }
    else if (exp.startsWith(CURDATE)) {
      String originalExp = exp;
      exp = exp.replaceFirst(CURDATE, "");
      char operator = exp.charAt(0);
      exp = exp.substring(1, exp.length());
      int operand = Integer.parseInt(exp);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      switch (operator) {
        case '+':
          calendar.add(Calendar.DAY_OF_MONTH, operand);
          return calendar.getTime();
        case '-':
          calendar.add(Calendar.DAY_OF_MONTH, -operand);
          return calendar.getTime();
      }
      return originalExp;
    }

    return exp;
  }
}
