package org.jbpm.ui.common.command;

import org.eclipse.gef.commands.Command;
import org.jbpm.ui.common.model.FormNode;

public class FormNodeSetFileCommand extends Command {

    private FormNode formNode;

    private String fileName;

    private String oldFileName;

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void execute() {
        oldFileName = formNode.getFormFileName();
        formNode.setFormFileName(fileName);
    }

    @Override
    public void undo() {
        formNode.setFormFileName(oldFileName);
    }

}
