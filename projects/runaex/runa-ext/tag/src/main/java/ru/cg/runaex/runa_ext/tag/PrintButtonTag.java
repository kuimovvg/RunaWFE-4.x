package ru.cg.runaex.runa_ext.tag;

import java.util.List;
import java.util.Locale;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.*;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.PrintButton;
import ru.cg.runaex.components.bean.component.part.Column;
import ru.cg.runaex.components.bean.component.part.StoredProcedure;

/**
 * @author Петров А.
 */
public class PrintButtonTag extends BaseFreemarkerTag<PrintButton> {
  private static final long serialVersionUID = 1L;
  private static final String DOCX_FORMAT = "DOCX";
  private static final String XLS_FORMAT = "XLS";
  private static final String ODT_FORMAT = "ODT";
  private static final String PDF_FORMAT = "PDF";
  private static final String HTML_FORMAT = "HTML";

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.PRINT_BUTTON;
  }

  @Override
  protected String executeToHtml(PrintButton component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    logger.debug("printButtonName - ".concat(component.getName()));

    StringBuilder html = new StringBuilder();
    appendComponentCssReference(PrintButtonTag, html);
    appendComponentJsReference(ExtractFormData, html);
    appendComponentJsReference(PrintButtonTag, html);
    appendJsReference("jquery.download.js", html);

    String templateFileName = component.getTemplateFileName();
    boolean hasTemplate = templateFileName != null && !templateFileName.isEmpty();
    if (hasTemplate) {
      Div div = new Div();
      div.setStyle("display:inline");
      div.setClass("btn-group");

      Span span = new Span();
      span.setClass("caret");

      Button button = new Button();
      button.setClass("runaex btn btn-primary btn-print dropdown-toggle");
      button.addAttribute("data-toggle", "dropdown");
      I htmlI = new I();
      htmlI.setClass("icon-print");
      button.setType("button");
      button.setTagText(htmlI.toString() + component.getName() + span.toString());
      button.addAttribute("template_file_name", templateFileName);
      if (component.getWidth() != null) {
        button.addAttribute("style", "width:" + component.getWidth() + "px;");
      }

      if (component.getTableId() != null && !component.getTableId().isEmpty()) {
        button.addAttribute("tableId", component.getTableId());
      }

      List<StoredProcedure> storedProcedures = component.getStoredProcedures();

      if (storedProcedures != null) {
        StringBuilder procedures = new StringBuilder();
        for (StoredProcedure storedProcedure : storedProcedures) {
          procedures.append(storedProcedure.getSchema());
          procedures.append(".");
          procedures.append(storedProcedure.getProcedureName());
          procedures.append("(");
          for (Column parameter : storedProcedure.getParameters()) {
            procedures.append(parameter.getName()).append(", ");
          }
          if (storedProcedure.getParameters().size() > 0) {
            procedures.delete(procedures.length() - 2, procedures.length());
          }
          procedures.append(")");
          procedures.append(";");
        }
        button.addAttribute("stored_procedures", procedures.toString());
      }

      LI[] links = new LI[5];

      LI liElement = getLi("generateDocx", DOCX_FORMAT, IMG_PATH + "docx.png");
      links[0] = liElement;

      liElement = getLi("generateExcel", XLS_FORMAT, IMG_PATH + "xls.png");
      links[1] = liElement;

      liElement = getLi("generateOdt", ODT_FORMAT, IMG_PATH + "odt.png");
      links[2] = liElement;

      liElement = getLi("generatePdf", PDF_FORMAT, IMG_PATH + "pdf.png");
      links[3] = liElement;

      liElement = getLi("generateHtml", HTML_FORMAT, IMG_PATH + "html.png");
      links[4] = liElement;

      UL ulElement = new UL();
      ulElement.setClass("dropdown-menu");
      ulElement.addElement(links);

      div.addElement(button);
      div.addElement(ulElement);

      html.append(div.toString());
    }
    else {
      Button button = new Button();
      button.setID("print_button_".concat(String.valueOf(System.nanoTime())));
      button.setClass("runaex btn btn-primary btn-print");
      button.setValue(component.getName());
      I htmlI = new I();
      htmlI.setClass("icon-print");
      button.setType("button");
      button.setTagText(htmlI.toString() + component.getName());
      if (component.getTableId() != null && !component.getTableId().isEmpty()) {
        button.addAttribute("tableId", component.getTableId());
      }
      button.setOnClick("print(this)");
      html.append(button.toString());
    }

    return html.toString();
  }

  private LI getLi(String userStrKey, String format, String iconSrc) {
    LI liElement = new LI();
    IMG img = new IMG();
    img.setSrc(iconSrc);
    A link = new A();
    link.setHref("#");
    link.addAttribute("report_type", format);
    link.setTagText(img.toString() + resourceBundleMessageSource.getMessage(userStrKey, null, Locale.ROOT));
    link.setOnClick("print(this)");
    liElement.addElement(link);
    return liElement;
  }
}
