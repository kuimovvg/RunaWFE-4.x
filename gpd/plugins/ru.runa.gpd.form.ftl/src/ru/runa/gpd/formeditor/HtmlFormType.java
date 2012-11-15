package ru.runa.gpd.formeditor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.model.FormNode;

public class HtmlFormType extends BaseHtmlFormType {
    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        return new HashMap<String, Integer>();
    }

    @Override
    public String getFormFileName(IFile definitionFile, FormNode formNode) {
        throw new UnsupportedOperationException("DEPRACATED. Use FTL forms.");
    }
}
