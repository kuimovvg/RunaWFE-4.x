package org.jbpm.ui.editor.ltk;

import java.util.List;

import org.jbpm.ui.jpdl3.model.SendMessageNode;
import org.jbpm.ui.util.VariableMapping;

public class SendMessageRenameProvider extends MessageRenameProvider<SendMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
