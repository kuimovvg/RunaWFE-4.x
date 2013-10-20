package ru.cg.runaex.components_plugin.component_parameter.descriptor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.database_property_editor.LabelDialogCellEditor;

public class GridColumnsPropertyDescriptor extends PropertyDescriptor {

    public GridColumnsPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);

        this.setLabelProvider(new GridColumnsLabelProvider<GridColumn>());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new GridColumnsCellEditor(parent);
    }

    private static class GridColumnsCellEditor extends LabelDialogCellEditor {

        public GridColumnsCellEditor(Composite parent) {
            super(parent);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<GridColumn> openDialogBox(Control cellEditorWindow) {
            GridColumnsDialog dialog = new GridColumnsDialog();
            dialog.setGridColumns((List<GridColumn>) getValue());
            return dialog.openDialog();
        }

        @Override
        public ILabelProvider getLabelProvider() {
            return new GridColumnsLabelProvider<GridColumn>();
        }
    }

    private static class GridColumnsPanel extends BaseGridColumnsPanel<GridColumn> {
        private static String[] columnNames = new String[] { DISPLAY_NAME_COLUMN, DATABASE_COLUMN, REFERENCE_COLUMN, WIDTH_COLUMN };

        public GridColumnsPanel(Composite parent, int style, Localization localization) {
            super(parent, style, localization);
        }

        @Override
        public GridColumn getEmptyGridColumn() {
            return new GridColumn("", "", null, 4, "");
        }

        @Override
        public ICellModifier getCellModifier() {
            return new GridColumnTableCellModifier(getColumnNames(), viewer);
        }

        @Override
        public ITableLabelProvider getTableLabelProvider() {
            return new GridColumnLabelProvider();
        }

        protected String[] getColumnNames() {
            return columnNames;
        }

    }

    private static class GridColumnsDialog extends Dialog {
        private GridColumnsPanel gridColumnsPanel;
        private List<GridColumn> columns;
        private Localization localization = DescriptorLocalizationFactory.getGridColumnsDialogLocalization();

        protected GridColumnsDialog() {
            super(Display.getCurrent().getActiveShell());
        }

        public List<GridColumn> openDialog() {
            try {
                if (open() != IDialogConstants.CANCEL_ID) {
                    return gridColumnsPanel.getGridColumns();
                }
            } catch (Exception e) {
                // ignore this and return null;
            }
            return null;
        }

        public void setGridColumns(List<GridColumn> columns) {
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

            gridColumnsPanel = new GridColumnsPanel(parent, SWT.NONE, localization);
            gridColumnsPanel.setLayout(new GridLayout(1, false));
            gridColumnsPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

            initValues();

            return gridColumnsPanel;
        }

        private void initValues() {
            gridColumnsPanel.setGridColumns(columns);
        }

    }

    private static class GridColumnLabelProvider extends LabelProvider implements ITableLabelProvider {

        public GridColumnLabelProvider() {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            GridColumn column = (GridColumn) element;

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
                return column.getWidth();
            }
            return null;
        }
    }

    private static class GridColumnTableCellModifier implements ICellModifier {
        private TableViewer viewer;
        private String[] columnNames;

        private GridColumnTableCellModifier(String[] columnNames, TableViewer viewer) {
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
            GridColumn parameter = (GridColumn) element;

            switch (columnIndex) {
            case 0:
                return parameter.getDisplayName();
            case 1:
                return new ColumnReference("", "", parameter.getDatabaseColumn(), 1);
            case 2:
                return parameter.getColumnReference();
            case 3:
                return parameter.getWidth();
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            int columnIndex = Arrays.asList(columnNames).indexOf(property);

            TableItem item = (TableItem) element;
            GridColumn column = (GridColumn) item.getData();

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
                column.setWidth(((String) value).trim());
                break;
            }

            viewer.update(column, null);
        }

    }
}
