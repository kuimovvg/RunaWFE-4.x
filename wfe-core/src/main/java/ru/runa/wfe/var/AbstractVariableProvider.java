package ru.runa.wfe.var;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Base implementation of {@link IVariableProvider}.
 * 
 * @author Dofs
 * @since 4.0
 */
public abstract class AbstractVariableProvider implements IVariableProvider {

    @Override
    public Object getValueNotNull(String variableName) throws VariableDoesNotExistException {
        Object object = getValue(variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

    @Override
    public <T> T getValue(Class<T> clazz, String variableName) {
        Object object = getValue(variableName);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    @Override
    public <T> T getValueNotNull(Class<T> clazz, String variableName) throws VariableDoesNotExistException {
        T object = getValue(clazz, variableName);
        if (object == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return object;
    }

    @Override
    public WfVariable getVariableNotNull(String variableName) throws VariableDoesNotExistException {
        WfVariable variable = getVariable(variableName);
        if (variable == null) {
            throw new VariableDoesNotExistException(variableName);
        }
        return null;
    }
}
