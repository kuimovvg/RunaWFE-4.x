package ru.runa.gpd.property;

import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.handler.LocalizationRegistry;

public class LocalizationLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        // space symbol used for alignment in properties view 
        return " " + LocalizationRegistry.getLabel(super.getText(element));
    }

}
