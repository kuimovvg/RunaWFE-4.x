package ru.runa.wfe.var;

import java.io.File;
import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.io.Files;

/**
 * This class eliminates need to persist large bytes array in database.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableDescriptor extends FileVariable {
    private static final long serialVersionUID = 1L;
    private String variablePath;
    private transient byte[] localData;

    public FileVariableDescriptor() {
    }

    public FileVariableDescriptor(Variable<?> variable, String variableName, FileVariable fileVariable) {
        super(fileVariable.getName(), null, fileVariable.getContentType());
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
        localData = fileVariable.getData();
    }

    public FileVariableDescriptor(Variable<?> variable, FileVariable fileVariable) {
        this(variable, variable.getName(), fileVariable);
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public byte[] getData() {
        if (localData == null) {
            File file = FileVariableStorage.getContentFile(this, false);
            try {
                localData = Files.toByteArray(file);
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to read file variable from '" + file + "'", e);
            }
        }
        return localData;
    }

}
