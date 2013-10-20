package ru.cg.runaex.wsdl_analyzer.builder;

import java.util.List;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * @author urmancheev
 */
public class SoapElementBuilder {
  public static final String XSI_NAMESPACE_PREFIX = "xsi";
  public static final String XSD_NAMESPACE_PREFIX = "xsd";

  /**
   * Builds a hierarchy of SOAPElements given a complex value JDOM Element
   *
   * @param envelope       The SOAP Envelope
   * @param rootElement    The root SOAP Element to build content for
   * @param xmlComplexType A JDOM Element that represents a complex value
   * @param isRPC          Pass true when building for RPC encoded messages
   * @throws javax.xml.soap.SOAPException
   */
  public static void buildSoapElement(SOAPEnvelope envelope, SOAPElement rootElement, Element xmlComplexType, boolean isRPC) throws SOAPException {
    setElementTextValue(envelope, rootElement, xmlComplexType, isRPC);
    setElementAttributes(envelope, rootElement, xmlComplexType);
    setElementChildren(envelope, rootElement, xmlComplexType, isRPC);
  }

  private static void setElementTextValue(SOAPEnvelope envelope, SOAPElement rootElement, Element xmlComplexType, boolean isRPC) throws SOAPException {
    String elemText = xmlComplexType.getText();
    if (elemText == null)
      return;

    if (isRPC) {
      String typeAttribute = xmlComplexType.getAttributeValue("type");
      if (typeAttribute != null) {
        rootElement.addAttribute(envelope.createName(XSI_NAMESPACE_PREFIX + ":type"), XSD_NAMESPACE_PREFIX + ":" + typeAttribute);
      }
    }

    rootElement.addTextNode(elemText);
  }

  private static void setElementAttributes(SOAPEnvelope envelope, SOAPElement rootElement, Element xmlComplexType) throws SOAPException {
    List<Attribute> attributes = xmlComplexType.getAttributes();
    if (attributes == null)
      return;

    for (Attribute attribute : attributes) {
      Name attrName = envelope.createName(attribute.getName(), attribute.getNamespacePrefix(), attribute.getNamespaceURI());
      rootElement.addAttribute(attrName, attribute.getValue());
    }
  }

  private static void setElementChildren(SOAPEnvelope envelope, SOAPElement rootElement, Element xmlComplexType, boolean isRPC) throws SOAPException {
    List<Element> childTypes = xmlComplexType.getChildren();
    if (childTypes == null)
      return;

    for (Element childType : childTypes) {
      SOAPElement childElement = rootElement.addChildElement(childType.getName());
      buildSoapElement(envelope, childElement, childType, isRPC);
    }
  }
}
