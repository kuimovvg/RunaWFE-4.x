package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.util.WorkspaceOperations;

public class OpenSubProcessFeature extends AbstractCustomFeature {
    public OpenSubProcessFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return true;
    }

    @Override
    public void execute(ICustomContext context) {
        Subprocess subprocess = (Subprocess) getFeatureProvider().getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        IFile definitionFile = ProcessCache.getProcessDefinitionFile(subprocess.getSubProcessName());
        WorkspaceOperations.openProcessDefinition((IFolder) definitionFile.getParent());
    }
}
