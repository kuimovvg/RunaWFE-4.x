package ru.runa.wfe.var.format;

/**
 * Variable format allows convertions between Strings and Objects. Each variable
 * in process definition bound to specific format.
 * 
 * @author dofs
 * @since 4.0
 */
public interface VariableFormat<T> {

    public Class<?> getJavaClass();

    /**
     * Parses variable object from strings. Array of strings here due to
     * conversation from html form.
     * 
     * @param source
     *            array of strings.
     * @return object, can be <code>null</code>
     * @throws Exception
     */
    public T parse(String[] source) throws Exception;

    /**
     * Formats given variable object.
     * 
     * @param object
     *            object, can be <code>null</code>
     * @return formatted string, can be <code>null</code>
     */
    public String format(T object);
}
