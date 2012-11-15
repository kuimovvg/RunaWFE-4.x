package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessDialog extends Dialog {

    private String subprocessName;
    private final ProcessDefinition definition;
    private final List<VariableMapping> subprocessVariables;

    private TableViewer tableViewer;

    public SubprocessDialog(Subprocess subprocess) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.subprocessVariables = subprocess.getVariablesList();
        this.definition = subprocess.getProcessDefinition();
        this.subprocessName = subprocess.getSubProcessName();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);

        Label label = new Label(area, SWT.NO_BACKGROUND);
        label.setLayoutData(new GridData());
        label.setText(Localization.getString("Subprocess.Name"));
        final Combo namesCombo = new Combo(area, SWT.BORDER);
        GridData namesComboData = new GridData(GridData.FILL_HORIZONTAL);
        namesComboData.minimumWidth = 400;
        namesCombo.setLayoutData(namesComboData);
        namesCombo.setItems(getNameProcessDefinitions());
        namesCombo.setVisibleItemCount(10);
        if (subprocessName != null) {
            namesCombo.setText(subprocessName);
        }
        namesCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                subprocessName = namesCombo.getText();
            }
        });
        namesCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                subprocessName = namesCombo.getText();
            }
        });
        Label label1 = new Label(area, SWT.NO_BACKGROUND);
        label1.setLayoutData(new GridData());
        label1.setText(Localization.getString("Subprocess.VariablesList"));
        createTableViewer(area);
        addButtons(area);

        return area;
    }

    private void createTableViewer(Composite parent) {
        tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.minimumHeight = 300;
        tableViewer.getControl().setLayoutData(data);
        final Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("Subprocess.ProcessVariableName"),
                Localization.getString("Subprocess.SubprocessVariableName"), Localization.getString("Subprocess.Usage") };
        int[] columnWidths = new int[] { 200, 200, 120 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }

        tableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        setTableInput();
    }

    private void addButtons(Composite parent) {
        final Composite par = parent;

        Composite composite = new Composite(par, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        composite.setLayout(gridLayout);

        Button addButton = new Button(composite, SWT.BUTTON1);
        addButton.setText(Localization.getString("button.add"));
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editVariableMapping(null);
            }
        });

        Button updateButton = new Button(composite, SWT.BUTTON1);
        updateButton.setText(Localization.getString("button.edit"));
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(oldMapping);
                }
            }
        });

        Button removeButton = new Button(composite, SWT.BUTTON1);
        final Composite comp = composite;
        removeButton.setText(Localization.getString("button.delete"));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    if (MessageDialog.openQuestion(
                    		comp.getShell(), 
                    		Localization.getString("Mapping.Remove.title"), 
                    		Localization.getString("Mapping.Remove.message"))) {
                        subprocessVariables.remove(mapping);
                        tableViewer.refresh();
                        setTableInput();
                    }
                }
            }
        });

    }
    
    private void editVariableMapping(VariableMapping oldMapping) {
        SubprocessVariableDialog dialog = new SubprocessVariableDialog(
                getProcessVariablesNames(definition.getName()), 
                getProcessVariablesNames(getSubprocessName()), 
                oldMapping);
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            VariableMapping mapping = new VariableMapping();
            mapping.setProcessVariable(dialog.getProcessVariable());
            mapping.setSubprocessVariable(dialog.getSubprocessVariable());
            String usage = dialog.getAccess();
            if (isArrayVariable(definition.getName(), mapping.getProcessVariable())
                    && !isArrayVariable(getSubprocessName(), mapping.getSubprocessVariable())) {
                usage += "," + VariableMapping.USAGE_MULTIINSTANCE_LINK;
            }
            mapping.setUsage(usage);
            if (oldMapping != null) {
                subprocessVariables.remove(oldMapping);
            }
            addVariable(mapping);
            tableViewer.refresh();
        }
    }

    private void setTableInput() {
        tableViewer.setInput(getSubprocessVariables());
    }

    public List<VariableMapping> getSubprocessVariables() {
        return subprocessVariables;
    }

    private void addVariable(VariableMapping variable) {
        subprocessVariables.add(variable);
    }

    private static class VariableMappingTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object element, int index) {
            VariableMapping mapping = (VariableMapping) element;
            switch (index) {
            case 0:
                return mapping.getProcessVariable();
            case 1:
                return mapping.getSubprocessVariable();
            case 2:
                return mapping.getUsage();
            default:
                return "unknown " + index;
            }
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private String[] getNameProcessDefinitions() {
        List<String> names = ProcessCache.getAllProcessDefinitionNames();
        return names.toArray(new String[names.size()]);
    }

    private List<String> getProcessVariablesNames(String name) {
        ProcessDefinition definition = ProcessCache.getProcessDefinition(name);
        if (definition != null) {
            return definition.getVariableNames(true);
        }
        return new ArrayList<String>();
    }

    private boolean isArrayVariable(String name, String variableName) {
        ProcessDefinition definition = ProcessCache.getProcessDefinition(name);
        if (definition != null) {
            Variable variable = definition.getVariablesMap().get(variableName);
            if (variable != null) {
                return variable.getFormat().contains("Array");
            }
        }
        return false;
    }

    public String getSubprocessName() {
        return subprocessName;
    }

}
