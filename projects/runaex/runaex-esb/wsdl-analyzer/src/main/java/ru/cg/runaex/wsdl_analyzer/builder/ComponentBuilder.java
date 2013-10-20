package ru.cg.runaex.wsdl_analyzer.builder;

import java.io.IOException;
import java.util.*;
import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.exolab.castor.xml.schema.*;
import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.cg.runaex.wsdl_analyzer.XMLSupport;
import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;
import ru.cg.runaex.wsdl_analyzer.bean.ServiceInfo;


/**
 * A class that defines methods for building components to invoke a web service
 * by analyzing a WSDL document.
 *
 * @author urmancheev
 */
public class ComponentBuilder {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";
  public final static String DEFAULT_STYLE = "document";

  private WSDLFactory wsdlFactory = null;
  private Schema wsdlTypes = null;

  /**
   * Builds a List of ServiceInfo components for each Service defined in a WSDL Document
   *
   * @param wsdlURI A URI that points to a WSDL XML Definition. Can be a filename or URL.
   * @return A List of SoapComponent objects populated for each service defined
   *         in a WSDL document. A null is returned if the document can't be read.
   */
  public Map<String, ServiceInfo> buildComponents(String wsdlURI) throws WSDLException {
    if (wsdlFactory == null)
      wsdlFactory = WSDLFactory.newInstance();
    WSDLReader reader = wsdlFactory.newWSDLReader();

    Definition wsdlDefinition = reader.readWSDL(null, wsdlURI);
    wsdlTypes = createSchemaFromTypes(wsdlDefinition);

    Map services = wsdlDefinition.getServices();
    Map<String, ServiceInfo> components = new HashMap<String, ServiceInfo>();
    if (services != null) {
      for (Object serviceObj : services.values()) {
        Service service = (Service) serviceObj;
        ServiceInfo serviceInfo = createServiceInfo(service);
        components.put(serviceInfo.getName(), serviceInfo);
      }
    }

    return components;
  }

