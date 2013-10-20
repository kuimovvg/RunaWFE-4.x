package ru.cg.runaex.core;

import java.text.SimpleDateFormat;

/**
 * @author urmancheev
 */
public final class DateFormat {
  public static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";
  public static final String DATE_TIME_FORMAT_PATTERN = "dd.MM.yyyy HH:mm:ss";

  public static SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat(DATE_FORMAT_PATTERN);
  }

  public static SimpleDateFormat getDateTimeFormat() {
    return new SimpleDateFormat(DATE_TIME_FORMAT_PATTERN);
  }
}
