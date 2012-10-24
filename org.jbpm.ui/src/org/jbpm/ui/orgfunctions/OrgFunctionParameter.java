package org.jbpm.ui.orgfunctions;

public class OrgFunctionParameter {
    public static final String TEXT_INPUT = "string";
    public static final String NUMBER_INPUT = "long";

    private final String name;

    private final String type;

    private String value = "";

    private final boolean multiple;

    private boolean transientParam = false;
    private boolean useVariable = false;

    public OrgFunctionParameter(String name, String type, boolean multiple) {
        this.name = name;
        this.type = type;
        this.multiple = multiple;
    }

    public String getValue() {
        return value;
    }

    public String getVariableName() {
        if (value.length() > 3) {
            return value.substring(2, value.length()-1);
        }
        return "";
    }

    public void setValue(String value) {
        this.value = value;
        useVariable = (value.length() > 3 && "${".equals(value.substring(0, 2)) && value.endsWith("}"));
        if (value.length() > 0 && NUMBER_INPUT.equals(type) && !useVariable) {
        	//check type
        	Long.parseLong(value);
        }
    }

	public void setVariableValue(String variableName) {
		this.value = "${" + variableName + "}";
		this.useVariable = true;
	}

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public boolean isTransientParam() {
        return transientParam;
    }

    public boolean isUseVariable() {
		return useVariable;
	}

	public void setTransientParam(boolean transientParam) {
        this.transientParam = transientParam;
    }

    protected OrgFunctionParameter getCopy() {
        OrgFunctionParameter copy = new OrgFunctionParameter(name, type, multiple);
        copy.value = value;
        copy.transientParam = transientParam;
        copy.useVariable = useVariable;
        return copy;
    }
}
