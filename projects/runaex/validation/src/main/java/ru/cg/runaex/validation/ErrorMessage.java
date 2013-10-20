package ru.cg.runaex.validation;

import java.util.List;

/**
 * @author Bagautdinov
 */
public class ErrorMessage {

  List<MessagePart> parts;

  public ErrorMessage(List<MessagePart> parts) {
    this.parts = parts;
  }

  public List<MessagePart> getParts() {
    return parts;
  }

  public void setParts(List<MessagePart> parts) {
    this.parts = parts;
  }

  @Override
  public String toString() {
    String str = null;
    if (!this.parts.isEmpty()) {
      for (MessagePart messagePart : this.parts) {
        if (messagePart.isErrorMessage())
        str = messagePart.getMessage();
      }
    }
    return str;
  }
}
