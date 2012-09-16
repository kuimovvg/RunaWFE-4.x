package ru.runa.bpm.ui.properties;

import org.eclipse.jface.viewers.LabelProvider;
import ru.runa.bpm.ui.util.TypeNameMapping;

public class MappingLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        // space symbol used for alignment in properties view 
        return " " + TypeNameMapping.getTypeName(super.getText(element));
    }

}
