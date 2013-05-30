package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.part.graph.NodeGraphicalEditPart;
import ru.runa.gpd.lang.model.Node;

import com.google.common.collect.Lists;

public class CopyAction extends SelectionAction {
    private final ProcessEditorBase editor;

    public CopyAction(ProcessEditorBase editor) {
        super(editor);
        this.editor = editor;
        setText(Localization.getString("button.copy"));
    }

    @Override
    protected boolean calculateEnabled() {
        return extractNodes().size() > 0;
    }
    
    private List<Node> extractNodes() {
        List<EditPart> editParts = editor.getGraphicalViewer().getSelectedEditParts();
        List<Node> result = Lists.newArrayList();
        for (EditPart editPart : editParts) {
            if (!(editPart instanceof NodeGraphicalEditPart)) {
                continue;
            }
            Node node = ((NodeGraphicalEditPart) editPart).getModel();
            result.add(node);
        }
        return result;
    }


    @Override
    public void run() {
        CopyBuffer copyBuffer = new CopyBuffer((IFolder) editor.getDefinitionFile().getParent(), editor.getDefinition(), extractNodes());
        copyBuffer.setToClipboard();
    }
}