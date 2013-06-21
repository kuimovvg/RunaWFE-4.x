package ru.runa.gpd.formeditor;

import java.util.HashMap;
import java.util.Map;

import ru.runa.gpd.lang.model.FormNode;

public class HtmlFormType extends BaseHtmlFormType {
    @Override
    public boolean isCreationAllowed() {
        return false;
    }

    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        return new HashMap<String, Integer>();
    }
}
