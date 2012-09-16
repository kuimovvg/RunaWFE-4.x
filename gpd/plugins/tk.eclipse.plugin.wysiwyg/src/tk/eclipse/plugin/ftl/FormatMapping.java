package tk.eclipse.plugin.ftl;

public class FormatMapping {
	private final String typeName;
	private final String name;
	
	public FormatMapping(String typeName, String name) {
		this.typeName = typeName;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}
	
}
