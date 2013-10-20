package ru.cg.runaex.components.validation;

import java.util.regex.Pattern;

/**
 * @author urmancheev
 */
public final class ValidationConstants {
  public static final Pattern EMAIL_PATTERN = Pattern.compile("^(\\S+)@([a-zA-Z0-9-]+)(\\.)([a-zA-Z]{2,4})(\\.?)([a-zA-Z]{0,4})+$");

}
