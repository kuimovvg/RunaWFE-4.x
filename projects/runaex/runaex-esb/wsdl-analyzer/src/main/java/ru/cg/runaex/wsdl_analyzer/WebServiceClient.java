package ru.cg.runaex.wsdl_analyzer;

import javax.xml.messaging.URLEndpoint;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;
import ru.cg.runaex.wsdl_analyzer.builder.SoapRequestBuilder;

/**
 * @author urmancheev
 */
public class WebServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(WebServiceClient.class);

  public static Document sendRequest(OperationInfo requestOperation, String messageXml) throws WebServiceRequestException {
    try {
      Document document = XMLSupport.buildDocumentFromXml(messageXml);
      return sendRequest(requestOperation, document);
    }
    catch (JDOMException e) {
      logger.error(e.getMessage(), e);
      throw new WebServiceRequestException(e);
    }
  }

  public static Document sendRequest(OperationInfo requestOperation, Document message) throws WebServiceRequestException {
    try {
      return trySendRequest(requestOperation, message);
    }
    catch (JDOMException e) {
      logger.error(e.getMessage(), e);
      throw new WebServiceRequestException(e);
    }
    catch (SOAPException e) {
      logger.error(e.getMessage(), e);
      throw new WebServiceRequestException(e);
    }
    catch (SOAPFaultException e) {
      logger.error(e.getMessage(), e);
      throw new WebServiceRequestException(e);
    }
    catch (TransformerException e) {
      logger.error(e.getMessage(), e);
      throw new WebServiceRequestException(e);
    }
  }

  private static Document trySendRequest(OperationInfo requestOperation, Document message) throws JDOMException, SOAPException, SOAPFaultException, TransformerException {
    SOAPMessage soapMessage = SoapRequestBuilder.buildMessage(requestOperation, message);

    URLEndpoint endpoint = new URLEndpoint(requestOperation.getTargetURL());
    SOAPConnectionFactory conFactory = SOAPConnectionFactory.newInstance();
    SOAPConnection connection = conFactory.createConnection();

    SOAPMessage response;
    try {
      response = connection.call(soapMessage, endpoint);
    }
    finally {
      connection.close();
    }

    SOAPBody soapBody = response.getSOAPPart().getEnvelope().getBody();
    if (soapBody.hasFault()) {
      SOAPFault soapFault = soapBody.getFault();
      throw new SOAPFaultException(soapFault.getFaultCodeAsQName(), soapFault.getFaultString(), soapFault.getFaultActor(), soapFault.getDetail());
    }

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();

    org.w3c.dom.Node rootNode = soapBody.getFirstChild();
    while (rootNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE && rootNode.getNextSibling() != null)
      rootNode = rootNode.getNextSibling();

    Source responseContent = new DOMSource(rootNode);
    JDOMResult responseElement = new JDOMResult();
    transformer.transform(responseContent, responseElement);

    Document responseDocument = responseElement.getDocument();

    return responseDocument;
  }
}