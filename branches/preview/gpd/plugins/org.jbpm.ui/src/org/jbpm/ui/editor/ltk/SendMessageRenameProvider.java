package ru.runa.bpm.ui.editor.ltk;

import java.util.List;

import ru.runa.bpm.ui.jpdl3.model.SendMessageNode;
import ru.runa.bpm.ui.util.VariableMapping;

public class SendMessageRenameProvider extends MessageRenameProvider<SendMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
