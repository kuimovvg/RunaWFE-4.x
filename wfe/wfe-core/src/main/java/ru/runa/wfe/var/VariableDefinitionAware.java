package ru.runa.wfe.var;

/**
 * Marker interface for objects which depends of variable definition. 
 * Currently used in some formats.
 * 
 * @author dofs
 * @since 4.1.0
 */
public interface VariableDefinitionAware {

    public void setVariableDefinition(VariableDefinition variableDefinition);

    public VariableDefinition getVariableDefinition();
    
}
