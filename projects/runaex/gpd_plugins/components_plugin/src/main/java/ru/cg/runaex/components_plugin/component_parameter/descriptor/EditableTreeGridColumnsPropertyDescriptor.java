package ru.cg.runaex.components_plugin.component_parameter.descriptor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.EditableTreeGridColumn;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.database_property_editor.LabelDialogCellEditor;

import ru.runa.gpd.PluginLogger;

public class EditableTreeGridColumnsPropertyDescriptor extends PropertyDescriptor {

    public EditableTreeGridColumnsPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);

        this.setLabelProvider(new GridColumnsLabelProvider<EditableTreeGridColumn>());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new EditableTreeGridColumnsCellEditor(parent);
    }

    private static class EditableTreeGridColumnsCellEditor extends LabelDialogCellEditor {

        public EditableTreeGridColumnsCellEditor(Composite parent) {
            super(parent);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<EditableTreeGridColumn> openDialogBox(Control cellEditorWindow) {
            EditableTreeGridColumnsDialog dialog = new EditableTreeGridColumnsDialog();
            dialog.setGridColumns((List<EditableTreeGridColumn>) getValue());
            return dialog.openDialog();
        }

        @Override
        public ILabelProvider getLabelProvider() {
            return new GridColumnsLabelProvider<EditableTreeGridColumn>();
        }
    }

    private static class EditableTreeGridColumnsPanel extends BaseGridColumnsPanel<EditableTreeGridColumn> {
        private static final String FORMAT_COLUMN = "columnFormat";
        private static String[] columnNames = new String[] { DISPLAY_NAME_COLUMN, DATABASE_COLUMN, REFERENCE_COLUMN, FORMAT_COLUMN, WIDTH_COLUMN };

        public EditableTreeGridColumnsPanel(Composite parent, int style, Localization localization) {
            super(parent, style, localization);
        }

        @Override
        public EditableTreeGridColumn getEmptyGridColumn() {
            return new EditableTreeGridColumn("", "", null, 5, 4, "");
        }

        @Override
        public ICellModifier getCellModifier() {
            return new EditableTreeGridColumnCellModifier(columnNames, viewer);
        }

        @Override
        public ITableLabelProvider getTableLabelProvider() {
            return new EditableTreeGridColumnLabelProvider(localization);
        }

        @Override
        protected void createAdditionalEditors(Table table, CellEditor[] editors) {
            editors[4] = new ComboBoxCellEditor(table, new String[] { "", localization.get("money") });

        }

        protected String[] getColumnNames() {
            return columnNames;
        }

        @Override
        protected void createAdditionalColumns(Table table) {
            TableColumn column = new TableColumn(table, SWT.FILL, 4);
            column.setText(localization.get("columnFormat"));
            column.setWidth(200);
        }

    }

    private static class EditableTreeGridColumnsDialog extends Dialog {
        private EditableTreeGridColumnsPanel gridColumnsPanel;
        private List<EditableTreeGridColumn> columns;
        private Localization localization = DescriptorLocalizationFactory.getGridColumnsDialogLocalization();

        protected EditableTreeGridColumnsDialog() {
            super(Display.getCurrent().getActiveShell());
        }

        public List<EditableTreeGridColumn> openDialog() {
            try {
                if (open() != IDialogConstants.CANCEL_ID) {
                    return gridColumnsPanel.getGridColumns();
                }
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
            return null;
        }

        public void setGridColumns(List<EditableTreeGridColumn> columns) {
            this.columns = columns;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, localization.get("dialog.selectBtn"), true);
            createButton(parent, IDialogConstants.CANCEL_ID, localization.get("dialog.cancelBtn"), false);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText(localization.get("title"));

            gridColumnsPanel = new EditableTreeGridColumnsPanel(parent, SWT.NONE, localization);
            gridColumnsPanel.setLayout(new GridLayout(1, false));
            gridColumnsPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

            initValues();

            return gridColumnsPanel;
        }

        private void initValues() {
            gridColumnsPanel.setGridColumns(columns);
        }

    }

    private static class EditableTreeGridColumnLabelProvider extends LabelProvider implements ITableLabelProvider {
        private Localization localization;

        public EditableTreeGridColumnLabelProvider(Localization localization) {
            this.localization = localization;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            EditableTreeGridColumn column = (EditableTreeGridColumn) element;

            switch (columnIndex) {
            case 0:
                return column.getDisplayName();
            case 1:
                return column.getDatabaseColumn();
            case 2:
                if (column.getColumnReference() != null)
                    return column.getColumnReference().toString();
                else
                    return "";
            case 3:
                switch (column.getColumnFormat()) {
                case 0:
                    return "";
                case 1:
                    return localization.get("money");
                }
            case 4:
                return column.getWidth();
            }
            return null;
        }
    }

    private static class EditableTreeGridColumnCellModifier implements ICellModifier {
        private TableViewer viewer;
        private String[] columnNames;

        private EditableTreeGridColumnCellModifier(String[] columnNames, TableViewer viewer) {
            this.columnNames = columnNames;
            this.viewer = viewer;
        }

        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        @Override
        public Object getValue(Object element, String property) {
            int columnIndex = Arrays.asList(columnNames).indexOf(property);
            EditableTreeGridColumn parameter = (EditableTreeGridColumn) element;

            switch (columnIndex) {
            case 0:
                return parameter.getDisplayName();
            case 1:
                return new ColumnReference("", "", parameter.getDatabaseColumn(), 1);
            case 2:
                return parameter.getColumnReference();
            case 3:
                return parameter.getColumnFormat();
            case 4:
                return parameter.getWidth();
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            int columnIndex = Arrays.asList(columnNames).indexOf(property);

            TableItem item = (TableItem) element;
            EditableTreeGridColumn column = (EditableTreeGridColumn) item.getData();

            switch (columnIndex) {
            case 0:
                column.setDisplayName(((String) value).trim());
                break;
            case 1:
                ColumnReference reference = (ColumnReference) value;
                String databaseColumn = reference != null ? reference.getColumn() : "";
                column.setDatabaseColumn(databaseColumn);
                break;
            case 2:
                column.setColumnReference((ColumnReference) value);
                break;
            case 3:
                column.setColumnFormat((Integer) value);
                break;
            case 4:
                column.setWidth(((String) value).trim());
                break;
            }

            viewer.update(column, null);
        }

    }
}
