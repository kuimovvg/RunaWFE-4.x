package ru.cg.runaex.runa_ext.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.FileUpload;

/**
 * @author Петров А.
 */
public class FileUploadTag extends BaseFileTag<FileUpload> {
  private static final long serialVersionUID = 1L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.FILE_UPLOAD;
  }

  @Override
  protected String executeToHtml(FileUpload component) throws TemplateModelException {
    super.executeToHtml(component);

    String fieldId = "file_upload" + String.valueOf(System.nanoTime());
    addObjectToJs("fieldId", fieldId, false);

    String sign = null;
    if (selectedRowId != null && component.isSignRequired()) {
      sign = getValue(selectedRowId, schema, table, signColumnName, null, String.class, null);

      if (sign != null) {
        html.append("<div id=\"").append(fieldId).append("-file_sign_div").append("\"><div style='display:inline; margin-left:10px'>")
            .append(BaseFreemarkerTag.resourceBundleMessageSource.getMessage("signForFileVerifying", null, Locale.ROOT))
            .append("</div></div>");
      }
    }
    addObjectToJs("signVerifyRequired", sign != null);

    boolean isRequired;
    boolean isEditable = true;

    String requireRuleGroovyScript = component.getRequireRule().getGroovyScript();
    if (requireRuleGroovyScript != null) {
      isRequired = executeGroovyRule(requireRuleGroovyScript);
    }
    else {
      isRequired = component.getRequireRule().isUnconditionallyRequired();
    }

    String editableRuleGroovyScript = component.getEditabilityRule() != null ? component.getEditabilityRule().getGroovyScript() : null;
    if (editableRuleGroovyScript != null) {
      isEditable = executeGroovyRule(editableRuleGroovyScript);
    }

    Input input = new Input();
    input.setClass("runaex fileupload check-error");
    input.setID(fieldId);
    input.setName(component.getField());
    input.addAttribute("data-schema", schema);
    input.addAttribute("data-table", table);

    if (component.isSignRequired() && signColumnName != null) {
      input.addAttribute("sign-column-name", signColumnName);
    }

    input.setType("file");

    Input clearBtn = new Input();
    clearBtn.setType("button");
    clearBtn.setClass("runaex clearfileupload");
    clearBtn.setID("clear-".concat(fieldId));
    StringBuilder onclickStr = new StringBuilder();
    onclickStr.append("clearFileUploadInput('").append(fieldId).append("');");
    clearBtn.addAttribute("onclick", onclickStr.toString());
    clearBtn.setValue(resourceBundleMessageSource.getMessage("clearBtn", null, Locale.ROOT));
    clearBtn.addAttribute("disabled", "");

    Input downloadBtn = new Input();
    downloadBtn.setType("button");
    downloadBtn.setClass("runaex downloadfile");
    downloadBtn.setID("download-".concat(fieldId));
    onclickStr = new StringBuilder();
    onclickStr.append("downloadFile('").append(schema).append("', '").append(table).append("', '").append(component.getField()).append("', ").append(selectedRowId).append(");");
    downloadBtn.addAttribute("onclick", onclickStr.toString());
    downloadBtn.setValue(resourceBundleMessageSource.getMessage("downloadBtn", null, Locale.ROOT));

    Label deleteFileLabel = new Label();
    deleteFileLabel.setTagText(resourceBundleMessageSource.getMessage("deleteBtn", null, Locale.ROOT));
    deleteFileLabel.setClass("runaex deletefile");
    deleteFileLabel.setFor("delete-".concat(fieldId));

    Input deleteFileChkBox = new Input();
    deleteFileChkBox.setType("checkbox");
    deleteFileChkBox.setClass("runaex deletefile");
    deleteFileChkBox.setID("delete-".concat(fieldId));
    deleteFileChkBox.setName("delete-".concat(fieldId));

    Input hidden = new Input();
    hidden.setType("hidden");
    hidden.setClass("runaex");
    hidden.setID(fieldId.concat("-hidden"));
    hidden.setName(component.getField().concat("-hidden"));

    if (fileName == null || fileName.isEmpty()) {
      deleteFileChkBox.addAttribute("disabled", "");
      downloadBtn.addAttribute("disabled", "");
    }
    else if (component.isDefaultValueSet()) {
      putReferenceValueAsCurrent(component);
      input.addAttribute("is-default-file-from-db", true);
      input.addAttribute("data-schema-reference", schema);
      input.addAttribute("data-table-reference", table);
      input.addAttribute("data-field-reference", getField(component));
      input.addAttribute("file-name-value", fileName);
      deleteFileChkBox.addAttribute("disabled", "");
      downloadBtn.addAttribute("disabled", "");
    }
    else {
      downloadBtn.addAttribute("title", fileName);
      hidden.setValue(fileName);
      hidden.addAttribute("uploaded", fileName);
    }

    if (!isEditable) {
      input.addAttribute("disabled", "");
      deleteFileChkBox.addAttribute("disabled", "");
    }

    if (isRequired && !component.isDefaultValueSet()) {
      List<WrapComponent> wrapComponents = new ArrayList<WrapComponent>(2);
      wrapComponents.add(new WrapComponent(null, input, clearBtn));
      wrapComponents.add(new WrapComponent(null, downloadBtn, hidden));
      html.append(getHtmlWithValidation(wrapComponents));
    }
    else {
      List<WrapComponent> wrapComponents = new ArrayList<WrapComponent>(2);
      wrapComponents.add(new WrapComponent(null, input, clearBtn));
      wrapComponents.add(new WrapComponent(deleteFileLabel, downloadBtn, deleteFileChkBox));
      html.append(getHtmlWithValidation(wrapComponents));
    }
    html.append("</div>");
    return html.toString();
  }
}
