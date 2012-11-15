package ru.runa.gpd.orgfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class OrgFunctionDefinition {
    public static final String DEFAULT_KEY = "EmptyOrgFunctionName";
    public static final String MISSED_DEFINITION = "Missed OrgFunction definition";

    public static final OrgFunctionDefinition DEFAULT = new OrgFunctionDefinition(DEFAULT_KEY, "");

    private final String key;
    private final String name;
    private final String className;
    private String relationName;

    private final List<OrgFunctionParameter> parameters = new ArrayList<OrgFunctionParameter>();

    public OrgFunctionDefinition(String key, String className) {
    	this.key = key;
        this.name = Localization.getString(key);
        this.className = className;
    }

    protected void checkMultipleParameters() {
        String multipleParamName = null;
        int multipleParamIndex = -1;
        for (int i = 0; i < parameters.size(); i++) {
            OrgFunctionParameter param = parameters.get(i);
            if (param.isMultiple()) {
                multipleParamIndex = i;
                multipleParamName = param.getName();
                break;
            }
        }
        // Now check all next parameters are with same name.
        // Set to them 'transientParam' property
        if (multipleParamIndex != -1) {
            for (int i = multipleParamIndex + 1; i < parameters.size(); i++) {
                OrgFunctionParameter param = parameters.get(i);
                if (!param.isMultiple() || !multipleParamName.equals(param.getName())) {
                    throw new RuntimeException("Misconfiguration in orgfunction definition: " + className);
                }
                // param.setTransientParam(true);
            }
        }
    }
    
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
    
    public String getRelationName() {
        return relationName;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
		return key;
	}

	public String getClassName() {
        return className;
    }

    public List<OrgFunctionParameter> getParameters() {
        return parameters;
    }

    public OrgFunctionParameter getParameter(String name) {
        for (OrgFunctionParameter parameter : parameters)
        	if (name.equals(parameter.getName()))
        		return parameter;
        return null;
    }

    public void propagateParameter(OrgFunctionParameter parameter, int count) {
        for (int i = 0; i < count; i++) {
            OrgFunctionParameter copy = parameter.getCopy();
            copy.setValue("");
            copy.setTransientParam(true);
            addParameter(copy);
        }
    }

    public void addParameter(OrgFunctionParameter parameter) {
        parameters.add(parameter);
    }

    public void removeParameter(OrgFunctionParameter parameter) {
        if (!parameter.isTransientParam()) {
            throw new RuntimeException("Trying to remove non-transient parameter");
        }
        parameters.remove(parameter);
    }

    public OrgFunctionDefinition getCopy() {
        OrgFunctionDefinition copy = new OrgFunctionDefinition(key, className);
        for (OrgFunctionParameter parameter : parameters) {
            copy.addParameter(parameter.getCopy());
        }
        return copy;
    }

    public List<String> getErrors(ProcessDefinition processDefinition) {
        List<String> errors = new ArrayList<String>();
        for (OrgFunctionParameter p : parameters) {
            String value = p.getValue();
            if (value.length() == 0) {
                errors.add("orgfunction.emptyParam");
            }
        	if (p.isUseVariable()) {
                Set<String> variableNames = OrgFunctionsRegistry.getVariableNames(processDefinition, p.getType());
                if (!variableNames.contains(p.getVariableName())) {
                    errors.add("orgfunction.varSelectorItemNotExist");
                }
        	}
        }
        return errors;
    }

	public String createSwimlaneConfiguration() {
        if (DEFAULT == this) {
            // special case, without initializer
            return "";
        }
        StringBuffer result = new StringBuffer();
        if (relationName != null) {
            result.append("@").append(relationName).append("(");
        }
        result.append(className).append("(");
        boolean first = true;
        for (OrgFunctionParameter parameter : parameters) {
            if (!first) {
                result.append(",");
            }
            first = false;
            result.append(parameter.getValue());
        }
        result.append(")");
        if (relationName != null) {
            result.append(")");
        }
        return result.toString();
    }

}
