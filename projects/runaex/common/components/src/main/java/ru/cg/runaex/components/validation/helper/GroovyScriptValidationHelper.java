package ru.cg.runaex.components.validation.helper;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

/**
 * @author Kochetkov
 */
public class GroovyScriptValidationHelper {
  public static boolean isValid(String groovyCode) {
    boolean valid = true;
    try {
      new GroovyShell().parse(groovyCode);
    }
    catch (MultipleCompilationErrorsException cfe) {
      valid = false;
    }
    return valid;
  }
}