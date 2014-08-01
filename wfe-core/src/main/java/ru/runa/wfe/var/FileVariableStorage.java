package ru.runa.wfe.var;

import java.io.File;
import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;

public class FileVariableStorage {
    private static File storageDir = new File(SystemProperties.getLocalFileStoragePath());

    static {
        if (SystemProperties.isLocalFileStorageEnabled()) {
            storageDir.mkdirs();
        }
    }

    public static File getContentFile(FileVariableDescriptor descriptor, boolean create) {
        File file = new File(storageDir, descriptor.getVariablePath());
        if (create) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to create file '" + file + "'");
            }
        }
        if (!file.exists()) {
            throw new InternalApplicationException("No file found by path '" + file + "'");
        }
        return file;
    }

}
