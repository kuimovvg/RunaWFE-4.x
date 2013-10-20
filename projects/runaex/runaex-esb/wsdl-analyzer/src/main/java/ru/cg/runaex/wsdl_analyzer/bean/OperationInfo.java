package ru.cg.runaex.wsdl_analyzer.bean;


/**
 * Defines an in memory model to support a SOAP invocation
 *
 * @author urmancheev
 */
public class OperationInfo {
  /**
   * The SOAP operation type
   */
  private String operationType = "";
  private String encodingStyle = "";
  private String targetURL = "";
  private String namespaceURI = "";

  private String targetObjectURI = "";
  private String targetMethodName = "";
  private String inputMessageName = "";
  private String outputMessageName = "";
  private String soapActionURI = "";

  private String inputMessageStub;
  private String outputMessageStub;

  /**
   * The encoding type "document" vs. "rpc"
   */
  private String style = "document";

  public OperationInfo(String style) {
    setStyle(style);
  }

  public void setEncodingStyle(String value) {
    encodingStyle = value;
  }

  public String getEncodingStyle() {
    return encodingStyle;
  }

  public void setTargetURL(String value) {
    targetURL = value;
  }

  public String getTargetURL() {
    return targetURL;
  }

  public void setNamespaceURI(String value) {
    namespaceURI = value;
  }

  public String getNamespaceURI() {
    return namespaceURI;
  }

  public void setTargetObjectURI(String value) {
    targetObjectURI = value;
  }

  public String getTargetObjectURI() {
    return targetObjectURI;
  }

  public void setTargetMethodName(String value) {
    targetMethodName = value;
  }

  public String getTargetMethodName() {
    return targetMethodName;
  }

  public void setInputMessageName(String value) {
    inputMessageName = value;
  }

  public String getInputMessageName() {
    return inputMessageName;
  }

  public void setOutputMessageName(String value) {
    outputMessageName = value;
  }

  public String getOutputMessageName() {
    return outputMessageName;
  }

  public void setSoapActionURI(String value) {
    soapActionURI = value;
  }

  public String getSoapActionURI() {
    return soapActionURI;
  }

  public String getInputMessageStub() {
    return inputMessageStub;
  }

  public void setInputMessageStub(String inputMessageStub) {
    this.inputMessageStub = inputMessageStub;
  }

  public String getOutputMessageStub() {
    return outputMessageStub;
  }

  public void setOutputMessageStub(String outputMessageStub) {
    this.outputMessageStub = outputMessageStub;
  }

  public void setStyle(String value) {
    style = value;
  }

  public String getStyle() {
    return style;
  }

  public String toString() {
    return getTargetMethodName();
  }
}
