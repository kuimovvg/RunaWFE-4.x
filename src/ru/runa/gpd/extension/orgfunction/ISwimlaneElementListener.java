package ru.runa.gpd.extension.orgfunction;

public interface ISwimlaneElementListener {

	public void opened(String path);
	
	public void completed(String path, OrgFunctionDefinition definition);
	
}
