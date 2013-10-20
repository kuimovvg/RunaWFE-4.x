package ru.cg.runaex.exceptions;

/**
 * @author tuhvatullin
 */
public class CantDoneTask extends Exception {
  public CantDoneTask() {
  }

  public CantDoneTask(String message) {
    super(message);
  }

  public CantDoneTask(String message, Throwable cause) {
    super(message, cause);
  }

  public CantDoneTask(Throwable cause) {
    super(cause);
  }
}
