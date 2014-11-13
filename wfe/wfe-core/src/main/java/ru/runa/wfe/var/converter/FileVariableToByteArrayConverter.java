package ru.runa.wfe.var.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.file.IFileVariableStorage;

/**
 * Besides straightforward functionality this class persist large file variables
 * in local disc storage.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter {
    private static final long serialVersionUID = 1L;
    private IFileVariableStorage storage;

    @Required
    public void setStorage(IFileVariableStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Object value) {
        return value instanceof IFileVariable || value instanceof List;
    }

    @Override
    public Object convert(Variable<?> variable, Object object) {
        object = storage.save(variable, object);
        return super.convert(variable, object);
    }

}
