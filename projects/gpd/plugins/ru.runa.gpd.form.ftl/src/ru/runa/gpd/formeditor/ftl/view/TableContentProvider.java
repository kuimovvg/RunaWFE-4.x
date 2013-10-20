package ru.runa.gpd.formeditor.ftl.view;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class TableContentProvider implements IStructuredContentProvider {

	@SuppressWarnings("unchecked")
    @Override
	public Object[] getElements(Object inputElement) {
		List<ToolPalleteMethodTag> list = (List<ToolPalleteMethodTag>) inputElement;
		return list.toArray();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}