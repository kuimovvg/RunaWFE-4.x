package ru.runa.gpd.quick.formeditor;

import java.util.ArrayList;
import java.util.List;

public class QuickForm {
	private String name;
	private String delegationClassName = "";
    private String delegationConfiguration = "";
    List<QuickFormGpdVariable> quickFormGpdVariable;
    public QuickForm() {
    	quickFormGpdVariable = new ArrayList<QuickFormGpdVariable>();
    }
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDelegationClassName() {
		return delegationClassName;
	}
	public void setDelegationClassName(String delegationClassName) {
		this.delegationClassName = delegationClassName;
	}
	public String getDelegationConfiguration() {
		return delegationConfiguration;
	}
	public void setDelegationConfiguration(String delegationConfiguration) {
		this.delegationConfiguration = delegationConfiguration;
	}
	public List<QuickFormGpdVariable> getQuickFormGpdVariable() {
		return quickFormGpdVariable;
	}
	public void setQuickFormGpdVariable(
			List<QuickFormGpdVariable> quickFormGpdVariable) {
		this.quickFormGpdVariable = quickFormGpdVariable;
	}
    
    
    
}
