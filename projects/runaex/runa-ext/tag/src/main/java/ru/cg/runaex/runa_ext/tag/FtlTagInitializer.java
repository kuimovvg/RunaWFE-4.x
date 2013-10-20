package ru.cg.runaex.runa_ext.tag;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.FreemarkerConfiguration;
import ru.runa.wfe.commons.ftl.FreemarkerTag;

/**
 * @author Kochetkov
 */
public class FtlTagInitializer implements InitializingBean {
  private static final Log logger = LogFactory.getLog(FtlTagInitializer.class);
  private static final String CONFIG = "freemarker-tags.xml";
  private static final String TAG_ELEMENT = "ftltag";
  private static final String NAME_ATTR = "name";
  private static final String CLASS_ATTR = "class";

  @Override
  @SuppressWarnings("unchecked")
  public void afterPropertiesSet() throws Exception {
    InputStream is = null;
    try {
      is = getClass().getResourceAsStream(CONFIG);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BOMInputStream(is), Charset.forName("UTF-8")));

      String readLine;
      StringBuilder stringBuilder = new StringBuilder();
      while ((readLine = bufferedReader.readLine()) != null) {
        stringBuilder.append(readLine);
      }
      String xml = stringBuilder.toString();
      XPathFactory xpFactory = XPathFactory.newInstance();
      XPath path = xpFactory.newXPath();

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = docFactory.newDocumentBuilder();
      org.w3c.dom.Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
      NodeList tagElements = (NodeList) path.evaluate("//ftltag[@name and @class]", document, XPathConstants.NODESET);

      FreemarkerConfiguration conf = FreemarkerConfiguration.getInstance();
      for (int i = 0; i < tagElements.getLength(); i++) {
        try {
          Node el = tagElements.item(i);
          String name = el.getAttributes().getNamedItem(NAME_ATTR).getNodeValue();
          String className = el.getAttributes().getNamedItem(CLASS_ATTR).getNodeValue();
          Class<? extends FreemarkerTag> tagClass = (Class<? extends FreemarkerTag>) ClassLoaderUtil.loadClass(className);
          Method method = conf.getClass().getDeclaredMethod("addTag", String.class, Class.class);
          method.setAccessible(true);
          method.invoke(conf, name, tagClass);
        }
        catch (Throwable e) {
          logger.warn("Unable to create freemarker tag", e);
        }
      }
    }
    finally {
      IOUtils.closeQuietly(is);
    }
  }
}
