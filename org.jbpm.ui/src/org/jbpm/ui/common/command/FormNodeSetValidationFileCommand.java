package org.jbpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import org.jbpm.ui.common.model.FormNode;

public class FormNodeSetValidationFileCommand extends Command {

    private FormNode formNode;

    private String validationFileName;

    private String oldFileName;

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public void setValidationFileName(String fileName) {
        this.validationFileName = fileName;
    }

    @Override
    public void execute() {
        oldFileName = formNode.getValidationFileName();
        formNode.setValidationFileName(validationFileName);
    }

    @Override
    public void undo() {
        formNode.setValidationFileName(oldFileName);
    }

}
