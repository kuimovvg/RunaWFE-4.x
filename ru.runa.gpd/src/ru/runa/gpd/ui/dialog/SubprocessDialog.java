package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class SubprocessDialog extends Dialog {
    private Combo subprocessDefinitionCombo;
    private String subprocessName;
    protected final ProcessDefinition definition;
    protected final List<VariableMapping> variableMappings;
    private VariablesComposite variablesComposite;
    private Label variablesLabel;

    public SubprocessDialog(Subprocess subprocess) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.variableMappings = subprocess.getVariableMappings();
        this.definition = subprocess.getProcessDefinition();
        this.subprocessName = subprocess.getSubProcessName();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        newShell.setSize(500, 500);
        super.configureShell(newShell);
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        
        Composite subprocessComposite = new Composite(composite, SWT.NONE);
        subprocessComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        subprocessComposite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(subprocessComposite, SWT.NO_BACKGROUND);
        label.setLayoutData(new GridData());
        label.setText(Localization.getString("Subprocess.Name"));
        subprocessDefinitionCombo = new Combo(subprocessComposite, SWT.BORDER);
        subprocessDefinitionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        subprocessDefinitionCombo.setItems(getProcessDefinitionNames());
        subprocessDefinitionCombo.setVisibleItemCount(10);
        if (subprocessName != null) {
            subprocessDefinitionCombo.setText(subprocessName);
        }
        subprocessDefinitionCombo.addSelectionListener(new LoggingSelectionAdapter() {
            
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                onSubprocessChanged();
            }
        });
        subprocessDefinitionCombo.addModifyListener(new LoggingModifyTextAdapter() {
            
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                onSubprocessChanged();
            }
        });
        
        createConfigurationComposite(composite);
        
        variablesLabel = new Label(composite, SWT.NONE);
        variablesLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        variablesLabel.setText(Localization.getString("Subprocess.EmbeddedSubprocessVariablesList"));
                
        variablesComposite = new VariablesComposite(composite);
        variablesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        if (subprocessName != null) {
            ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(subprocessName);
            if (definition instanceof SubprocessDefinition) {
                variablesComposite.setVisible(false);
                variablesLabel.setText(Localization.getString("Subprocess.EmbeddedSubprocessVariablesList"));
            } else {
                variablesComposite.setVisible(true);
                variablesLabel.setText(Localization.getString("Subprocess.VariablesList"));
            }
        }
        return composite;
    }
    
    protected void createConfigurationComposite(Composite composite) {
        
    }
    
    protected void onSubprocessChanged() {
        subprocessName = subprocessDefinitionCombo.getText();
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(subprocessName);
        if (definition instanceof SubprocessDefinition) {
            if (variablesComposite.isVisible()) {
                variablesComposite.setVisible(false);
                variablesLabel.setText(Localization.getString("Subprocess.EmbeddedSubprocessVariablesList"));
                //getContents().pack(true);
            }
        } else {
            if (!variablesComposite.isVisible()) {
                variablesComposite.setVisible(true);
                variablesLabel.setText(Localization.getString("Subprocess.VariablesList"));
                //getContents().pack(true);
            }
        }
    }

    private class VariablesComposite extends Composite {
        private TableViewer tableViewer;

        public VariablesComposite(Composite parent) {
            super(parent, SWT.BORDER);
            setLayout(new GridLayout(1, false));

            tableViewer = new TableViewer(this, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
            Table table = tableViewer.getTable();
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            String[] columnNames = new String[] { Localization.getString("Subprocess.ProcessVariableName"), Localization.getString("Subprocess.SubprocessVariableName"),
                    Localization.getString("Subprocess.Usage") };
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

            Composite buttonsComposite = new Composite(this, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 4;
            buttonsComposite.setLayout(gridLayout);
            Button addButton = new Button(buttonsComposite, SWT.BUTTON1);
            addButton.setText(Localization.getString("button.add"));
            addButton.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    editVariableMapping(null);
                }
            });
            Button updateButton = new Button(buttonsComposite, SWT.BUTTON1);
            updateButton.setText(Localization.getString("button.edit"));
            updateButton.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                    if (!selection.isEmpty()) {
                        VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                        editVariableMapping(oldMapping);
                    }
                }
            });
            Button removeButton = new Button(buttonsComposite, SWT.BUTTON1);
            removeButton.setText(Localization.getString("button.delete"));
            removeButton.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                    if (!selection.isEmpty()) {
                        VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                        if (Dialogs.confirm(Localization.getString("confirm.delete"))) {
                            variableMappings.remove(mapping);
                            tableViewer.refresh();
                            setTableInput();
                        }
                    }
                }
            });
        }
        
        private void editVariableMapping(VariableMapping oldMapping) {
            SubprocessVariableDialog dialog = new SubprocessVariableDialog(getProcessVariablesNames(definition.getName()), getProcessVariablesNames(getSubprocessName()), oldMapping);
            if (dialog.open() != IDialogConstants.CANCEL_ID) {
                VariableMapping mapping = new VariableMapping();
                mapping.setProcessVariableName(dialog.getProcessVariable());
                mapping.setSubprocessVariableName(dialog.getSubprocessVariable());
                String usage = dialog.getAccess();
                if (isListVariable(definition.getName(), mapping.getProcessVariableName()) && !isListVariable(getSubprocessName(), mapping.getSubprocessVariableName())) {
                    usage += "," + VariableMapping.USAGE_MULTIINSTANCE_LINK;
                }
                mapping.setUsage(usage);
                if (oldMapping != null) {
                    variableMappings.remove(oldMapping);
                }
                variableMappings.add(mapping);
                tableViewer.refresh();
            }
        }

        private void setTableInput() {
            tableViewer.setInput(getVariableMappings(false));
        }

    }

    public List<VariableMapping> getVariableMappings(boolean includeMetadata) {
        return variableMappings;
    }

    private static class VariableMappingTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            VariableMapping mapping = (VariableMapping) element;
            switch (index) {
            case 0:
                return mapping.getProcessVariableName();
            case 1:
                return mapping.getSubprocessVariableName();
            case 2:
                return mapping.getUsage();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private String[] getProcessDefinitionNames() {
        List<String> names = Lists.newArrayList();
        for (ProcessDefinition testProcessDefinition : ProcessCache.getAllProcessDefinitions()) {
            if (testProcessDefinition instanceof SubprocessDefinition) {
                if (!Objects.equal(definition, testProcessDefinition.getParent())) {
                    continue;
                }
            }
            if (!names.contains(testProcessDefinition.getName())) {
                names.add(testProcessDefinition.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private List<String> getProcessVariablesNames(String name) {
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(name);
        if (definition != null) {
            return definition.getVariableNames(true);
        }
        return new ArrayList<String>();
    }

    private boolean isListVariable(String name, String variableName) {
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(name);
        if (definition != null) {
            Variable variable = definition.getVariable(variableName, false);
            if (variable != null) {
                return List.class.getName().equals(variable.getJavaClassName());
            }
        }
        return false;
    }

    public String getSubprocessName() {
        return subprocessName;
    }
}
