package ru.cg.runaex.components_plugin.component_parameter;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components_plugin.property_editor.database.TableReferencePropertyDescriptor;

public class TableReferenceParameter extends ComponentParameter<TableReference> {

    @Override
    protected String convertValueToString() {
        return rawValue.toString();
    }

    @Override
    public TableReference getNullValue() {
        return new TableReference("", "", -1);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        return new TableReferencePropertyDescriptor(propertyId, param.label);
    }

    @Override
    protected TableReference convertValueFromString(String valueStr) {
        TableReference reference = ComponentParser.parseTableReference(valueStr, null);

        if (reference != null) {
            String schema = reference.getSchema() != null ? reference.getSchema() : "";
            String table = reference.getTable() != null ? reference.getTable() : "";

            return new TableReference(schema, table, reference.getTermCount());
        }

        return new TableReference("", "", 0);
    }

}