package ru.runa.gpd.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.wfe.definition.IFileDataProvider;

import com.google.common.base.Strings;

public class EmbeddedFileUtils {

    public static IFile getProcessFile(String fileName) {
        return IOUtils.getFile(fileName);
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

    public static IFile getBotTaskFile(String fileName) {
        return IOUtils.getFile(fileName);
    }

    public static boolean isBotTaskFile(String path) {
        return path != null && path.startsWith(IFileDataProvider.BOT_TASK_FILE_PROTOCOL);
    }

    public static boolean isBotTaskFileName(String fileName, String botTaskName) {
        if (fileName.startsWith(botTaskName + BotTaskUtils.EMBEDDED_SUFFIX)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getBotTaskFileName(String path) {
        return path.substring(IFileDataProvider.BOT_TASK_FILE_PROTOCOL.length());
    }

    public static String getBotTaskFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return IFileDataProvider.BOT_TASK_FILE_PROTOCOL + fileName;
        }
        return fileName;
    }

    public static void deleteBotTaskFile(String path) {
        if (isBotTaskFile(path)) {
            String fileName = getBotTaskFileName(path);
            deleteBotTaskFile(getBotTaskFile(fileName));
        }
    }

    public static void deleteBotTaskFile(IFile file) {
        if (file.exists()) {
            try {
                file.delete(true, null);
            } catch (CoreException e) {
                PluginLogger.logError("Unable to delete file " + file + " from bot task", e);
            }
        }
    }

    public static String generateEmbeddedFileName(Delegable delegable, String fileExtension) {
        if (delegable instanceof GraphElement) {
            String id = ((GraphElement) delegable).getId();
            return id + ".template." + fileExtension;
        }
        if (delegable instanceof BotTask) {
            return ((BotTask) delegable).getName() + BotTaskUtils.EMBEDDED_SUFFIX + "." + fileExtension;
        }
        return null;
    }

}
