package ru.runa.wfe.definition.par;

import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.IFileDataProvider;

import com.google.common.base.Preconditions;

public abstract class FileDataProvider implements IFileDataProvider {

    @Override
    public byte[] getFileDataNotNull(String fileName) {
        byte[] data = getFileData(fileName);
        if (data == null) {
            throw new DefinitionFileDoesNotExistException(fileName);
        }
        Preconditions.checkNotNull(data, "no '" + fileName + "' inside process archive");
        return data;
    }

}
