package ru.runa.gpd.formeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.util.InetAddrPort;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.IOUtils;

/**
 * The main plugin class to be used in the desktop.
 */
public class WYSIWYGPlugin extends AbstractUIPlugin {
    private static WYSIWYGPlugin plugin;
    private Server server;
    private static ResourceBundle resource = ResourceBundle.getBundle("ru.runa.gpd.formeditor.wysiwyg.messages");
    public static final int SERVER_PORT = 48780;

    /**
     * The FCKeditor directory name.
     */
    public String FCK_EDITOR() {
        return useCKEditor3() ? "CKEditor" : "FCKeditor";
    }

    public File FCK_RESOURCES_FOLDER() {
        File result;
        if (useCKEditor3()) {
            result = new File(plugin.getStateLocation().toFile(), FCK_EDITOR());
        } else {
            result = new File(new File(plugin.getStateLocation().toFile(), FCK_EDITOR()), "editor");
        }
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public WYSIWYGPlugin() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    public static String getResourceString(String key) {
        return resource.getString(key);
    }
    
    public static String getResourceString(String key, Object... parameters) {
        String msg = getResourceString(key);
        return MessageFormat.format(msg, parameters);
    }


    public String getEditorURL() {
        return "http://localhost:" + SERVER_PORT + (useCKEditor3() ? "/ckeditor.html" : "/fckeditor.html");
    }

    private String lastUsedEditor = null;

    public synchronized void startWebServer(IProgressMonitor monitor, int allProgressCount) throws Exception {
        int remain = allProgressCount;
        monitor.subTask(WYSIWYGPlugin.getResourceString("editor.subtask.copy_resources"));
        IPath location = getStateLocation();
        File fckRootFolder = new File(location.toFile(), FCK_EDITOR());
        if (!FCK_EDITOR().equals(lastUsedEditor)) {
            lastUsedEditor = FCK_EDITOR();
            fckRootFolder.mkdir();
            int progressForFck = remain - 1;
            copyFolderWithProgress(location, FCK_EDITOR(), monitor, progressForFck);
            // monitor.worked(progressForFck);
            remain = 1;
            if (isWebServerStarted()) {
                server.stop();
            }
        }
        monitor.subTask(WYSIWYGPlugin.getResourceString("editor.subtask.start_server"));
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

    private boolean isWebServerStarted() {
        return server != null && server.isStarted();
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

    public static void log(int severity, int code, String message, Throwable exception) {
        getDefault().getLog().log(new Status(severity, getDefault().getBundle().getSymbolicName(), code, message, exception));
    }

    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void logError(String message, Throwable exception) {
        log(IStatus.ERROR, IStatus.OK, message, exception);
    }

    /**
     * This method is called when the plug-in is stopped Stop web server
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (isWebServerStarted()) {
            server.stop();
        }
        // hide underlying exceptions
        //super.stop(context);
        plugin = null;
    }

    public static WYSIWYGPlugin getDefault() {
        return plugin;
    }

    public static InputStream loadTagImage(Bundle bundle, String imagePath) throws IOException {
        String lang = Locale.getDefault().getLanguage();
        int ldi = imagePath.lastIndexOf(".");
        if (ldi > 0) {
            String nlImagePath = imagePath.substring(0, ldi) + "." + lang + imagePath.substring(ldi);
            if (FileLocator.find(bundle, new Path(nlImagePath), new HashMap<String, String>()) != null) {
                return FileLocator.openStream(bundle, new Path(nlImagePath), false);
            }
        }
        return FileLocator.openStream(bundle, new Path(imagePath), false);
    }

    public static boolean useCKEditor3() {
        return PrefConstants.FORM_CK_EDITOR.equals(Activator.getPrefString(PrefConstants.P_FORM_DEFAULT_FCK_EDITOR));
    }
}
