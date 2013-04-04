package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.lang.par.ParContentProvider;

/**
 * Be careful using this class (methods can return nulls if no active editor opened).
 */
public class ProjectFinder {
    public static IProject[] getAllProcessDefinitionProjects() {
        List<IProject> returnList = new ArrayList<IProject>();
        try {
            for (IProject project : getWorkspaceProjects()) {
                if (project.getNature(BotStationNature.NATURE_ID) == null) {
                    returnList.add(project);
                }
            }
        } catch (CoreException e) {
            return new IProject[] {};
        }
        return returnList.toArray(new IProject[0]);
    }

    private static IProject[] getWorkspaceProjects() {
        return ResourcesPlugin.getWorkspace().getRoot().getProjects();
    }

    public static IFile getCurrentFile() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow == null) {
            return null;
        }
        IEditorPart editorPart = activeWindow.getActivePage().getActiveEditor();
        if (editorPart == null) {
            return null;
        }
        if (editorPart.getEditorInput() instanceof IFileEditorInput) {
            return ((IFileEditorInput) editorPart.getEditorInput()).getFile();
        }
        return null;
    }

    public static IProject getCurrentProject() {
        IFile file = getCurrentFile();
        return file == null ? null : file.getProject();
    }

    public static IJavaProject getAnyJavaProject() {
        IProject[] projects = getAllProcessDefinitionProjects();
        if (projects.length > 0) {
            return JavaCore.create(projects[0]);
        }
        return null;
    }

    public static IFile getFile(String fileName) {
        return IOUtils.getAdjacentFile(getCurrentFile(), fileName);
    }

    public static List<IFile> getAllProcessDefinitionFiles() {
        List<IFile> fileList = new ArrayList<IFile>();
        IProject[] projects = getAllProcessDefinitionProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].isOpen()) {
                fileList.addAll(getProcessDefinitionFiles(projects[i]));
            }
        }
        return fileList;
    }

    public static List<IFile> getProcessDefinitionFiles(IProject project) {
        try {
            List<IFile> fileList = new ArrayList<IFile>();
            IFolder processFolder = getProcessFolder(project);
            findProcessDefinitionFiles(processFolder, fileList, JavaCore.create(project).getOutputLocation());
            return fileList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IFolder getProcessFolder(IProject project) {
        try {
            IFolder srcFolder = project.getFolder("src");
            if (!srcFolder.exists()) {
                srcFolder.create(true, true, null);
            }
            IFolder processFolder = srcFolder.getFolder("process");
            if (!processFolder.exists()) {
                processFolder.create(true, true, null);
            }
            return processFolder;
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static void findProcessDefinitionFiles(IResource resource, List<IFile> fileList, IPath excludePath) throws CoreException {
        if (resource instanceof IContainer) {
            IContainer folder = (IContainer) resource;
            IResource[] resources = folder.members();
            for (int i = 0; i < resources.length; i++) {
                findProcessDefinitionFiles(resources[i], fileList, excludePath);
            }
        } else if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            if (ParContentProvider.PROCESS_DEFINITION_FILE_NAME.equals(file.getName()) && !file.getFullPath().toString().startsWith(excludePath.toString())) {
                fileList.add(file);
            }
        }
    }

    public static void refreshProcessFolder(IFile definitionFile) throws CoreException {
        definitionFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
    }

    public static IFile getProcessDefinitionFile(IFolder folder) {
        return folder.getFile(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
    }

    public static IProject[] getAllBotStationProjects() {
        try {
            List<IProject> projects = new ArrayList<IProject>();
            for (IProject project : getWorkspaceProjects()) {
                if (project.isOpen() && project.getNature(BotStationNature.NATURE_ID) != null) {
                    projects.add(project);
                }
            }
            return projects.toArray(new IProject[0]);
        } catch (CoreException e) {
            throw new RuntimeException();
        }
    }

    public static List<IFolder> getAllBotFolders() {
        List<IFolder> folderList = new ArrayList<IFolder>();
        for (IProject botStation : getAllBotStationProjects()) {
            folderList.addAll(getBotFolders(botStation));
        }
        return folderList;
    }

    public static List<IFolder> getBotFolders(IProject project) {
        try {
            List<IFolder> folderList = new ArrayList<IFolder>();
            IFolder botFolder = project.getFolder("src/botstation");
            IResource[] resources = botFolder.members();
            for (int i = 0; i < resources.length; i++) {
                if (resources[i] instanceof IFolder) {
                    folderList.add((IFolder) resources[i]);
                }
            }
            return folderList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<IFile> getBotTaskFiles(IFolder botFolder) {
        List<IFile> fileList = new ArrayList<IFile>();
        try {
            IResource[] resources = botFolder.members();
            for (int i = 0; i < resources.length; i++) {
                if (resources[i] instanceof IFile && resources[i].getFileExtension() == null) {
                    fileList.add((IFile) resources[i]);
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return fileList;
    }

    public static List<IFile> getAllBotTasks() {
        List<IFile> fileList = new ArrayList<IFile>();
        for (IFolder folder : getAllBotFolders()) {
            fileList.addAll(getBotTaskFiles(folder));
        }
        return fileList;
    }
}
