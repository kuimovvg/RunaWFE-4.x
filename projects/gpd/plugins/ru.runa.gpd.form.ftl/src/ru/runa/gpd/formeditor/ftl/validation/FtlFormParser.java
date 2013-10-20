package ru.runa.gpd.formeditor.ftl.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FtlFormParser {
	private static final Pattern COMPONENT_PATTERN = Pattern.compile("\\$\\{((.+?)\\((.*?)\\))\\}", Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.MULTILINE);
	private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?:(?:^\")|(?:(?<!\\\\)\"))(.*?)(?<!\\\\)\"");

	public static List<FreemarkerTag> getFreemarkerTags(String form) {
		List<FreemarkerTag> components = new LinkedList<FreemarkerTag>();

	    Matcher matcher = COMPONENT_PATTERN.matcher(form);
	    while (matcher.find()) {
	      String componentName = matcher.group(2);
	      String parametersStr = matcher.group(3);

	      Matcher parameterMatcher = PARAMETER_PATTERN.matcher(parametersStr);

	      List<String> parameters = new LinkedList<String>();
	      String parameter;

	      while (parameterMatcher.find()) {
	        parameter = parameterMatcher.group(1);
	        if (parameter != null) {
	        	parameter = parameter.trim();
	        	if(parameter.isEmpty())
	        		parameter = null;
	        }
	        parameters.add(parameter);
	      }

	      components.add(new FreemarkerTag(componentName, parameters));
	    }
		
		
		return components;
	}
}
