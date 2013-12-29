package ru.runa.wf.web.tag;

import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.ImportDataFileAction;

/**
 * 
 * @author riven
 * @jsp.tag name = "importDataFile" body-content = "JSP"
 */
public class ImportDataFileTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isVisible() {
        return WebResources.getBooleanProperty("action.datafile.enabled", false);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);

        TD td = new TD();
        Input uploadInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setID(ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setChecked(true);
        td.addElement(uploadInput);
        Label label = new Label(ImportDataFileAction.UPLOAD_ONLY);
        label.addElement(new StringElement(Messages.getMessage("managesystem.datafile.uploadonly.label", pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);
        Input uploadAndClearInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        uploadAndClearInput.setID(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        td.addElement(uploadAndClearInput);
        label = new Label(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        label.addElement(new StringElement(Messages.getMessage("managesystem.datafile.clearbeforeupload.label", pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(Messages.getMessage("managesystem.datafile.action.title", pageContext), td));

        table.addElement(HTMLUtils.createInputRow(Messages.getMessage("managesystem.datafile.title", pageContext), FileForm.FILE_INPUT_NAME, "",
                true, true, Input.FILE));
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_IMPORT_DATAFILE, pageContext);
    }

    @Override
    public String getAction() {
        return ImportDataFileAction.ACTION_PATH;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.TITLE_IMPORT_DATAFILE, pageContext);
    }
}
