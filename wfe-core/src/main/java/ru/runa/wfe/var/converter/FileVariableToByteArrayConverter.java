package ru.runa.wfe.var.converter;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.FileVariableDescriptor;
import ru.runa.wfe.var.Variable;

import com.google.common.io.Files;

/**
 * Besides straightforward functionality this class persist large file variables
 * in local disc storage.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter implements InitializingBean {
    private static final long serialVersionUID = 1L;
    private File storageDir = new File(SystemProperties.getLocalFileStoragePath());

    @Override
    public void afterPropertiesSet() throws Exception {
        if (SystemProperties.isLocalFileStorageEnabled()) {
            storageDir.mkdirs();
        }
    }

    @Override
    public boolean supports(Object value) {
        return value instanceof FileVariable;
    }

    private File getContentFile(FileVariableDescriptor descriptor) throws IOException {
        File file = new File(storageDir, descriptor.getVariablePath());
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    @Override
    public Object convert(Variable<?> variable, Object o) {
        FileVariable fileVariable = (FileVariable) o;
        if (SystemProperties.isLocalFileStorageEnabled() && fileVariable.getData().length > SystemProperties.getLocalFileStorageFileLimit()) {
            try {
                FileVariableDescriptor descriptor = new FileVariableDescriptor(variable, fileVariable);
                Files.write(descriptor.getData(), getContentFile(descriptor));
                return super.convert(variable, descriptor);
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to save file variable to local drive", e);
            }
        }
        return super.convert(variable, o);
    }

    @Override
    public Object revert(Object o) {
        FileVariable fileVariable = (FileVariable) super.revert(o);
        if (fileVariable instanceof FileVariableDescriptor) {
            FileVariableDescriptor descriptor = (FileVariableDescriptor) fileVariable;
            try {
                descriptor.setData(Files.toByteArray(getContentFile(descriptor)));
            } catch (IOException e) {
                throw new InternalApplicationException("Unable to read file variable from local drive", e);
            }
            return descriptor;
        }
        return fileVariable;
    }

}