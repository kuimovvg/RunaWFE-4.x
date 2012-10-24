package org.jbpm.ui.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.PluginConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Util for HTML with custom tags
 * 
 * @author Dofs
 */
public class XmlUtil {

    public static Document parseDocument(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        return parseDocument(is, false, false, null);
    }

    public static Document parseDocumentValidateDTD(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        return parseDocument(is, true, false, null);
    }

    public static Document parseDocumentValidateXSD(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        return parseDocument(is, false, true, null);
    }

    private static Document parseDocument(InputStream is, boolean validateDTD, boolean validateXSD, String xsdSchemaName) throws SAXException,
            IOException, ParserConfigurationException {
        boolean validateAny = validateDTD || validateXSD;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // See http://xerces.apache.org/xerces2-j/features.html
        factory.setFeature("http://xml.org/sax/features/validation", validateAny);
        factory.setValidating(validateAny);

        // DTD validation
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", validateDTD);

        // XSD validation
        if (validateXSD) {
            /*
             * SchemaFactory schemaFactory =
             * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
             * schemaFactory.setErrorHandler(SimpleErrorHandler.getInstance());
             * Source schemaSource = new
             * StreamSource(XmlUtil.class.getResourceAsStream("/schema/" +
             * xsdSchemaName)); Schema schema =
             * schemaFactory.newSchema(schemaSource); factory.setSchema(schema);
             */
            // factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            // "http://www.w3.org/2001/XMLSchema");
            // factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
            // xsdSchemaUrl);
            factory.setFeature("http://apache.org/xml/features/validation/schema", true);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        }
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(SimpleErrorHandler.getInstance());
        documentBuilder.setEntityResolver(XmlEntityResolver.getInstance());
        return documentBuilder.parse(is);
    }

    public static Document createDocument(String rootElementName, String xsdSchema) throws ParserConfigurationException {
        return createDocument(rootElementName, xsdSchema, "http://runa.ru/xml");
    }

    public static Document createDocument(String rootElementName, String xsdSchema, String namespace) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(XmlEntityResolver.getInstance());
        Document document = builder.newDocument();
        Element root = document.createElement(rootElementName);
        if (xsdSchema != null) {
            root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, namespace);
            root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            root.setAttributeNS(XMLConstants.NULL_NS_URI, "xsi:schemaLocation", "http://runa.ru/xml " + xsdSchema);
        }
        document.appendChild(root);
        return document;
    }

    public static void writeXml(Document document, OutputStream os) throws TransformerException {
        try {
            OutputFormat format = new OutputFormat(document, PluginConstants.UTF_ENCODING, true);
            format.setIndent(2);
            Writer output = new BufferedWriter(new OutputStreamWriter(os, PluginConstants.UTF_ENCODING));
            XMLSerializer serializer = new XMLSerializer(output, format);
            serializer.serialize(document);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
    }

    public static byte[] writeXml(Document document) throws TransformerException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeXml(document, os);
        return os.toByteArray();
    }

    public static String normalize(String text) {
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll("'", "&apos;");
        text = text.replaceAll("\"", "&quot;");
        return text;
    }

    public static class XmlEntityResolver implements EntityResolver {

        private static final XmlEntityResolver instance = new XmlEntityResolver();

        private XmlEntityResolver() {
        }

        public static XmlEntityResolver getInstance() {
            return instance;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            InputStream in = null;
            if (systemId != null) {
                int index = systemId.lastIndexOf("/");
                String fileName = systemId.substring(index + 1);
                in = getClass().getResourceAsStream("/schema/" + fileName);
            }
            if (in != null) {
                return new InputSource(in);
            }
            DesignerLogger.logInfo("WARN: no entity resolved for systemId: " + systemId);
            return null;
        }
    }
}
