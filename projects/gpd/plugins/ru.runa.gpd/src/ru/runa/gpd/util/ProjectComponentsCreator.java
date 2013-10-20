package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.PreferenceConstants;

public final class ProjectComponentsCreator {
    private static final Path CONTAINER_PATH = new Path("JBPM");
    private static final String SOURCE_LOCATION = "src/process";
    private static final String OUTPUT_LOCATION = "bin";
    private static final String PROJECT_NAME_KEY = "projectName";
    private static final String PROJECT_PROPERTIES_FILE_NAME = "project.properties";

    public static void createOutputLocation(IJavaProject javaProject) throws CoreException {
        IFolder binFolder = javaProject.getProject().getFolder(OUTPUT_LOCATION);
        ru.runa.gpd.util.IOUtils.createFolder(binFolder);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);
    }

    public static void addJavaBuilder(IJavaProject javaProject) throws CoreException {
        IProjectDescription desc = javaProject.getProject().getDescription();
        ICommand command = desc.newCommand();
        command.setBuilderName(JavaCore.BUILDER_ID);
        desc.setBuildSpec(new ICommand[] { command });
        javaProject.getProject().setDescription(desc, null);
    }

    public static void setClasspath(IJavaProject javaProject) throws CoreException {
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        // addSourceFolders
        IFolder folder = javaProject.getProject().getFolder(SOURCE_LOCATION);
        ru.runa.gpd.util.IOUtils.createFolder(folder);
        IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);
        entries.add(JavaCore.newSourceEntry(root.getPath()));
        // add JRE libraries
        entries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        // add RunaWFE libraries
        JavaCore.setClasspathContainer(CONTAINER_PATH, new IJavaProject[] { javaProject }, new IClasspathContainer[] { new ru.runa.gpd.util.ProjectClasspathContainer(javaProject,
                CONTAINER_PATH) }, null);
        entries.add(JavaCore.newContainerEntry(CONTAINER_PATH));
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
    }

    public static void createProjectProperties(IJavaProject javaProject, String projectName) throws IOException, CoreException {
        IFolder folder = javaProject.getProject().getFolder(SOURCE_LOCATION);
        IFile projectPropFile = folder.getFile(PROJECT_PROPERTIES_FILE_NAME);

        InputStream source = null;
        try {
            if (!projectPropFile.exists()) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append(PROJECT_NAME_KEY);
                contentBuilder.append("=");
                contentBuilder.append(projectName);
                String content = contentBuilder.toString();
                byte[] bytes = content.getBytes("UTF-8");
                source = new ByteArrayInputStream(bytes);
                projectPropFile.create(source, IResource.NONE, null);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }
}
