package ru.runa.gpd.formeditor.ftl.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public enum ContentProvider {
    INSTANCE;

    public List<ToolPalleteMethodTag> getModel() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (activePage != null) {
                IEditorPart actEditor = activePage.getActiveEditor();
                if (actEditor instanceof IToolPalleteDropEditor) {
                    IToolPalleteDropEditor toolPalleteDropEditor = ((IToolPalleteDropEditor) actEditor);
                    return toolPalleteDropEditor.getAllMethods();
                }
            }
        }
        return new ArrayList<ToolPalleteMethodTag>(0);
    }
}