package ru.cg.runaex.validation;

/**
 * @author Bagautdinov
 */
public class MessagePart {
  private String title;
  private String message;
  private boolean isErrorMessage;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isErrorMessage() {
    return isErrorMessage;
  }

  public void setErrorMessage(boolean errorMessage) {
    isErrorMessage = errorMessage;
  }
}
