package ru.runa.gpd.formeditor.wysiwyg;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class ConnectorServletHelper {
    private static String baseDir;
    private static List<String> syncronizations = new ArrayList<String>();

    public static void sync() {
        try {
            if (baseDir == null) {
                return;
            }
            File dir = new File(baseDir);
            File[] resourceFiles = dir.listFiles(new ConnectorServletHelper.FileExtensionFilter());
            for (File file : resourceFiles) {
                if (!file.isDirectory() && !syncronizations.contains(file.getAbsolutePath())) {
                    IOUtils.copyFileToDir(file, WYSIWYGPlugin.getDefault().FCK_RESOURCES_FOLDER());
                    syncronizations.add(file.getAbsolutePath());
                }
            }
            File formCssFile = new File(WYSIWYGPlugin.getDefault().FCK_RESOURCES_FOLDER(), ParContentProvider.FORM_CSS_FILE_NAME);
            if (formCssFile.exists()) {
                formCssFile.delete();
            }
            formCssFile = new File(dir, ParContentProvider.FORM_CSS_FILE_NAME);
            if (formCssFile.exists()) {
                IOUtils.copyFileToDir(formCssFile, WYSIWYGPlugin.getDefault().FCK_RESOURCES_FOLDER());
            }
        } catch (IOException e) {
            PluginLogger.logError(e);
        }
    }

    public static String getBaseDir() {
        return baseDir;
    }

    public static void setBaseDir(String dir) {
        File baseFile = new File(dir);
        if (!baseFile.exists()) {
            baseFile.mkdir();
        }
        baseDir = dir;
    }

    static class FileExtensionFilter implements FilenameFilter {
        private final List<String> extensionExceptions = Arrays.asList("ftl", "xml");

        @Override
        public boolean accept(File dir, String name) {
            int index = name.lastIndexOf(".");
            if (index == -1) {
                return false;
            }
            return !extensionExceptions.contains(name.substring(index + 1));
        }
    }
}