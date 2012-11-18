package ru.runa.gpd.property;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.util.LocalizationRegistry;

public class MappingLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        // space symbol used for alignment in properties view 
        return " " + LocalizationRegistry.getTypeName(super.getText(element));
    }

}
