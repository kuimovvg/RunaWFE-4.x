package ru.runa.gpd.formeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.util.InetAddrPort;

import ru.runa.gpd.Activator;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.IOUtils;

public class WebServerUtils {
    private static Server server;
    public static final int SERVER_PORT = 48780;
    private static String lastUsedEditor = null;

    /**
     * The FCKeditor directory name.
     */
    private static String getEditorDirectoryName() {
        return useCKEditor3() ? "CKEditor" : "FCKeditor";
    }

    public static File getEditorDirectory() {
        File result;
        if (useCKEditor3()) {
            result = new File(getStateLocation().toFile(), getEditorDirectoryName());
        } else {
            result = new File(new File(getStateLocation().toFile(), getEditorDirectoryName()), "editor");
        }
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public static String getEditorURL() {
        return "http://localhost:" + SERVER_PORT + (useCKEditor3() ? "/ckeditor.html" : "/fckeditor.html");
    }

    public static boolean useCKEditor3() {
        return PrefConstants.FORM_CK_EDITOR.equals(Activator.getPrefString(PrefConstants.P_FORM_DEFAULT_FCK_EDITOR));
    }
    
    private static IPath getStateLocation() {
        return WYSIWYGPlugin.getDefault().getStateLocation();
    }

    public static void startWebServer(IProgressMonitor monitor, int allProgressCount) throws Exception {
        int remain = allProgressCount;
        monitor.subTask(Messages.getString("editor.subtask.copy_resources"));
        IPath location = getStateLocation();
        File fckRootFolder = new File(location.toFile(), getEditorDirectoryName());
        if (!getEditorDirectoryName().equals(lastUsedEditor)) {
            lastUsedEditor = getEditorDirectoryName();
            fckRootFolder.mkdir();
            int progressForFck = remain - 1;
            copyFolderWithProgress(location, getEditorDirectoryName(), monitor, progressForFck);
            // monitor.worked(progressForFck);
            remain = 1;
            if (isWebServerStarted()) {
                server.stop();
            }
        }
        monitor.subTask(Messages.getString("editor.subtask.start_server"));
        if (!isWebServerStarted()) {
            server = new Server();
            WebApplicationContext applicationContext = new WebApplicationContext(fckRootFolder.getAbsolutePath());
            applicationContext.setContextPath("/");
            applicationContext.addHandler(new ResourceHandler());
            server.addContext(applicationContext);
            InetAddrPort address = new InetAddrPort(SERVER_PORT);
            server.addListener(address);
            server.start();
        }
        monitor.worked(remain);
    }

    private static boolean isWebServerStarted() {
        return server != null && server.isStarted();
    }

    public static void stopWebServer() throws Exception {
        if (isWebServerStarted()) {
            server.stop();
        }
    }

    private static void copyFolderWithProgress(IPath root, String path, IProgressMonitor monitor, int allProgressCount) throws IOException {
        int allFilesCount = countFiles(path);
        int filesForUnitWork = allFilesCount / allProgressCount;
        copyFolder(root, path, monitor, filesForUnitWork, 0);
    }

    private static int copyFolder(IPath root, String path, IProgressMonitor monitor, int filesForUnitWork, int currentUnitFilesCount) throws IOException {
        File folder = new File(root.toFile(), path);
        folder.mkdir();
        Enumeration<String> e = WYSIWYGPlugin.getDefault().getBundle().getEntryPaths(path);
        int filesSize = 0;
        while (e != null && e.hasMoreElements()) {
            String child = e.nextElement();
            if (child.endsWith("/")) {
                filesSize = copyFolder(root, child, monitor, filesForUnitWork, currentUnitFilesCount);
            } else {
                InputStream in = WYSIWYGPlugin.getDefault().getBundle().getEntry(child).openStream();
                File targetFile = new File(root.toFile(), child);
                OutputStream out = new FileOutputStream(targetFile);
                IOUtils.copyStream(in, out);
                filesSize++;
                if (filesSize == filesForUnitWork) {
                    monitor.worked(1);
                    filesSize = 0;
                }
            }
        }
        return filesSize;
    }

    private static int countFiles(String path) throws IOException {
        int result = 0;
        Enumeration<String> e = WYSIWYGPlugin.getDefault().getBundle().getEntryPaths(path);
        while (e != null && e.hasMoreElements()) {
            String child = e.nextElement();
            if (child.endsWith("/")) {
                result += countFiles(child);
            } else {
                result++;
            }
        }
        return result;
    }


}
