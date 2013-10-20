/*
 * Created on 24.09.2005
 */
package ru.runa.gpd.ui.view;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunction;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;
import ru.runa.gpd.data.DaoFactory;

/**
 * @author Kochetkov
 */
public class GroovyFunctionsViewContentProvider implements IStructuredContentProvider {

	private GroovyFunctionList functionList = new GroovyFunctionList();
	private IProject project;

	public GroovyFunctionsViewContentProvider(IProject project) {
		this.project = project;
		functionList.setGroovyFunctionList(new LinkedList<GroovyFunction>());
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			if (project != null) {
				functionList = DaoFactory.getGroovyFunctionsDao().get(project);
			}
			else{
				return new Object[] {};
		}

			return functionList.getGroovyFunctionList().toArray();
		} catch (Exception e) {
			return new Object[] {};
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void saveToList(GroovyFunction function) {
		List<GroovyFunction> groovyFunctionList = functionList.getGroovyFunctionList();
		if (!groovyFunctionList.contains(function)) {
			groovyFunctionList.add(function);			
		}		
		DaoFactory.getGroovyFunctionsDao().save(project, functionList);
	}

	public void removeFromList(GroovyFunction function) {
		if (functionList.getGroovyFunctionList() != null) {
			functionList.getGroovyFunctionList().remove(function);
			DaoFactory.getGroovyFunctionsDao().save(project, functionList);
		}
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IProject getProject() {
		return this.project;
	}	
}
