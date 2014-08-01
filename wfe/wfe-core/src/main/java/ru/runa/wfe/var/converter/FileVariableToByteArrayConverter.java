package ru.runa.wfe.var.converter;

import java.io.IOException;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.FileVariableDescriptor;
import ru.runa.wfe.var.FileVariableStorage;
import ru.runa.wfe.var.Variable;

import com.google.common.io.Files;

/**
 * Besides straightforward functionality this class persist large file variables
 * in local disc storage.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        return value instanceof FileVariable;
    }

    @Override
    public Object convert(Variable<?> variable, Object o) {
        FileVariable fileVariable = (FileVariable) o;
        if (SystemProperties.isLocalFileStorageEnabled() && fileVariable.getData().length > SystemProperties.getLocalFileStorageFileLimit()) {
            try {
                FileVariableDescriptor descriptor = new FileVariableDescriptor(variable, fileVariable);
                Files.write(descriptor.getData(), FileVariableStorage.getContentFile(descriptor, true));
                return super.convert(variable, descriptor);
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to save file variable to local drive", e);
            }
        }
        return super.convert(variable, o);
    }

}