  /**
   * Creates a castor schema based on the types defined by a WSDL document
   *
   * @param wsdlDefinition The WSDL4J instance of a WSDL definition.
   * @return A castor schema is returned if the WSDL definition contains
   *         a types element. null is returned otherwise.
   */
  protected Schema createSchemaFromTypes(Definition wsdlDefinition) {
    org.w3c.dom.Element schemaElement = null;

    if (wsdlDefinition.getTypes() != null) {
      ExtensibilityElement schemaExtElem = findExtensibilityElement(wsdlDefinition.getTypes().getExtensibilityElements(), "schema");
      if (schemaExtElem != null && schemaExtElem instanceof javax.wsdl.extensions.schema.Schema) {
        schemaElement = ((javax.wsdl.extensions.schema.Schema) schemaExtElem).getElement();
      }
    }

    if (schemaElement == null) {
      logger.error("Unable to find schema extensibility element in WSDL");
      return null;
    }

    // Convert from DOM to JDOM
    DOMBuilder domBuilder = new DOMBuilder();
    Element jdomSchemaElement = domBuilder.build(schemaElement);

    if (jdomSchemaElement == null) {
      logger.error("Unable to read schema defined in WSDL");
      return null;
    }

    // Add namespaces from the WSDL
    Map namespaces = wsdlDefinition.getNamespaces();
    if (namespaces != null && !namespaces.isEmpty()) {
      for (Object o : namespaces.keySet()) {
        String namespacePrefix = (String) o;
        String namespaceURI = (String) namespaces.get(namespacePrefix);

        if (namespacePrefix != null && namespacePrefix.length() > 0) {
          Namespace namespaceDeclaration = Namespace.getNamespace(namespacePrefix, namespaceURI);
          jdomSchemaElement.addNamespaceDeclaration(namespaceDeclaration);
        }
      }
    }

    // Make sure that the types element is not processed
    jdomSchemaElement.detach();

    // Convert it into a Castor schema instance
    Schema schema = null;
    try {
      schema = XMLSupport.convertElementToSchema(jdomSchemaElement);
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

    return schema;
  }

  /**
   * Creates a ServiceInfo instance from the specified Service definition
   *
   * @param service The Service to populate from
   * @return The populated component is returned representing the Service parameter
   */
  private ServiceInfo createServiceInfo(Service service) {
    QName qName = service.getQName();
    String namespaceUri = qName.getNamespaceURI();
    String name = qName.getLocalPart();

    ServiceInfo serviceInfo = new ServiceInfo();
    serviceInfo.setName(name);

    Map<String, OperationInfo> operationsByMethod = new HashMap<String, OperationInfo>();
    Map ports = service.getPorts();
    for (Object portObj : ports.values()) {
      Port port = (Port) portObj;
      Binding binding = port.getBinding();

      List<OperationInfo> operations = buildOperations(binding);
      for (OperationInfo operation : operations) {
        operation.setNamespaceURI(namespaceUri);

        // Find the SOAP target URL
        ExtensibilityElement addressElement = findExtensibilityElement(port.getExtensibilityElements(), "address");
        if (addressElement != null && addressElement instanceof SOAPAddress) {
          SOAPAddress soapAddress = (SOAPAddress) addressElement;
          operation.setTargetURL(soapAddress.getLocationURI());
        }

        operationsByMethod.put(operation.getTargetMethodName(), operation);
      }
    }
    serviceInfo.setOperationsByName(operationsByMethod);//todo is operation name unique within all ports/endpoints?

    return serviceInfo;
  }

  /**
   * Creates Info objects for each Binding Operation defined in a Port Binding
   *
   * @param binding The Binding that defines Binding Operations used to build info objects from
   * @return A List of built and populated OperationInfos is returned for each Binding Operation
   */
  private List<OperationInfo> buildOperations(Binding binding) {
    List<OperationInfo> operationInfos = new ArrayList<OperationInfo>();

    List operations = binding.getBindingOperations();
    if (operations != null && !operations.isEmpty()) {
      // Determine encoding
      ExtensibilityElement soapBindingElem = findExtensibilityElement(binding.getExtensibilityElements(), "binding");
      String style = DEFAULT_STYLE;

      if (soapBindingElem != null && soapBindingElem instanceof SOAPBinding) {
        SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
        style = soapBinding.getStyle();
      }

      for (Object operationObj : operations) {
        BindingOperation operation = (BindingOperation) operationObj;

        // We currently only support soap:operation bindings
        // filter out http:operations for now until we can dispatch them properly
        ExtensibilityElement operationElement = findExtensibilityElement(operation.getExtensibilityElements(), "operation");
        if (operationElement != null && operationElement instanceof SOAPOperation) {
          OperationInfo operationInfo = new OperationInfo(style);
          buildOperation(operationInfo, operation);
          operationInfos.add(operationInfo);
        }
      }
    }

    return operationInfos;
  }

  /**
   * Populates an OperationInfo from the specified Binding Operation
   *
   * @param operationInfo    The component to populate
   * @param bindingOperation A Binding Operation to define the OperationInfo from
   * @return The populated OperationInfo object is returned.
   */
  private OperationInfo buildOperation(OperationInfo operationInfo, BindingOperation bindingOperation) {
    Operation operation = bindingOperation.getOperation();
    operationInfo.setTargetMethodName(operation.getName());

    setOperationActionUri(operationInfo, bindingOperation);
    setOperationSoapBody(operationInfo, bindingOperation);
    setOperationInputMessage(operationInfo, operation);
    setOperationOutputMessage(operationInfo, operation);

    return operationInfo;
  }

  private void setOperationActionUri(OperationInfo operationInfo, BindingOperation bindingOperation) {
    ExtensibilityElement operationElement = findExtensibilityElement(bindingOperation.getExtensibilityElements(), "operation");
    if (operationElement != null && operationElement instanceof SOAPOperation) {
      SOAPOperation soapOperation = (SOAPOperation) operationElement;
      operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
    }
  }

  private void setOperationSoapBody(OperationInfo operationInfo, BindingOperation bindingOperation) {
    BindingInput bindingInput = bindingOperation.getBindingInput();
    ExtensibilityElement soapBodyElement = findExtensibilityElement(bindingInput.getExtensibilityElements(), "body");
    if (soapBodyElement != null && soapBodyElement instanceof SOAPBody) {
      SOAPBody soapBody = (SOAPBody) soapBodyElement;

      List soapEncodingStyles = soapBody.getEncodingStyles();
      String encodingStyle = null;
      if (soapEncodingStyles != null) {
        encodingStyle = soapEncodingStyles.get(0).toString();
      }
      if (encodingStyle == null) {
        encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
      }
      operationInfo.setEncodingStyle(encodingStyle);
      operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
    }
  }

  private void setOperationInputMessage(OperationInfo operationInfo, Operation operation) {
    Input operationInput = operation.getInput();
    if (operationInput != null) {
      Message inputMessage = operationInput.getMessage();
      if (inputMessage != null) {
        operationInfo.setInputMessageName(inputMessage.getQName().getLocalPart());
        operationInfo.setInputMessageStub(buildMessageText(operationInfo, inputMessage));
      }
    }
  }

  private void setOperationOutputMessage(OperationInfo operationInfo, Operation operation) {
    Output operationOutput = operation.getOutput();
    if (operationOutput != null) {
      Message outputMessage = operationOutput.getMessage();
      if (outputMessage != null) {
        operationInfo.setOutputMessageName(outputMessage.getQName().getLocalPart());
        operationInfo.setOutputMessageStub(buildMessageText(operationInfo, outputMessage));
      }
    }
  }

  /**
   * Builds and adds parameters to the supplied info object
   * given a SOAP Message definition (from WSDL)
   *
   * @param operationInfo The component to build message text for
   * @param message       The SOAP Message definition that has parts to defined parameters for
   */
  private String buildMessageText(OperationInfo operationInfo, Message message) {
    Element rootElement;
    List messageParts = message.getOrderedParts(null);
    if (messageParts.size() == 1 && ((Part) messageParts.get(0)).getElementName() != null) {
      rootElement = new Element(((Part) messageParts.get(0)).getElementName().getLocalPart());
    }
    else {
      rootElement = new Element(operationInfo.getTargetMethodName());
    }

    for (Object msgPart : messageParts) {
      Part part = (Part) msgPart;

      String partName = part.getName();
      if (partName != null) {
        XMLType xmlType = getXMLType(part);
        if (xmlType != null && xmlType.isComplexType()) {
          // Build the message structure

          buildComplexPart((ComplexType) xmlType, rootElement);
        }
        else {
          // Build the element that will be added to the message
          Element partElement = new Element(partName);
          setParameterPlaceholder(partElement);

          if ("rpc".equalsIgnoreCase(operationInfo.getStyle())) {
            partElement.setAttribute("type", part.getTypeName().getLocalPart());
          }

          rootElement.addContent(partElement);
        }
      }
    }

    return XMLSupport.outputString(rootElement);
  }

  /**
   * Populate a JDOM element using the complex XML type passed in
   *
   * @param complexType The complex XML type to build the element for
   * @param partElement The JDOM element to build content for
   */
  protected void buildComplexPart(ComplexType complexType, Element partElement) {
    Enumeration particleEnum = complexType.enumerate();
    Group group = null;

    while (particleEnum.hasMoreElements()) {
      Particle particle = (Particle) particleEnum.nextElement();
      if (particle instanceof Group) {
        group = (Group) particle;
        break;
      }
    }

    if (group != null) {
      Enumeration<Annotated> groupEnum = group.enumerate();

      while (groupEnum.hasMoreElements()) {
        Structure item = groupEnum.nextElement();

        if (item.getStructureType() == Structure.ELEMENT) {
          ElementDecl elementDecl = (ElementDecl) item;
          Element childElem = new Element(elementDecl.getName());
          XMLType xmlType = elementDecl.getType();

          if (xmlType != null && xmlType.isComplexType()) {
            buildComplexPart((ComplexType) xmlType, childElem);
          }
          else {
            setParameterPlaceholder(childElem);
          }

          partElement.addContent(childElem);
        }
      }
    }
  }

  protected XMLType getXMLType(Part part) {
    if (wsdlTypes == null) {
      // No defined types, Nothing to do
      return null;
    }

    XMLType xmlType = null;
    if (part.getElementName() != null) {
      String elementName = part.getElementName().getLocalPart();
      ElementDecl elementDeclaration = wsdlTypes.getElementDecl(elementName);
      if (elementDeclaration != null) {
        xmlType = elementDeclaration.getType();
      }
    }

    return xmlType;
  }

  private static ExtensibilityElement findExtensibilityElement(List extensibilityElements, String elementType) {
    if (extensibilityElements != null) {
      for (Object extensibilityElement : extensibilityElements) {
        ExtensibilityElement element = (ExtensibilityElement) extensibilityElement;
        if (element.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
          return element;
        }
      }
    }

    return null;
  }

  private void setParameterPlaceholder(Element element) {
    CDATA cdata = new CDATA("reference");
    element.addContent(cdata);
  }
}
