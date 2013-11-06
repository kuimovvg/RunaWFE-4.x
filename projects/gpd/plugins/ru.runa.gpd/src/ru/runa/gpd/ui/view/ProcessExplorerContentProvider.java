/*
 * Created on 24.09.2005
 */
package ru.runa.gpd.ui.view;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

import com.google.common.collect.Lists;

public class ProcessExplorerContentProvider implements ITreeContentProvider {
    
    @Override
    public Object[] getChildren(Object parentElement) {
        List<IFolder> folders = Lists.newArrayList();
        if (parentElement instanceof IProject) {
            IProject project = (IProject) parentElement;
            if (IOUtils.isProjectHasProcessNature(project)) {
                findFolders(project, folders);
            } else {
                List<IFile> files = IOUtils.getProcessDefinitionFiles(project);
                for (IFile file : files) {
                    folders.add((IFolder) file.getParent());
                }
            }
        }
        if (parentElement instanceof IFolder) {
            IFolder folder = (IFolder) parentElement;
            findFolders(folder, folders);
        }
        return folders.toArray(new IFolder[folders.size()]);
    }

    private static void findFolders(IContainer container, List<IFolder> result) {
        try {
            for (IResource resource : container.members()) {
                if (resource instanceof IFolder) {
                    IFolder folder = (IFolder) resource;
                    IFile definitionFile = folder.getFile(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                    if (definitionFile.exists()) {
                        result.add(folder);
                        continue;
                    }
                    if (folder.getName().startsWith(".")) {
                        continue;
                    }
                    result.add(folder);
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object parentElement) {
        return getChildren(parentElement).length > 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return IOUtils.getAllProcessDefinitionProjects();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
