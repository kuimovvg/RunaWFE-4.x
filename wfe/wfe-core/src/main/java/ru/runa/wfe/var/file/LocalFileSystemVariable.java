package ru.runa.wfe.var.file;

import java.io.File;
import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.Variable;

import com.google.common.io.Files;

/**
 * This class eliminates need to persist large bytes array in database.
 * 
 * @author dofs
 * @since 4.0
 */
public class LocalFileSystemVariable implements IFileVariable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String contentType;
    private transient byte[] data;
    private String variablePath;

    public LocalFileSystemVariable() {
    }

    public LocalFileSystemVariable(Variable<?> variable, String variableName, IFileVariable fileVariable) {
        name = fileVariable.getName();
        contentType = fileVariable.getContentType();
        long version = variable.getVersion() != null ? variable.getVersion() + 1 : 0;
        StringBuffer b = new StringBuffer();
        for (char ch : variableName.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                b.append(ch);
            } else {
                b.append('_');
            }
        }
        variablePath = variable.getProcess().getId() + "/" + b + "/" + version;
        data = fileVariable.getData();
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public byte[] getData() {
        if (data == null) {
            File file = LocalFileSystemStorage.getContentFile(variablePath, false);
            try {
                data = Files.toByteArray(file);
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to read file variable from '" + file + "'", e);
            }
        }
        return data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

}
