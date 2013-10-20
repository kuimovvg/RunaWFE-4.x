package ru.cg.runaex.validation;

/**
 * @author urmancheev
 */
public class Problem {
  public final String message;
  public final Severity severity;

  private Problem(String message, Severity severity) {
    this.message = message;
    this.severity = severity;
  }

  public static Problem error(String message) {
    return new Problem(message, Severity.ERROR);
  }

  public static Problem warning(String message) {
    return new Problem(message, Severity.WARNING);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Problem problem = (Problem) o;

    if (message != null ? !message.equals(problem.message) : problem.message != null) return false;
    if (severity != problem.severity) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = message != null ? message.hashCode() : 0;
    result = 31 * result + (severity != null ? severity.hashCode() : 0);
    return result;
  }
}
