package ru.runa.gpd.formeditor;

import java.util.Map;

import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.collect.Maps;

public class HtmlFormType extends BaseHtmlFormType {
    @Override
    public boolean isCreationAllowed() {
        return false;
    }

    @Override
    protected Map<String, FormVariableAccess> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        return Maps.newHashMap();
    }
}
