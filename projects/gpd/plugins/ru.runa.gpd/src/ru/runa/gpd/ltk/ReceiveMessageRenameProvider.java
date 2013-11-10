package ru.runa.gpd.ltk;

import java.util.List;

import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.util.VariableMapping;

public class ReceiveMessageRenameProvider extends MessageRenameProvider<ReceiveMessageNode> {

    @Override
    protected List<VariableMapping> getVariableMappings() {
        return element.getVariablesList();
    }

}
