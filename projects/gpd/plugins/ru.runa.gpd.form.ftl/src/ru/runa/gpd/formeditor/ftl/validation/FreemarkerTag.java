package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

public class FreemarkerTag {
	private String name;
	private List<String> parameters;
	
	public FreemarkerTag(String name, List<String> parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getParameters() {
		return parameters;
	}

}
