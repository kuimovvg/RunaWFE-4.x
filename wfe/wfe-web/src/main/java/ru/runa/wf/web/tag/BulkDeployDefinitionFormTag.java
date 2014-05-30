package ru.runa.wf.web.tag;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wf.web.tag.ProcessDefinitionBaseFormTag;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created 26.05.2014 bulkDeployDefinitionControlForm
 * 
 */
public class BulkDeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {
	
	private static final long serialVersionUID = 5106978254165128752L;
    private static final String TYPE_DEFAULT = "_default_type_";

    private static Select getTypeSelectElement(String selectedValue, String[] definitionTypes, User user, PageContext pageContext) {
        ProcessTypesIterator iter = new ProcessTypesIterator(user);
        Select select = new Select("typeSel");
        select.setID("processDefinitionTypeSelect");
        {
            Option option = new Option();
            option.addElement(Messages.getMessage("batch_presentation.process.no_type", pageContext));
            option.setValue(TYPE_DEFAULT);
            if (TYPE_DEFAULT.equals(selectedValue)) {
                option.setSelected(true);
            }
            select.addElement(option);
        }
        int idx = 0;
        while (iter.hasNext()) {
            String[] type = iter.next();

            StringBuilder typeBuild = new StringBuilder();
            for (int i = 1; i < type.length; ++i) {
                typeBuild.append(Entities.NBSP).append(Entities.NBSP).append(Entities.NBSP);
            }
            typeBuild.append(type[type.length - 1]);
            Option option = new Option();
            option.setValue(Integer.toString(idx));
            option.addElement(typeBuild.toString());
            if ((selectedValue == null && Arrays.equals(type, definitionTypes)) || Integer.toString(idx).equals(selectedValue)) {
                option.setSelected(true);
            }
            select.addElement(option);
            ++idx;
        }
        return select;
    }

    public static void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext, WebHelper strutsWebHelper) {
        form.setEncType(Form.ENC_UPLOAD);

        String selectedValue = definitionTypes == null ? TYPE_DEFAULT : null;
        String newTypeName = "";
        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute("TypeAttributes");
        if (attr != null) {
            selectedValue = attr.get("typeSel");
            newTypeName = attr.get("type");
        }

        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(createFileInputRow(Messages.getMessage("process_definition.archive", pageContext), FileForm.FILE_INPUT_NAME, "", true,
                true, Input.FILE, strutsWebHelper));
        TD td = new TD();
        Select select = getTypeSelectElement(selectedValue, definitionTypes, user, pageContext);
        td.addElement(select);
        td.addElement(Entities.NBSP);
        Input typeInput = new Input(Input.TEXT, "type", String.valueOf(newTypeName));
        typeInput.setID("processDefinitionTypeName");
        typeInput.setStyle("width: 300px;");
        if (!TYPE_DEFAULT.equals(selectedValue)) {
            typeInput.setDisabled(true);
        }
        typeInput.setClass(Resources.CLASS_REQUIRED);
        td.addElement(typeInput);
        table.addElement(HTMLUtils.createRow(Messages.getMessage("batch_presentation.process_definition.process_type", pageContext), td));
        tdFormElement.addElement(table);
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext, new StrutsWebHelper(pageContext));
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.REDEPLOY_DEFINITION;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    @Override
    public String getAction() {
        return RedeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), DefinitionPermission.REDEPLOY_DEFINITION, getIdentifiable());
    }
    
    private static TR createFileInputRow(String label, String name, String value, boolean enabled, boolean required, String type, WebHelper strutsWebHelper)  {
    	TR tr = new TR();
        TD labelTd = new TD(label);
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        tr.addElement(labelTd);
        
        String fileInput = getFileInput(strutsWebHelper, name);
        
        Input input = new Input(type, name, String.valueOf(value));
        input.setDisabled(!enabled);
        input.addAttribute("multiple", true);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        
        tr.addElement(new TD(fileInput).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }
    
    private static String getFileInput(WebHelper webHelper, String variableName) {
        UploadedFile file = null;
       
        String attachImageUrl = "";
        String loadingImageUrl = "";
        String deleteImageUrl = "";
        String uploadFileTitle = Commons.getMessage("message.upload.file", webHelper.getPageContext());
        String loadingMessage = Commons.getMessage("message.loading", webHelper.getPageContext());
        if (webHelper != null) {
            attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
            loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
            deleteImageUrl = webHelper.getUrl(Resources.IMAGE_DELETE);
            loadingMessage = Commons.getMessage("message.loading", webHelper.getPageContext());
        }
        String hideStyle = "style=\"display: none;\"";
        String html = "<div class=\"inputFileContainer\">";
        html += "<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "<label class=\"inputFileAttach\">";
        html += "<div class=\"inputFileAttachButtonDiv\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "<input class=\"inputFile inputFileAjax \" name=\"" + variableName + "\" type=\"file\" multiple>";
        html += "</label></div>";
        html += "<div class=\"progressbar\" " + (file == null ? hideStyle : "") + ">";
        html += "<div class=\"line\" style=\"width: " + (file != null ? "10" : "") + "0%;\"></div>";
        html += "<div class=\"status\">";
        if (file != null) {
            html += "<img src=\"" + deleteImageUrl + "\" class=\"inputFileDelete\" inputId=\"" + variableName + "\">";
        } else {
            html += "<img src=\"" + loadingImageUrl + "\" inputId=\"" + variableName + "\">";
        }
        html += "<span class=\"statusText\">";
        if (file != null && webHelper != null) {
            String viewUrl = webHelper.getUrl("/upload?action=view&inputId=" + variableName);
            html += "<a href='" + viewUrl + "'>" + file.getName() + " - " + file.getSize() + "</a>";
        } else {
            html += loadingMessage;
        }
        html += "</span></div></div></div>";
        return html;
    }

}
