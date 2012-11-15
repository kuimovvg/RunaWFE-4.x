package ru.runa.gpd.orgfunction;

public interface ISwimlaneElementListener {

	public void opened(String path);
	
	public void completed(String path, OrgFunctionDefinition definition);
	
}
