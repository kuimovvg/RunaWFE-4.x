package ru.cg.runaex.components_plugin.component_parameter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

import ru.cg.runaex.components.bean.component.part.EditableTreeGridColumn;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.util.ComponentSerializer;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.EditableTreeGridColumnsPropertyDescriptor;

public class EditableTreeGridColumnsParameter extends ComponentParameter<List<EditableTreeGridColumn>> {

    @Override
    protected String convertValueToString() {
        return ComponentSerializer.serializeEditableTreeGridColumns(rawValue);
    }

    @Override
    protected List<EditableTreeGridColumn> convertValueFromString(String valueStr) {
        return ComponentParser.parseEditableTreeGridColumns(valueStr, null);
    }

    @Override
    public List<EditableTreeGridColumn> getNullValue() {
        return new ArrayList<EditableTreeGridColumn>();
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new EditableTreeGridColumnsPropertyDescriptor(propertyId, param.label);
    }

}
