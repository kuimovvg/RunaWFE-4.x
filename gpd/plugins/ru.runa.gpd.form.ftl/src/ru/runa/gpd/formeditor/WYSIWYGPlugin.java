package ru.runa.gpd.formeditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class WYSIWYGPlugin extends AbstractUIPlugin {
    private static WYSIWYGPlugin plugin;

    public WYSIWYGPlugin() {
        plugin = this;
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
        WebServerUtils.stopWebServer();
        super.stop(context);
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

}
