package ru.cg.runaex.web.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author urmancheev
 */
public final class SAXUtils {
  private static Logger logger = LoggerFactory.getLogger(SAXUtils.class);
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  public static Document getDocument(String xml) throws IOException, SAXException, ParserConfigurationException {
    Document doc = null;
    InputStream input = null;
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = docFactory.newDocumentBuilder();
      input = new ByteArrayInputStream(xml.getBytes(UTF_8));
      doc = builder.parse(input);
    }
    catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
      throw ex;
    }
    catch (SAXException ex) {
      logger.error(ex.getMessage(), ex);
      throw ex;
    }
    finally {
      IOUtils.closeQuietly(input);
    }
    return doc;
  }
}
