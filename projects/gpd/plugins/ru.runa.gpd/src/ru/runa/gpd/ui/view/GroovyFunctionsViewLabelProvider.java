package ru.runa.gpd.ui.view;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ru.cg.runaex.shared.bean.project.xml.GroovyFunction;

public class GroovyFunctionsViewLabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        GroovyFunction function = (GroovyFunction) element;
        switch (columnIndex) {
        case 0:
            return function.getCode();
        case 1:
            return function.getDescription();
        }
        return "";
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener arg0) {

    }

    @Override
    public void removeListener(ILabelProviderListener arg0) {

    }
}
