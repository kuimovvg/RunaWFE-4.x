package org.jbpm.ui.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JbpmClasspathContainer implements IClasspathContainer {
    private static IClasspathEntry[] jbpmLibraryEntries;

    private final IPath path;

    public JbpmClasspathContainer(IJavaProject javaProject, IPath path) {
        this.path = path;
    }

    public IClasspathEntry[] getClasspathEntries() {
        if (jbpmLibraryEntries == null) {
            jbpmLibraryEntries = createJbpmLibraryEntries();
        }
        return jbpmLibraryEntries;
    }

    public String getDescription() {
        return "jBPM Library";
    }

    public int getKind() {
        return IClasspathContainer.K_APPLICATION;
    }

    public IPath getPath() {
        return path;
    }

    @SuppressWarnings("unchecked")
	private IClasspathEntry[] createJbpmLibraryEntries() {
        List<IClasspathEntry> classPathEntries = new ArrayList<IClasspathEntry>();
        Enumeration<URL> enumUrls = Platform.getBundle("org.jbpm.core").findEntries("/", "*.jar", true);
        while (enumUrls.hasMoreElements()) {
            URL bundleUrl = enumUrls.nextElement();
            try {
                URL fileUrl = FileLocator.toFileURL(bundleUrl);
                Path jarPath = new Path(fileUrl.getPath());
                classPathEntries.add(JavaCore.newLibraryEntry(jarPath, null, null));
            } catch (IOException e) {
                throw new RuntimeException("Error loading classpath library from: " + bundleUrl);
            }
        }
        return classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]);
    }
}
