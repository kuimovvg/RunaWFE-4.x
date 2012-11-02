/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot.webservice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wf.logic.bot.WebServiceTaskHandler;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;

/**
 * Class for parsing {@link WebServiceTaskHandler} settings from XML file.
 */
public class WebServiceTaskHandlerXMLParser {
    /**
     * XML element name: Web service URL or variable name to read web service
     * URL.
     */
    private static final String URL = "url";

    /**
     * XML element name: Value of SOAPAction HTTP header.
     */
    private static final String SOAP_ACTION = "SOAPAction";

    /**
     * XML element name: Action to be performed if error response received from
     * web service.
     */
    private static final String ERROR_ACTION = "errorAction";

    /**
     * XML element name: One interaction with web service.
     */
    private static final String INTERACTION = "interaction";

    /**
     * XML element name: Request, which must be send to web service as SOAP XML.
     */
    private static final String INTERACTION_REQUEST = "request";

    /**
     * XML element name: XSLT to be applied to web service response.
     */
    private static final String INTERACTION_RESPONSE = "response";

    /**
     * XML element name: Variable name to store response.
     */
    private static final String INTERACTION_VARIABLE = "variable";

    /**
     * XML element name: Maximum allowed response length.
     */
    private static final String INTERACTION_RESPONSE_LENGTH = "maxLength";

    /**
     * XML element name: Credentials, sends in authorization property of web
     * request for Basic authentication.
     */
    private static final String AUTH_BASE = "basic-authentication";

    /**
     * XML element name: HTTP Request method (GET, POST, PUT and so on).
     */
    private static final String REQUEST_METHOD = "request-method";

    /**
     * XML element name: Log request and response with debug priority if set to
     * true.
     */
    private static final String LOGGING = "log";

    /**
     * Path to XSD for validating settings.
     */
    private static final String XSD_PATH = "/webServiceTaskHandlerConfig.xsd";

    /**
     * Allows XML parser to resolve external entities (XSD).
     */
    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    /**
     * Read XML from specified stream and create
     * {@link WebServiceTaskHandlerSettings} instance according to XML.
     * 
     * @param data
     *            Stream with XML to read.
     * @return Instance of {@link WebServiceTaskHandlerSettings} created
     *         according to XML.
     */
    public static WebServiceTaskHandlerSettings read(InputStream data) {
        try {
            Document document = XMLHelper.getDocument(data, PATH_ENTITY_RESOLVER);
            Element configuration = document.getDocumentElement();
            String url = getElementText(configuration, URL);
            String soapAction = getElementText(configuration, SOAP_ACTION);
            ErrorResponseProcessingResult errorAction = readErrorAction(configuration);
            String authBase = getElementText(configuration, AUTH_BASE);
            String requestMethod = getElementText(configuration, REQUEST_METHOD);
            boolean isLoggingEnable = "true".equalsIgnoreCase(getElementText(configuration, LOGGING));
            List<Interaction> interactions = readInteractions(configuration.getElementsByTagName(INTERACTION));
            return new WebServiceTaskHandlerSettings(url, soapAction, interactions, document.getXmlEncoding(), authBase, requestMethod,
                    isLoggingEnable, errorAction);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    /**
     * Read interactions from elements.
     * 
     * @param interactionsElements
     *            List of XML elements, describing interactions.
     * @return List of readed interactions.
     */
    private static List<Interaction> readInteractions(NodeList interactionsElements) {
        List<Interaction> result = new ArrayList<Interaction>();
        for (int i = 0; i < interactionsElements.getLength(); ++i) {
            Element interaction = (Element) interactionsElements.item(i);
            String requestXML = getElementText(interaction, INTERACTION_REQUEST);
            String responseXSLT = getElementText(interaction, INTERACTION_RESPONSE);
            String variableName = getElementAttribute(interaction, INTERACTION_RESPONSE, INTERACTION_VARIABLE);
            String maxLength = getElementAttribute(interaction, INTERACTION_RESPONSE, INTERACTION_RESPONSE_LENGTH);
            int maxResponseLength = maxLength == null ? 128 * 1024 : Integer.parseInt(maxLength);
            if (maxResponseLength < 0) {
                maxResponseLength = 128 * 1024;
            }
            ErrorResponseProcessingResult errorAction = readErrorAction(interaction);
            result.add(new Interaction(requestXML, responseXSLT, errorAction, maxResponseLength, variableName));
        }
        return result;
    }

    /**
     * Read element with specified name and returns it text content.
     * 
     * @param parentElement
     *            Parent element to read element with specified name.
     * @param elementName
     *            Element name to read.
     * @return Text content of element with specified name or null, if no such
     *         element found.
     */
    private static String getElementText(Element parentElement, String elementName) {
        NodeList elements = parentElement.getElementsByTagName(elementName);
        if (elements.getLength() == 0) {
            return null;
        }
        return elements.item(0).getTextContent();
    }

    /**
     * Read element with specified name and returns it text content.
     * 
     * @param parentElement
     *            Parent element to read element with specified name.
     * @param elementName
     *            Element name to read.
     * @param attributeName
     *            Attribute name to read
     * @return Text content of element with specified name or null, if no such
     *         element found.
     */
    private static String getElementAttribute(Element parentElement, String elementName, String attributeName) {
        NodeList elements = parentElement.getElementsByTagName(elementName);
        if (elements.getLength() == 0) {
            return null;
        }
        Node attribute = elements.item(0).getAttributes().getNamedItem(attributeName);
        return attribute == null ? null : attribute.getNodeValue();
    }

    /**
     * Convert errorAction element in configuration to
     * {@link ErrorResponseProcessingResult}.
     * 
     * @param configuration
     *            Element to read errorAction element.
     * @return Converted to {@link ErrorResponseProcessingResult} errorAction or
     *         null, if errorAction is not set.
     */
    private static ErrorResponseProcessingResult readErrorAction(Element configuration) {
        NodeList actionElement = configuration.getElementsByTagName(ERROR_ACTION);
        if (actionElement.getLength() == 0) {
            return null;
        }
        return ErrorResponseProcessingResult.valueOf(actionElement.item(0).getTextContent());
    }
}
