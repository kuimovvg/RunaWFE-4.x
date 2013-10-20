package ru.cg.runaex.runa_ext.tag.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.cg.runaex.runa_ext.tag.template.exception.TemplateNotFoundException;
import ru.cg.runaex.runa_ext.tag.template.exception.TemplateProcessException;

/**
 * Manager для работы с *.js шаблонами
 *
 * @author Абдулин Ильдар
 */
public class JsTemplateManager {

  protected static final Log logger = LogFactory.getLog(JsTemplateManager.class);

  private static final Map<String, Template> templates = new HashMap<String, Template>();

  private static Template getTemplateForClass(String jsTemplateName) {
    try {

      if (templates.get(jsTemplateName) != null) {
        return templates.get(jsTemplateName);
      }

      Configuration cfg = new Configuration();
      cfg.setClassForTemplateLoading(JsTemplateManager.class, "");
      Template template = cfg.getTemplate(jsTemplateName + ".js");
      templates.put(jsTemplateName, template);
      return template;
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new TemplateNotFoundException(e.getMessage(), e);
    }
  }

  public static StringBuffer process(String jsTemplateName, Map<String, String> attrs) {
    StringWriter stringWriter = new StringWriter();
    try {
      getTemplateForClass(jsTemplateName).process(attrs, stringWriter);

      StringBuffer buffer = new StringBuffer();
      buffer.append("<script type=\"text/javascript\">");
      buffer.append(stringWriter.getBuffer());
      buffer.append("</script>");
      return buffer;

    }
    catch (TemplateException e) {
      logger.error(e.getMessage(), e);
      throw new TemplateProcessException(e.getMessage(), e);
    }
    catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new TemplateProcessException(e.getMessage(), e);
    }
  }
}
