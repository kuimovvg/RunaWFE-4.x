package ru.runa.wfe.var;

import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Base implementation of {@link IVariableProvider}.
 * 
 * @author Dofs
 * @since 4.0
 */
public abstract class AbstractVariableProvider implements IVariableProvider {

    @Override
    public Object getNotNull(String variableName) throws VariableDoesNotExistException {
        Object object = get(variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

    @Override
    public <T> T get(Class<T> clazz, String variableName) {
        Object object = get(variableName);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    @Override
    public <T> T getNotNull(Class<T> clazz, String variableName) throws VariableDoesNotExistException {
        T object = get(clazz, variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

}
