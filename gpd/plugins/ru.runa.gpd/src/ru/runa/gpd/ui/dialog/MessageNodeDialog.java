package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.util.VariableMapping;

public class MessageNodeDialog extends Dialog {
    private final ProcessDefinition definition;
    private final List<VariableMapping> variableMappings;
    private final boolean sendMode;
    private TableViewer selectorTableViewer;
    private TableViewer dataTableViewer;

    public MessageNodeDialog(ProcessDefinition definition, List<VariableMapping> variableMappings, boolean sendMode) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.variableMappings = variableMappings;
        this.definition = definition;
        this.sendMode = sendMode;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        Label label2 = new Label(area, SWT.NO_BACKGROUND);
        label2.setLayoutData(new GridData());
        label2.setText(Localization.getString(sendMode ? "MessageNodeDialog.SelectorSend" : "MessageNodeDialog.SelectorReceive"));
        createSelectorTableViewer(area);
        addSelectorButtons(area);
        Label label1 = new Label(area, SWT.NO_BACKGROUND);
        label1.setLayoutData(new GridData());
        label1.setText(Localization.getString("MessageNodeDialog.VariablesList"));
        createDataTableViewer(area);
        addDataButtons(area);
        return area;
    }

    private void createSelectorTableViewer(Composite parent) {
        selectorTableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.minimumHeight = 100;
        selectorTableViewer.getControl().setLayoutData(data);
        final Table table = selectorTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("property.name"), Localization.getString("property.value") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        selectorTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                }
            }
        });
        selectorTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        selectorTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_SELECTOR));
        selectorTableViewer.setInput(new Object());
    }

    private void addSelectorButtons(Composite parent) {
        final Composite par = parent;
        Composite composite = new Composite(par, SWT.NONE);
        composite.setLayout(new GridLayout(6, false));
        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setText(Localization.getString("button.add"));
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editVariableMapping(null, VariableMapping.USAGE_SELECTOR);
            }
        });
        Button editButton = new Button(composite, SWT.PUSH);
        editButton.setText(Localization.getString("button.edit"));
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                }
            }
        });
        Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setText(Localization.getString("button.delete"));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    variableMappings.remove(mapping);
                    selectorTableViewer.refresh();
                }
            }
        });
        Button addByProcessIdButton = new Button(composite, SWT.PUSH);
        addByProcessIdButton.setText(Localization.getString("MessageNodeDialog.addByProcessId"));
        addByProcessIdButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                VariableMapping mapping = new VariableMapping("processId", "${currentProcessId}", VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping);
                }
            }
        });
        Button addByProcessNameButton = new Button(composite, SWT.PUSH);
        addByProcessNameButton.setText(Localization.getString("MessageNodeDialog.addByProcessName"));
        addByProcessNameButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                VariableMapping mapping = new VariableMapping("processDefinitionName", "${currentDefinitionName}", VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping);
                }
            }
        });
        Button addByNodeNameButton = new Button(composite, SWT.PUSH);
        addByNodeNameButton.setText(Localization.getString("MessageNodeDialog.addByNodeName"));
        addByNodeNameButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                VariableMapping mapping = new VariableMapping("processNodeName", "${currentNodeName}", VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping);
                }
            }
        });
    }

    private void createDataTableViewer(Composite parent) {
        dataTableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.minimumHeight = 200;
        dataTableViewer.getControl().setLayoutData(data);
        final Table table = dataTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("MessageNodeDialog.VariableName"), Localization.getString("MessageNodeDialog.Alias") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        dataTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(oldMapping, VariableMapping.USAGE_READ);
                }
            }
        });
        dataTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        dataTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_READ));
        dataTableViewer.setInput(new Object());
    }

    private void addDataButtons(Composite parent) {
        final Composite par = parent;
        Composite composite = new Composite(par, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));
        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setText(Localization.getString("button.add"));
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editVariableMapping(null, VariableMapping.USAGE_READ);
            }
        });
        Button addAllButton = new Button(composite, SWT.PUSH);
        addAllButton.setText(Localization.getString("button.addAll"));
        addAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<String> variableNamesToAdd = new ArrayList<String>(definition.getVariableNames(false));
                for (VariableMapping mapping : variableMappings) {
                    variableNamesToAdd.remove(mapping.getProcessVariable());
                }
                for (String variableName : variableNamesToAdd) {
                    VariableMapping mapping = new VariableMapping(variableName, variableName, VariableMapping.USAGE_READ);
                    addVariableMapping(mapping);
                }
            }
        });
        Button updateButton = new Button(composite, SWT.PUSH);
        updateButton.setText(Localization.getString("button.edit"));
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(oldMapping, VariableMapping.USAGE_READ);
                }
            }
        });
        Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setText(Localization.getString("button.delete"));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    variableMappings.remove(mapping);
                    dataTableViewer.refresh();
                }
            }
        });
    }

    private void editVariableMapping(VariableMapping oldMapping, String usage) {
        MessageVariableDialog dialog = new MessageVariableDialog(definition.getVariableNames(true), VariableMapping.USAGE_SELECTOR.equals(usage), oldMapping);
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            VariableMapping mapping = new VariableMapping();
            mapping.setProcessVariable(dialog.getVariable());
            mapping.setSubprocessVariable(dialog.getAlias());
            mapping.setUsage(usage);
            if (oldMapping != null) {
                variableMappings.remove(oldMapping);
            }
            addVariableMapping(mapping);
        }
    }

    public List<VariableMapping> getSubprocessVariables() {
        return variableMappings;
    }

    private void addVariableMapping(VariableMapping mapping) {
        for (VariableMapping existingMapping : variableMappings) {
            if (existingMapping.getProcessVariable().equals(mapping.getProcessVariable())) {
                variableMappings.remove(existingMapping);
                break;
            }
        }
        variableMappings.add(mapping);
        selectorTableViewer.refresh();
        dataTableViewer.refresh();
    }

    private static class VariableMappingTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            VariableMapping mapping = (VariableMapping) element;
            switch (index) {
            case 0:
                return mapping.getProcessVariable();
            case 1:
                return mapping.getSubprocessVariable();
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private class UsageContentProvider implements IStructuredContentProvider {
        private final String usage;

        private UsageContentProvider(String usage) {
            this.usage = usage;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            List<VariableMapping> list = new ArrayList<VariableMapping>();
            for (VariableMapping variableMapping : variableMappings) {
                if (usage.equals(variableMapping.getUsage())) {
                    list.add(variableMapping);
                }
            }
            return list.toArray(new VariableMapping[list.size()]);
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // do nothing.
        }

        @Override
        public void dispose() {
            // do nothing.
        }
    }
}
