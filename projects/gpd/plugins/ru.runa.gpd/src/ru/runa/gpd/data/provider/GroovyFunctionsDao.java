package ru.runa.gpd.data.provider;

import org.eclipse.core.resources.IProject;

import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;

public interface GroovyFunctionsDao {
	
	boolean save(IProject project,GroovyFunctionList functionList);
	
	GroovyFunctionList get(IProject project);
}
