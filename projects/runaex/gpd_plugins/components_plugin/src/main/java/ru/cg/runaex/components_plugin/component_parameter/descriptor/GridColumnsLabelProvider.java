package ru.cg.runaex.components_plugin.component_parameter.descriptor;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.components.bean.component.part.GridColumn;

public class GridColumnsLabelProvider<T extends GridColumn> extends LabelProvider {

    @SuppressWarnings("unchecked")
    @Override
    public String getText(Object element) {
        List<T> gridColumns = (List<T>) element;

        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (T column : gridColumns) {
            if (!first)
                text.append(", ");
            else
                first = false;

            text.append(column.getDisplayName());
        }

        return text.toString();
    }

}
