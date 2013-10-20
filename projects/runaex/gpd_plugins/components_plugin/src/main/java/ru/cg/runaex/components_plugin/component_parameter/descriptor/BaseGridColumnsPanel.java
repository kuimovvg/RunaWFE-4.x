package ru.cg.runaex.components_plugin.component_parameter.descriptor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.property_editor.database.ColumnReferencePropertyCellEditor;

public abstract class BaseGridColumnsPanel<T extends GridColumn> extends Composite {
    protected TableViewer viewer;
    protected List<T> columns;
    protected Button remove;

    protected Localization localization;
    protected static final String DISPLAY_NAME_COLUMN = "displayName";
    protected static final String DATABASE_COLUMN = "databaseColumn";
    protected static final String REFERENCE_COLUMN = "reference";
    protected static final String WIDTH_COLUMN = "columnWidth";

    public BaseGridColumnsPanel(Composite parent, int style, Localization localization) {
        super(parent, style);
        this.localization = localization;
        this.columns = new ArrayList<T>();

        this.setLayout(new GridLayout(1, false));
        this.setLayoutData(new GridData(GridData.FILL_BOTH));

        createControls();
        createTable();
    }

    private void createControls() {
        Composite buttonPanel = new Composite(this, SWT.NONE);
        RowLayout layout = new RowLayout(SWT.NONE);
        layout.wrap = true;
        layout.pack = true;
        layout.justify = false;
        layout.type = SWT.HORIZONTAL;
        buttonPanel.setLayout(layout);

        Button add = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
        add.setText(localization.get("addButton"));
        add.setToolTipText(localization.get("addButton"));
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                T column = getEmptyGridColumn();
                columns.add(column);
                viewer.add(column);
                viewer.editElement(column, 0);
                remove.setEnabled(true);
            }
        });

        remove = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
        remove.setText(localization.get("removeButton"));
        remove.setToolTipText(localization.get("removeButton"));
        remove.setEnabled(false);
        remove.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GridColumn column = (GridColumn) ((IStructuredSelection) viewer.getSelection()).getFirstElement();

                if (column != null) {
                    viewer.remove(column);
                    columns.remove(column);
                }
                remove.setEnabled(false);
            }
        });
    }

    public abstract T getEmptyGridColumn();

    public abstract ICellModifier getCellModifier();

    public abstract ITableLabelProvider getTableLabelProvider();

    private void createTable() {
        Table table = new Table(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        // parametersTable.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 200;
        table.setLayoutData(data);

        TableColumn column = new TableColumn(table, SWT.NONE, 0);
        column.setText(localization.get("displayName"));
        column.setWidth(200);

        column = new TableColumn(table, SWT.FILL, 1);
        column.setText(localization.get("databaseColumn"));
        column.setWidth(200);

        column = new TableColumn(table, SWT.FILL, 2);
        column.setText(localization.get("columnReference"));
        column.setWidth(200);

        column = new TableColumn(table, SWT.FILL, 3);
        column.setText(localization.get("width"));
        column.setWidth(200);

        createAdditionalColumns(table);

        viewer = new TableViewer(table);
        viewer.setUseHashlookup(true);
        viewer.setColumnProperties(getColumnNames());

        CellEditor[] editors = new CellEditor[getColumnNames().length];
        // ((Text) textEditor.getControl()).setTextLimit(60);
        editors[0] = new TextCellEditor(table);
        editors[1] = new ColumnReferencePropertyCellEditor(table);
        editors[2] = new ColumnReferencePropertyCellEditor(table);
        editors[3] = new TextCellEditor(table);
        createAdditionalEditors(table, editors);

        viewer.setCellEditors(editors);
        viewer.setCellModifier(getCellModifier());

        viewer.setLabelProvider(getTableLabelProvider());
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(columns);

        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                GridColumn gridColumn = null;
                IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
                if (sel != null) {
                    gridColumn = (GridColumn) sel.getFirstElement();
                }
                remove.setEnabled(gridColumn != null);
            }
        });
    }

    protected abstract String[] getColumnNames();

    protected void createAdditionalEditors(Table table, CellEditor[] editors) {
    }

    protected void createAdditionalColumns(Table table) {

    }

    public List<T> getGridColumns() {
        return columns;
    }

    public void setGridColumns(List<T> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
        viewer.refresh();
    }
}