package ru.runa.bpm.ui.editor.ltk;

import java.util.List;

import ru.runa.bpm.ui.jpdl3.model.ReceiveMessageNode;
import ru.runa.bpm.ui.util.VariableMapping;

public class ReceiveMessageRenameProvider extends MessageRenameProvider<ReceiveMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
