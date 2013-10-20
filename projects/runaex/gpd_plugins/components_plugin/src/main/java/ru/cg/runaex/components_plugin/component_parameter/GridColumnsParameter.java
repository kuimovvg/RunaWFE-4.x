package ru.cg.runaex.components_plugin.component_parameter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.util.ComponentSerializer;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.GridColumnsPropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

public class GridColumnsParameter extends ComponentParameter<List<GridColumn>> {

    @Override
    protected String convertValueToString() {
        return ComponentSerializer.serializeGridColumns(rawValue);
    }

    @Override
    protected List<GridColumn> convertValueFromString(String valueStr) {
        return ComponentParser.parseGridColumns(valueStr, null);
    }

    @Override
    public List<GridColumn> getNullValue() {
        return new ArrayList<GridColumn>();
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new GridColumnsPropertyDescriptor(propertyId, param.label);
    }
    
}
