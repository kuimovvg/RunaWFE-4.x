package ru.runa.gpd.formeditor.ftl.view;

import java.util.List;

public interface IToolPalleteDropEditor {
	
	public void doDrop(String tagName);
	
	public List<ToolPalleteMethodTag> getAllMethods();
}
