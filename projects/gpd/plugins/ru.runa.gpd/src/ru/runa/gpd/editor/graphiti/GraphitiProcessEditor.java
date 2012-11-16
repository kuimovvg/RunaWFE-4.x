package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.ui.parts.GraphicalEditor;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;

public class GraphitiProcessEditor extends ProcessEditorBase {
    public final static String ID = "ru.runa.gpd.GraphitiProcessEditor";

    @Override
    protected GraphicalEditor createGraphPage() {
        return new DiagramEditorPage(this);
    }

    @Override
    protected void selectGraphElement(GraphElement model) {
        ((DiagramEditorPage) graphPage).select(model);
    }
}
