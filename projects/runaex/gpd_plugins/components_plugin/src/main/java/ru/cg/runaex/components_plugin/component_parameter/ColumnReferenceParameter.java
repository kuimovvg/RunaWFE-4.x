package ru.cg.runaex.components_plugin.component_parameter;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components_plugin.property_editor.database.ColumnReferencePropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;

public class ColumnReferenceParameter extends ComponentParameter<ColumnReference> {

    @Override
    protected String convertValueToString() {
        return rawValue.toString();
    }

    @Override
    public ColumnReference getNullValue() {
        return new ColumnReference("", "", "", -1);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new ColumnReferencePropertyDescriptor(propertyId, param.label);
    }

    @Override
    protected ColumnReference convertValueFromString(String valueStr) {
        ColumnReference reference = ComponentParser.parseColumnReference(valueStr, null);

        if (reference != null) {
            String schema = reference.getSchema() != null ? reference.getSchema() : "";
            String table = reference.getTable() != null ? reference.getTable() : "";
            String column = reference.getColumn() != null ? reference.getColumn() : "";
            return new ColumnReference(schema, table, column, reference.getTermCount());
        }

        return new ColumnReference("", "", "", 0);
    }

}