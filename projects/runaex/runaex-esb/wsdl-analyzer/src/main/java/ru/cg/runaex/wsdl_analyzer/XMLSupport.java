package ru.cg.runaex.wsdl_analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.exolab.castor.xml.schema.writer.SchemaWriter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * XML support and utilities
 *
 * @author Bikash Behera
 * @author Jim Winfield
 */

public class XMLSupport {
  private static final Logger logger = LoggerFactory.getLogger(XMLSupport.class);

  private XMLSupport() {
  }

  /**
   * Returns a string representation of the given jdom document.
   *
   * @param doc The jdom document to be converted into a string.
   * @return Ths string representation of the given jdom document.
   */
  public static String outputString(Document doc) {
    XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
    return xmlWriter.outputString(doc);
  }

  /**
   * Returns a string representation of the given jdom element.
   *
   * @param elem The jdom element to be converted into a string.
   * @return The string representation of the given jdom element.
   */
  public static String outputString(Element elem) {
    XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
    return xmlWriter.outputString(elem);
  }

  /**
   * It reads the given xml returns the jdom document.
   *
   * @param xml The xml to read.
   * @return The jdom document created from the xml.
   * @throws org.jdom2.JDOMException If the parsing failed.
   */
  public static Document buildDocumentFromXml(String xml) throws JDOMException {
    try {
      return readXML(new StringReader(xml));
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new JDOMException("Xml parsing failed", e);
    }
  }

  /**
   * It reads the given xml reader and returns the jdom document.
   *
   * @param reader The xml reader to read.
   * @return The jdom document created from the xml reader.
   * @throws org.jdom2.JDOMException If the parsing failed.
   */
  public static Document readXML(Reader reader) throws JDOMException, IOException {
    SAXBuilder xmlBuilder = new SAXBuilder(XMLReaders.NONVALIDATING, null, null);
    return xmlBuilder.build(reader);
  }

  /**
   * It reads the given reader and returns the castor schema.
   *
   * @param reader The reader to read.
   * @return The castor schema created from the reader.
   * @throws java.io.IOException If the schema could not be read from the reader.
   */
  public static Schema readSchema(Reader reader) throws IOException {
    InputSource inputSource = new InputSource(reader);
    SchemaReader schemaReader = new SchemaReader(inputSource);
    schemaReader.setValidation(false);
    return schemaReader.read();
  }

  /**
   * Converts a castor schema into a jdom element.
   *
   * @param schema The castor schema to be converted.
   * @return The jdom element representing the schema.
   * @throws org.xml.sax.SAXException If the castor schema could not be written out.
   * @throws java.io.IOException      If the castor schema could not be written out.
   * @throws org.jdom2.JDOMException  If the output of the castor schema could not be converted into a jdom element.
   */
  public static Element convertSchemaToElement(Schema schema) throws SAXException, IOException, JDOMException {
    String content = outputString(schema);

    if (content != null) {
      Document doc = readXML(new StringReader(content));
      return doc.getRootElement();
    }
    return null;
  }

  /**
   * Converts the jdom element into a castor schema.
   *
   * @param element The jdom element to be converted into a castor schema.
   * @return The castor schema corresponding to the element.
   * @throws java.io.IOException If the jdom element could not be written out.
   */
  public static Schema convertElementToSchema(Element element) throws IOException {
    String content = outputString(element);

    if (content != null) {
      return readSchema(new StringReader(content));
    }

    return null;
  }

  /**
   * Returns a string representation of the given castor schema.
   *
   * @param schema The castor schema to be converted into a string.
   * @return The string representation of the given castor schema.
   * @throws java.io.IOException      If the schema could not be written out.
   * @throws org.xml.sax.SAXException If the schema could not be written out.
   */
  public static String outputString(Schema schema) throws IOException, SAXException {
    StringWriter writer = new StringWriter();
    SchemaWriter schemaWriter = new SchemaWriter(writer);
    schemaWriter.write(schema);
    return writer.toString();
  }
}