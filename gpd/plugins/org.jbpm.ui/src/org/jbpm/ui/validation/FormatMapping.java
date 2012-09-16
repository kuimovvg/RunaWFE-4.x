package ru.runa.bpm.ui.validation;

public class FormatMapping {
    private final String typeName;
    private final String name;
    private final String javaType;

    public FormatMapping(String typeName, String name, String javaType) {
        this.typeName = typeName;
        this.name = name;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

	public String getJavaType() {
		return javaType;
	}

}
