package ru.runa.bpm.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import ru.runa.bpm.ui.view.ValidationErrorsView;
import ru.runa.bpm.ui.view.PropertiesView;

public class DesignerPerspectiveFactory implements IPerspectiveFactory {

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        IFolderLayout propsFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.70, editorArea);
        propsFolder.addView(PropertiesView.ID);
        propsFolder.addView(ValidationErrorsView.ID);
    }

}
