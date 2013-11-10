package ru.runa.gpd.ltk;

import java.util.List;

import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.util.VariableMapping;

public class SendMessageRenameProvider extends MessageRenameProvider<SendMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
