package ru.runa.wfe.var.format;

/**
 * Variable format allows convertions between Strings and Objects. Each variable
 * in process definition bound to specific format.
 * 
 * @author dofs
 * @since 4.0
 */
public interface VariableFormat {

    public Class<?> getJavaClass();

    public String getName();

    /**
     * Parses variable object from strings. Array of strings here due to
     * conversation from html form.
     * 
     * @param source
     *            serialized string.
     * @return object, can be <code>null</code>
     * @throws Exception
     */
    public Object parse(String source) throws Exception;

    /**
     * Formats given variable object.
     * 
     * @param object
     *            object, can be <code>null</code>
     * @return formatted string or <code>null</code>
     */
    public String format(Object object);
}
