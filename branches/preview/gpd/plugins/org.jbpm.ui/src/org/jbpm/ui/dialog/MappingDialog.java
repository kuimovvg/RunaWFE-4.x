package ru.runa.bpm.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.TypeNameMapping;

public class MappingDialog extends Dialog {

    private TableViewer tableViewer;

    public MappingDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());

        tableViewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 300;
        tableViewer.getControl().setLayoutData(data);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Messages.getString("Mapping.Type"), Messages.getString("property.name") };
        int[] columnWidths = new int[] { 400, 240 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }

        tableViewer.setLabelProvider(new TableItemDataLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setComparator(new ViewerComparator());
        setTableInput();
        return composite;
    }

    private void setTableInput() {
        tableViewer.setInput(createItems(TypeNameMapping.getMapping(), TypeNameMapping.getHiddenMapping()));
        for (int i = 0; i < tableViewer.getTable().getItemCount(); i++) {
            TableItem item = tableViewer.getTable().getItem(i);
            TableItemData data = (TableItemData) item.getData();
            if (data.isHidden()) {
                item.setChecked(false);
            } else {
                item.setChecked(true);
            }
        }
    }

    private List<TableItemData> createItems(Map<String, String> mapping, Map<String, String> hiddenMapping) {
        List<TableItemData> list = new ArrayList<TableItemData>();
        for (String key : mapping.keySet()) {
            TableItemData data = new TableItemData(key, mapping.get(key), false);
            list.add(data);
        }

        for (String key : hiddenMapping.keySet()) {
            TableItemData data = new TableItemData(key, hiddenMapping.get(key), true);
            list.add(data);
        }
        // Collections.sort(list, new TableItemDataComparator());
        return list;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        Button addButton = createButton(parent, 101, Messages.getString("button.add"), false);
        addButton.addSelectionListener(new AddSelectionAdapter());
        final Button editButton = createButton(parent, 102, Messages.getString("button.edit"), false);
        editButton.addSelectionListener(new EditSelectionAdapter());
        final Button deleteButton = createButton(parent, 103, Messages.getString("button.delete"), false);
        deleteButton.addSelectionListener(new DeleteSelectionAdapter());
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                editButton.setEnabled(!tableViewer.getSelection().isEmpty());
                deleteButton.setEnabled(!tableViewer.getSelection().isEmpty());
            }

        });
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("Mapping.title"));
    }

    @Override
    protected void okPressed() {
        saveMappings();
        super.okPressed();
    }

    private void saveMappings() {
        Map<String, String> mapping = new HashMap<String, String>();
        Map<String, String> hiddenMapping = new HashMap<String, String>();
        for (int i = 0; i < tableViewer.getTable().getItemCount(); i++) {
            TableItem item = tableViewer.getTable().getItem(i);
            TableItemData data = (TableItemData) item.getData();
            if (item.getChecked()) {
                mapping.put(data.getType(), data.getName());
            } else {
                hiddenMapping.put(data.getType(), data.getName());
            }
            TypeNameMapping.setMapping(mapping);
            TypeNameMapping.setHiddenMapping(hiddenMapping);
        }
    }

    private class AddSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            UpdateMappingDialog dialog = new UpdateMappingDialog(Display.getCurrent().getActiveShell(), false);
            if (dialog.open() == IDialogConstants.OK_ID) {
                tableViewer.add(new TableItemData(dialog.getType(), dialog.getName(), false));
            }
        }

    }

    private class EditSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            TableItemData data = (TableItemData) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
            UpdateMappingDialog dialog = new UpdateMappingDialog(Display.getCurrent().getActiveShell(), true);
            dialog.setType(data.getType());
            dialog.setName(data.getName());
            if (dialog.open() == IDialogConstants.OK_ID) {
                data.setType(dialog.getType());
                data.setName(dialog.getName());
                // TypeNameMapping.updateMapping(dialog.getType(), dialog.getName());
                tableViewer.refresh(data);
            }
            // setTableInput();
        }

    }

    private class DeleteSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            TableItemData data = (TableItemData) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
            tableViewer.remove(data);
        }

    }

    private static class TableItemDataLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object element, int index) {
            TableItemData data = (TableItemData) element;
            switch (index) {
            case 0:
                return data.getType();
            case 1:
                return data.getName();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public String getText(Object element) {
            TableItemData data = (TableItemData) element;
            return data.getType();
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private static class TableItemData {

        private String type;
        private String name;
        private boolean hidden;

        public TableItemData(String type, String name, boolean hidden) {
            this.type = type;
            this.name = name;
            this.hidden = hidden;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

    }

}
