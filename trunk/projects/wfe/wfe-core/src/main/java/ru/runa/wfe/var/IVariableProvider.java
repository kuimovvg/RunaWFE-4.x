package ru.runa.wfe.var;

/**
 * Access to variables.
 * 
 * @author Dofs
 * @since 4.0
 */
public interface IVariableProvider {

    Object get(String variableName);

    Object getNotNull(String variableName) throws VariableDoesNotExistException;

    <T extends Object> T get(Class<T> clazz, String variableName);

    <T extends Object> T getNotNull(Class<T> clazz, String variableName) throws VariableDoesNotExistException;

}
