package ru.cg.runaex.wsdl_analyzer;

/**
 * @author urmancheev
 */
public class WebServiceRequestException extends Exception {
  private static final long serialVersionUID = -6736138783157062427L;

  public WebServiceRequestException() {
  }

  public WebServiceRequestException(String message) {
    super(message);
  }

  public WebServiceRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public WebServiceRequestException(Throwable cause) {
    super(cause);
  }
}
