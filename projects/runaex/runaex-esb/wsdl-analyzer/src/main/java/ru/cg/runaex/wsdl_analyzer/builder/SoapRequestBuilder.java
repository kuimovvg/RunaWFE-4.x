package ru.cg.runaex.wsdl_analyzer.builder;

import javax.xml.soap.*;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;

/**
 * @author urmancheev
 */
public class SoapRequestBuilder {
  public static final String XSI_NAMESPACE_PREFIX = "xsi";
  public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String XSD_NAMESPACE_PREFIX = "xsd";
  public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

  public static SOAPMessage buildMessage(OperationInfo operation, Document messageDocument) throws SOAPException, JDOMException {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage message = messageFactory.createMessage();

    SOAPPart soapPart = message.getSOAPPart();
    SOAPEnvelope envelope = soapPart.getEnvelope();
    String defaultNamespacePrefix = "";
    envelope.addNamespaceDeclaration(defaultNamespacePrefix, operation.getNamespaceURI());

    boolean isRPC = "rpc".equalsIgnoreCase(operation.getStyle());
    if (isRPC) {
      // Add namespace declarations to the envelope, usually only required for RPC/encoded
      envelope.addNamespaceDeclaration(XSI_NAMESPACE_PREFIX, XSI_NAMESPACE_URI);
      envelope.addNamespaceDeclaration(XSD_NAMESPACE_PREFIX, XSD_NAMESPACE_URI);
    }

    SOAPHeader header = envelope.getHeader();
    // The client does not yet support SOAP headers
    header.detachNode();

    SOAPBody envelopeBody = envelope.getBody();

    // Add the service information
    String targetObjectURI = operation.getTargetObjectURI();
    if (targetObjectURI == null) {
      // The target object URI should not be null
      targetObjectURI = "";
    }

    Name rootElementXmlName = envelope.createName(operation.getTargetMethodName(), defaultNamespacePrefix, targetObjectURI);
    SOAPElement rootElement = envelopeBody.addChildElement(rootElementXmlName);

    if (isRPC) {
      rootElement.setEncodingStyle(operation.getEncodingStyle());
    }

    if (messageDocument.hasRootElement()) {
      SoapElementBuilder.buildSoapElement(envelope, rootElement, messageDocument.getRootElement(), isRPC);
    }

    String soapActionURI = operation.getSoapActionURI();
    if (soapActionURI != null && soapActionURI.length() > 0) {
      // Add the SOAPAction value as a MIME header
      MimeHeaders mimeHeaders = message.getMimeHeaders();
      mimeHeaders.setHeader("SOAPAction", "\"" + operation.getSoapActionURI() + "\"");
    }

    message.saveChanges();
    return message;
  }
}
