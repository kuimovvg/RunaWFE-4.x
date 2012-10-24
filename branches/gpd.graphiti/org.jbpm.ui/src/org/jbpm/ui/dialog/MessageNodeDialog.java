package org.jbpm.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.VariableMapping;

public class MessageNodeDialog extends Dialog {

    private final ProcessDefinition definition;
    private final List<VariableMapping> variableMappings;
    private final boolean sendMode;

    static final String INPUT_VALUE = Messages.getString("BSH.InputValue");

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
        label2.setText(Messages.getString(sendMode ? "MessageNodeDialog.SelectorSend" : "MessageNodeDialog.SelectorReceive"));
        createSelectorTableViewer(area);
        addSelectorButtons(area);

        Label label1 = new Label(area, SWT.NO_BACKGROUND);
        label1.setLayoutData(new GridData());
        label1.setText(Messages.getString("MessageNodeDialog.VariablesList"));
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
        String[] columnNames = new String[] { Messages.getString("property.name"), Messages.getString("property.value") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        selectorTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        selectorTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_SELECTOR));
        selectorTableViewer.setInput(new Object());
    }

    private void addSelectorButtons(Composite parent) {
        final Composite par = parent;

        Composite composite = new Composite(par, SWT.NONE);
        composite.setLayout(new GridLayout(5, false));

        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setText(Messages.getString("button.add"));
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                editVariableMapping(null, VariableMapping.USAGE_SELECTOR);
            }

        });

        Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setText(Messages.getString("button.delete"));
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
        addByProcessIdButton.setText(Messages.getString("MessageNodeDialog.addByProcessId"));
        addByProcessIdButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                VariableMapping mapping = new VariableMapping("processInstanceId", "${currentInstanceId}", VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping);
                }
            }
        });

        Button addByProcessNameButton = new Button(composite, SWT.PUSH);
        addByProcessNameButton.setText(Messages.getString("MessageNodeDialog.addByProcessName"));
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
        addByNodeNameButton.setText(Messages.getString("MessageNodeDialog.addByNodeName"));
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
        String[] columnNames = new String[] { Messages.getString("MessageNodeDialog.VariableName"), Messages.getString("MessageNodeDialog.Alias") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        dataTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        dataTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_READ));
        dataTableViewer.setInput(new Object());
    }

    private void addDataButtons(Composite parent) {
        final Composite par = parent;

        Composite composite = new Composite(par, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));

        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setText(Messages.getString("button.add"));
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                editVariableMapping(null, VariableMapping.USAGE_READ);
            }
        });

        Button addAllButton = new Button(composite, SWT.PUSH);
        addAllButton.setText(Messages.getString("button.addAll"));
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
        updateButton.setText(Messages.getString("button.edit"));
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
        removeButton.setText(Messages.getString("button.delete"));
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
        MessageVariableDialog dialog = new MessageVariableDialog(definition.getVariableNames(true), VariableMapping.USAGE_SELECTOR.equals(usage),
                oldMapping);
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
        variableMappings.add(mapping);
        selectorTableViewer.refresh();
        dataTableViewer.refresh();
    }

    private static class VariableMappingTableLabelProvider extends LabelProvider implements ITableLabelProvider {

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

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class UsageContentProvider implements IStructuredContentProvider {

        private final String usage;

        private UsageContentProvider(String usage) {
            this.usage = usage;
        }

        public Object[] getElements(Object inputElement) {
            List<VariableMapping> list = new ArrayList<VariableMapping>();
            for (VariableMapping variableMapping : variableMappings) {
                if (usage.equals(variableMapping.getUsage())) {
                    list.add(variableMapping);
                }
            }
            return list.toArray(new VariableMapping[list.size()]);
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // do nothing.
        }

        public void dispose() {
            // do nothing.
        }
    }

}
