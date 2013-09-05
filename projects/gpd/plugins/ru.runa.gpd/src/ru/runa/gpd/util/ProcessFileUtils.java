package ru.runa.gpd.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Strings;

import ru.runa.gpd.PluginLogger;
import ru.runa.wfe.definition.IFileDataProvider;

public class ProcessFileUtils {

    public static IFile getProcessFile(String fileName) {
        return IOUtils.getAdjacentFile(ProjectFinder.getCurrentFile(), fileName);
    }

    public static boolean isProcessFile(String path) {
        return path != null && path.startsWith(IFileDataProvider.PROCESS_FILE_PROTOCOL);
    }

    public static String getProcessFileName(String path) {
        return path.substring(IFileDataProvider.PROCESS_FILE_PROTOCOL.length());
    }

    public static String getProcessFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return IFileDataProvider.PROCESS_FILE_PROTOCOL + fileName;
        }
        return fileName;
    }

    public static void deleteProcessFile(String path) {
        if (isProcessFile(path)) {
            String fileName = getProcessFileName(path);
            deleteProcessFile(getProcessFile(fileName));
        }
    }

    public static void deleteProcessFile(IFile file) {
        if (file.exists()) {
            try {
                file.delete(true, null);
            } catch (CoreException e) {
                PluginLogger.logError("Unable to delete file " + file + " from process definition", e);
            }
        }
    }

}
