package org.jbpm.ui.editor.ltk;

import java.util.List;

import org.jbpm.ui.jpdl3.model.ReceiveMessageNode;
import org.jbpm.ui.util.VariableMapping;

public class ReceiveMessageRenameProvider extends MessageRenameProvider<ReceiveMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
