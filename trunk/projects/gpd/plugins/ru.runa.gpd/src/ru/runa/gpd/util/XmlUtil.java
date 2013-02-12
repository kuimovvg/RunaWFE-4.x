package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.osgi.framework.Bundle;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;

/**
 * Util for HTML with custom tags
 * 
 * @author Dofs
 */
public class XmlUtil {
    public static Document parseWithoutValidation(String data) {
        return parseWithoutValidation(data.getBytes(Charsets.UTF_8));
    }

    public static Document parseWithoutValidation(byte[] data) {
        return parse(new ByteArrayInputStream(data), false, null);
    }

    public static Document parseWithoutValidation(InputStream in) {
        return parse(in, false, null);
    }

    public static Document parseWithXSDValidation(InputStream in) {
        return parse(in, true, null);
    }

    public static Document parseWithXSDValidation(byte[] data) {
        return parseWithXSDValidation(new ByteArrayInputStream(data));
    }

    public static Document parseWithXSDValidation(String data) {
        return parseWithXSDValidation(data.getBytes(Charsets.UTF_8));
    }

    public static Document parseWithXSDValidation(InputStream in, InputStream xsdInputStream) {
        return parse(in, true, xsdInputStream);
    }

    private static Document parse(InputStream in, boolean xsdValidation, InputStream xsdInputStream) {
        try {
            SAXReader reader;
            if (xsdValidation) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                if (xsdInputStream != null) {
                    SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                    schemaFactory.setResourceResolver(new LSResourceResolver() {
                        @Override
                        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                            InputStream xsd = getClass().getResourceAsStream("/schema/" + systemId);
                            if (xsd != null) {
                                return new LSInputImpl(publicId, systemId, baseURI, xsd, Charsets.UTF_8.name());
                            }
                            return null;
                        }
                    });
                    factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(xsdInputStream) }));
                } else {
                    factory.setValidating(true);
                }
                SAXParser parser = factory.newSAXParser();
                if (xsdInputStream == null) {
                    parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                }
                reader = new SAXReader(parser.getXMLReader());
            } else {
                reader = new SAXReader();
            }
            reader.setValidation(xsdValidation && xsdInputStream == null);
            reader.setErrorHandler(SimpleErrorHandler.getInstance());
            return reader.read(new InputStreamReader(in, Charsets.UTF_8));
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static byte[] writeXml(Document document) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeXml(document, baos);
        return baos.toByteArray();
    }

    public static byte[] writeXml(Document document, OutputFormat outputFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeXml(document, baos, outputFormat);
        return baos.toByteArray();
    }

    public static void writeXml(Document document, OutputStream outputStream) {
        OutputFormat format = new OutputFormat("  ", true);
        format.setPadText(true);
        writeXml(document, outputStream, format);
    }

    public static void writeXml(Document document, OutputStream outputStream, OutputFormat outputFormat) {
        try {
            XMLWriter writer = new XMLWriter(outputStream, outputFormat);
            writer.write(document);
            writer.flush();
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String toString(Document document) {
        return new String(writeXml(document), Charsets.UTF_8);
    }

    public static String toString(Document document, OutputFormat outputFormat) {
        return new String(writeXml(document, outputFormat), Charsets.UTF_8);
    }

    public static Document createDocument(String rootElementName) {
        Document document = DocumentHelper.createDocument();
        document.addElement(rootElementName);
        return document;
    }

    public static String getParamDefConfig(Bundle bundle, String className) {
        int dotIndex = className.lastIndexOf(".");
        String simpleClassName = className.substring(dotIndex + 1);
        String path = "/conf/" + simpleClassName + ".xml";
        try {
            InputStream is = bundle.getEntry(path).openStream();
            return IOUtils.readStream(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config at " + path, e);
        }
    }
}
