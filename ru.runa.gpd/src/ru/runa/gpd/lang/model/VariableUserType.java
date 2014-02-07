package ru.runa.gpd.lang.model;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class VariableUserType extends EventSupport implements VariableContainer, PropertyNames {
	public static final String PREFIX = "usertype:";
    public static final String DELIM = ".";
	private String name;
	private List<Variable> attributes = Lists.newArrayList();
	private ProcessDefinition processDefinition;
	
	public VariableUserType() {
	    
	}
	
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}
	
	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}
	
	@Override
	public void firePropertyChange(String propName, Object old, Object newValue) {
		super.firePropertyChange(propName, old, newValue);
		if (processDefinition != null) {
		    processDefinition.setDirty();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, old, name);
	}
	
	public List<Variable> getAttributes() {
		return attributes;
	}
	
	public void addAttribute(Variable variable) {
		attributes.add(variable);
		firePropertyChange(PROPERTY_CHILDS_CHANGED, null, variable);
	}

	public void removeAttribute(Variable variable) {
		attributes.remove(variable);
		firePropertyChange(PROPERTY_CHILDS_CHANGED, null, variable);
	}

	@Override
	public List<Variable> getVariables(boolean expandComplexTypes,
			boolean includeSwimlanes, String... typeClassNameFilters) {
		Preconditions.checkArgument(!expandComplexTypes, "Complex type expansion is not supported");
		Preconditions.checkArgument(typeClassNameFilters==null || typeClassNameFilters.length==0, "Filtering is not supported");
		return getAttributes();
	}
	
	public VariableUserType getCopy() {
	    VariableUserType type = new VariableUserType();
	    type.name = name;
	    type.attributes.addAll(getAttributes());
	    return type;
	}

	@Override
	public int hashCode() {
	    return Objects.hashCode(name, attributes);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof VariableUserType)) {
	        return false;
	    }
	    VariableUserType type = (VariableUserType) obj;
	    return Objects.equal(name, type.name) && Objects.equal(attributes, type.attributes);
	}
	
	@Override
	public String toString() {
	    return Objects.toStringHelper(getClass()).add("name", name).add("attributes", attributes).toString();
	}
}
