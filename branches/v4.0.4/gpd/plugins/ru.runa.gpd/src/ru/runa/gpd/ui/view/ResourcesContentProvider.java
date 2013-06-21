/*
 * Created on 24.09.2005
 */
package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.util.ProjectFinder;

/**
 * @author Nana
 */
public class ResourcesContentProvider implements ITreeContentProvider {
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IProject) {
            List<IFile> definitionFiles = ProjectFinder.getProcessDefinitionFiles((IProject) parentElement);
            IContainer[] elements = new IContainer[definitionFiles.size()];
            for (int i = 0; i < definitionFiles.size(); i++) {
                elements[i] = definitionFiles.get(i).getParent();
            }
            return elements;
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IProject) {
            return ProjectFinder.getProcessDefinitionFiles((IProject) element).size() > 0;
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<Object> returnList = new ArrayList<Object>();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            for (IResource resource : workspace.getRoot().members()) {
                if (resource instanceof IProject && ((IProject) resource).getNature(BotStationNature.NATURE_ID) != null) {
                    continue;
                }
                returnList.add(resource);
            }
            return returnList.toArray();
        } catch (CoreException e) {
            return new Object[] {};
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
