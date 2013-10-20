package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;
import java.util.Map;

import ru.runa.gpd.lang.model.Variable;

public interface FreemarkerTagValidator {
	List<ValidationMessage> validateTag(FreemarkerTag freemarkerTag, Map<String, Variable> variables);
}
