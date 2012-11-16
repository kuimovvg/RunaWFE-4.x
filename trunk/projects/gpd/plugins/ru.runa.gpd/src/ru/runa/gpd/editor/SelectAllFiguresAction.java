package ru.runa.gpd.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionFactory;

public class SelectAllFiguresAction extends Action {
    private final ProcessEditorBase editor;

    public SelectAllFiguresAction(ProcessEditorBase editor) {
        this.editor = editor;
        setText(GEFMessages.SelectAllAction_Label);
        setToolTipText(GEFMessages.SelectAllAction_Tooltip);
        setId(ActionFactory.SELECT_ALL.getId());
    }

    @Override
    public void run() {
        GraphicalViewer viewer = editor.getGraphicalViewer();
        if (viewer != null) {
            List<EditPart> elements = new ArrayList<EditPart>();
            getAllChildren(viewer.getContents(), elements);
            viewer.setSelection(new StructuredSelection(elements));
        }
    }

    @SuppressWarnings("unchecked")
    private void getAllChildren(EditPart editPart, List<EditPart> allChildren) {
        List<EditPart> children = editPart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            GraphicalEditPart child = (GraphicalEditPart) children.get(i);
            allChildren.add(child);
            allChildren.addAll(child.getSourceConnections());
            allChildren.addAll(child.getTargetConnections());
            getAllChildren(child, allChildren);
        }
    }
}
