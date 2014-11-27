package ru.runa.wfe.var.format;

import java.util.Map;

import ru.runa.wfe.var.VariableUserType;

/**
 * Container marker. Used for lists, maps, etc.
 * 
 * @author dofs
 * @since 4.0.5
 */
public interface VariableFormatContainer {
    public static final String COMPONENT_QUALIFIER_START = "[";
    public static final String COMPONENT_QUALIFIER_END = "]";

    /**
     * @return component format by index.
     */
    public String getComponentClassName(int index);

    /**
     * Sets component formats.
     */
    public void setComponentClassNames(String[] componentClassNames);

    /**
     * @return all user types for this process definition
     */
    public Map<String, VariableUserType> getUserTypes();

    public void setUserTypes(Map<String, VariableUserType> userTypes);

}
