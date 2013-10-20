package ru.cg.runaex.database.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ru.cg.runaex.database.bean.ParFile;
import ru.cg.runaex.database.bean.WbaFile;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;
import ru.cg.runaex.shared.bean.project.xml.Project;
import ru.cg.runaex.exceptions.BusinessProcessException;
import ru.cg.runaex.exceptions.ProjectParseException;
import ru.cg.runaex.shared.util.XmlUtils;
import ru.cg.runaex.web.utils.SAXUtils;

/**
 * @author urmancheev
 */
public final class WbaParser {
  private static final String PROJECT_STRUCTURE_DESCRIPTOR_FILENAME = "structure.xml";
  private static final String PROJECT_DATA_SOURCE_FILENAME = "datasource.xml";
  private static final String PROJECT_GROOVY_FUNCTIONS_FILENAME = "groovy_functions.xml";

  public static WbaFile processFile(MultipartFile multipartFile, ResourceBundleMessageSource messageSource) throws IOException, ProjectParseException, BusinessProcessException {
    WbaFile request = new WbaFile();
    byte[] archiveContent = multipartFile.getBytes();

    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archiveContent));
    String entryName;
    try {
      for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
        entryName = entry.getName();
        if (entryName.endsWith(".par")) {
          request.addParFile(new ParFile(readZipEntryContent(zipInputStream), messageSource));  //todo have to redefine in jquery.validate.min.js
        }
        else if (PROJECT_STRUCTURE_DESCRIPTOR_FILENAME.equals(entryName)) {
          request.setProjectStructure(parseProjectStructure(readZipEntryContent(zipInputStream)));
        }
        else if (PROJECT_DATA_SOURCE_FILENAME.equals(entryName)) {
          fillConnectinInfo(request, new String(readZipEntryContent(zipInputStream), "utf-8"));
        }
        else if (PROJECT_GROOVY_FUNCTIONS_FILENAME.equals(entryName)) {
          request.setProjectGroovyFunctions(parseProjectGroovyFunctionList(readZipEntryContent(zipInputStream)));
        }
      }
    }
    finally {
      IOUtils.closeQuietly(zipInputStream);
    }

    return request;
  }

  private static byte[] readZipEntryContent(ZipInputStream zipInputStream) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
    IOUtils.copy(zipInputStream, bufferedOutputStream);
    bufferedOutputStream.flush();
    return byteArrayOutputStream.toByteArray();
  }

  private static void fillConnectinInfo(WbaFile wbaFile, String datasourceFile) throws ProjectParseException {
    XPathFactory xpFactory = XPathFactory.newInstance();
    XPath path = xpFactory.newXPath();
    try {
      Document doc = SAXUtils.getDocument(datasourceFile);

      wbaFile.setJndiName((String) path.evaluate("//datasource/@jndi-name", doc, XPathConstants.STRING));
      wbaFile.setJdbcDriverClassName((String) path.evaluate("//datasource/@driver-class", doc, XPathConstants.STRING));
    }
    catch (IOException ex) {
      throw new ProjectParseException("Unreadable process definition", ex);
    }
    catch (ParserConfigurationException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
    catch (SAXException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
    catch (XPathExpressionException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
  }

  private static Project parseProjectStructure(byte[] projectStructureFile) {
    return XmlUtils.deserializeProjectStructure(new ByteArrayInputStream(projectStructureFile));
  }

  private static GroovyFunctionList parseProjectGroovyFunctionList(byte[] projectGroovyFunctionsFile) {
    return XmlUtils.deserializeFunctionList(new ByteArrayInputStream(projectGroovyFunctionsFile));
  }
}
