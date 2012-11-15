package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;

public class ExtDiagramEditorInput extends DiagramEditorInput {
    private final IFile file;

    public ExtDiagramEditorInput(URI diagramUri, String providerId, IFile file) {
        super(diagramUri, providerId);
        this.file = file;
    }
    
    public IFile getFile() {
        return file;
    }
}
