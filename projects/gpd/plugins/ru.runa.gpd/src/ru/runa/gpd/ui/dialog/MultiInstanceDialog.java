package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.var.format.ListFormat;

public class MultiInstanceDialog extends Dialog {
    private String subprocessName;
    private final ProcessDefinition definition;
    private final List<VariableMapping> subprocessVariables;
    static final String INPUT_VALUE = Localization.getString("BSH.InputValue");
    private Combo tabVariableSubProcessVariablesField = null;
    private String tabVariableProcessVariable = "";
    private List<String> tabVariableSubProcessVariables;
    private String tabVariableSubProcessVariable = "";
    private Combo tabGroupSubProcessVariablesField = null;
    private String tabGroupName = "";
    private String tabGroupNameType = "";
    private String tabGroupSubProcessVariable = "";
    private Combo tabRelationSubProcessVariablesField = null;
    private String tabRelationName = "";
    private String tabRelationNameType = "";
    private String tabRelationParam = "";
    private String tabRelationParamType = "";
    private String tabRelationSubProcessVariable = "";
    private String typeMultiInstance = "";
    private TableViewer tableViewer;
    private CTabFolder typeTabFolder;

    public MultiInstanceDialog(MultiSubprocess multiInstance) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.subprocessVariables = multiInstance.getVariablesList();
        this.definition = multiInstance.getProcessDefinition();
        this.subprocessName = multiInstance.getSubProcessName();
        Iterator<VariableMapping> iter = this.subprocessVariables.iterator();
        while (iter.hasNext()) {
            VariableMapping vm = iter.next();
            if (vm.getUsage().equals("multiinstance-vars")) {
                if (vm.getProcessVariable().equals("tabVariableProcessVariable")) {
                    tabVariableProcessVariable = vm.getSubprocessVariable();
                } else if (vm.getProcessVariable().equals("tabVariableSubProcessVariable")) {
                    tabVariableSubProcessVariable = vm.getSubprocessVariable();
                } else if (vm.getProcessVariable().equals("tabGroupName")) {
                    tabGroupName = vm.getSubprocessVariable();
                    if (tabGroupName.startsWith("${") && tabGroupName.endsWith("}")) {
                        tabGroupNameType = "variable";
                        tabGroupName = tabGroupName.substring(2, tabGroupName.length() - 1);
                    } else {
                        tabGroupNameType = "constant";
                    }
                } else if (vm.getProcessVariable().equals("tabGroupSubProcessVariable")) {
                    tabGroupSubProcessVariable = vm.getSubprocessVariable();
                } else if (vm.getProcessVariable().equals("tabRelationName")) {
                    tabRelationName = vm.getSubprocessVariable();
                    if (tabRelationName.startsWith("${") && tabRelationName.endsWith("}")) {
                        tabRelationNameType = "variable";
                        tabRelationName = tabRelationName.substring(2, tabRelationName.length() - 1);
                    } else {
                        tabRelationNameType = "constant";
                    }
                } else if (vm.getProcessVariable().equals("tabRelationParam")) {
                    tabRelationParam = vm.getSubprocessVariable();
                    if (tabRelationParam.startsWith("${") && tabRelationParam.endsWith("}")) {
                        tabRelationParamType = "variable";
                        tabRelationParam = tabRelationParam.substring(2, tabRelationParam.length() - 1);
                    } else {
                        tabRelationParamType = "constant";
                    }
                } else if (vm.getProcessVariable().equals("tabRelationSubProcessVariable")) {
                    tabRelationSubProcessVariable = vm.getSubprocessVariable();
                } else if (vm.getProcessVariable().equals("typeMultiInstance")) {
                    typeMultiInstance = vm.getSubprocessVariable();
                }
            }
        }
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
                updateTabVariableSubProcessVariables();
            }
        });
        namesCombo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                subprocessName = namesCombo.getText();
                updateTabVariableSubProcessVariables();
            }
        });
        Label label2 = new Label(area, SWT.NO_BACKGROUND);
        label2.setLayoutData(new GridData());
        label2.setText(Localization.getString("Multiinstance.TypeMultiInstance"));
        createTabFolder(area);
        Label label1 = new Label(area, SWT.NO_BACKGROUND);
        label1.setLayoutData(new GridData());
        label1.setText(Localization.getString("Subprocess.VariablesList"));
        createTableViewer(area);
        addButtons(area);
        return area;
    }

    private void createTabFolder(Composite parent) {
        updateTabVariableSubProcessVariables();
        typeTabFolder = new CTabFolder(parent, SWT.TOP | SWT.BORDER);
        typeTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        {
            Composite composite1 = new Composite(typeTabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(typeTabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.variable"));
            tabItem1.setControl(composite1);
            createTabVariable(composite1);
        }
        {
            Composite composite1 = new Composite(typeTabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(typeTabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.group"));
            tabItem1.setControl(composite1);
            createTabGroup(composite1);
        }
        {
            Composite composite1 = new Composite(typeTabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(typeTabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("Multiinstance.tab.relation"));
            tabItem1.setControl(composite1);
            createTabRelation(composite1);
        }
        if (typeMultiInstance.equals("relation")) {
            typeTabFolder.setSelection(2);
        } else if (typeMultiInstance.equals("group")) {
            typeTabFolder.setSelection(1);
        } else {
            typeTabFolder.setSelection(0);
        }
    }

    private void createTabVariable(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());
        {
            Label labelProcessVariable = new Label(composite, SWT.NONE);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.ProcessVariableName") + ":");
            List<String> tabVariableProcessVariables = getSelectorVariableNames(definition.getName());
            final Combo processVariableField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            processVariableField.setItems(tabVariableProcessVariables.toArray(new String[tabVariableProcessVariables.size()]));
            processVariableField.setLayoutData(processVariableTextData);
            processVariableField.setText(tabVariableProcessVariable);
            processVariableField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabVariableProcessVariable = processVariableField.getText();
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.NONE);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");
            tabVariableSubProcessVariablesField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            tabVariableSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabVariableSubProcessVariablesField.setLayoutData(processVariableTextData);
            tabVariableSubProcessVariablesField.setText(tabVariableSubProcessVariable);
            tabVariableSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabVariableSubProcessVariable = tabVariableSubProcessVariablesField.getText();
                }
            });
        }
        Button setButton = new Button(parent, SWT.BUTTON1);
        setButton.setText(Localization.getString("Multiinstance.button.setTypeMultiInstance"));
        setButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                typeMultiInstance = "variable";
            }
        });
    }

    private void createTabGroup(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Multiinstance.GroupName") + ":");
            List<String> tabList = getProcessVariablesNames(definition.getName());
            final Combo processVariableField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            processVariableField.setItems(tabList.toArray(new String[tabList.size()]));
            processVariableField.add(INPUT_VALUE);
            if (tabGroupNameType.equals("constant") && !tabGroupName.equals("")) {
                processVariableField.add(tabGroupName, 0);
                processVariableField.select(0);
            } else {
                processVariableField.setText(tabGroupName);
            }
            processVariableField.setLayoutData(processVariableTextData);
            processVariableField.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (INPUT_VALUE.equals(processVariableField.getText())) {
                        String old = tabGroupName;
                        UserInputDialog inputDialog = new UserInputDialog(INPUT_VALUE, "");
                        if (OK == inputDialog.open()) {
                            String userInput = inputDialog.getUserInput();
                            if (tabGroupNameType.equals("constant") && tabGroupName.length() != 0) {
                                processVariableField.remove(0);
                            }
                            processVariableField.add(userInput, 0);
                            processVariableField.select(0);
                            tabGroupNameType = "constant";
                            tabGroupName = processVariableField.getText();
                        } else {
                            processVariableField.setText(old);
                        }
                    } else {
                        if (tabGroupNameType.equals("constant") && tabGroupName.length() != 0) {
                            if (processVariableField.getSelectionIndex() == 0) {
                                return;
                            }
                            processVariableField.remove(0);
                        }
                        tabGroupNameType = "variable";
                        tabGroupName = processVariableField.getText();
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");
            tabGroupSubProcessVariablesField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            tabGroupSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabGroupSubProcessVariablesField.setLayoutData(processVariableTextData);
            tabGroupSubProcessVariablesField.setText(tabGroupSubProcessVariable);
            tabGroupSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabGroupSubProcessVariable = tabGroupSubProcessVariablesField.getText();
                }
            });
        }
        Button setButton = new Button(parent, SWT.BUTTON1);
        setButton.setText(Localization.getString("Multiinstance.button.setTypeMultiInstance"));
        setButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                typeMultiInstance = "group";
            }
        });
    }

    private void createTabRelation(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Multiinstance.RelationName") + ":");
            List<String> tabList = getProcessVariablesNames(definition.getName());
            final Combo processVariableField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            processVariableField.setItems(tabList.toArray(new String[tabList.size()]));
            processVariableField.add(INPUT_VALUE);
            if (tabRelationNameType.equals("constant") && !tabRelationName.equals("")) {
                processVariableField.add(tabRelationName, 0);
                processVariableField.select(0);
            } else {
                processVariableField.setText(tabRelationName);
            }
            processVariableField.setLayoutData(processVariableTextData);
            processVariableField.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (INPUT_VALUE.equals(processVariableField.getText())) {
                        String old = tabRelationName;
                        UserInputDialog inputDialog = new UserInputDialog(INPUT_VALUE, "");
                        if (OK == inputDialog.open()) {
                            String userInput = inputDialog.getUserInput();
                            if (tabRelationNameType.equals("constant") && tabRelationName.length() != 0) {
                                processVariableField.remove(0);
                            }
                            processVariableField.add(userInput, 0);
                            processVariableField.select(0);
                            tabRelationNameType = "constant";
                            tabRelationName = processVariableField.getText();
                        } else {
                            processVariableField.setText(old);
                        }
                    } else {
                        if (tabRelationNameType.equals("constant") && tabRelationName.length() != 0) {
                            if (processVariableField.getSelectionIndex() == 0) {
                                return;
                            }
                            processVariableField.remove(0);
                        }
                        tabRelationNameType = "variable";
                        tabRelationName = processVariableField.getText();
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Multiinstance.RelationParam") + ":");
            List<String> tabList = getProcessVariablesNames(definition.getName());
            final Combo processVariableField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            processVariableField.setItems(tabList.toArray(new String[tabList.size()]));
            processVariableField.add(INPUT_VALUE);
            if (tabRelationParamType.equals("constant") && !tabRelationParam.equals("")) {
                processVariableField.add(tabRelationParam, 0);
                processVariableField.select(0);
            } else {
                processVariableField.setText(tabRelationParam);
            }
            processVariableField.setLayoutData(processVariableTextData);
            processVariableField.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (INPUT_VALUE.equals(processVariableField.getText())) {
                        String old = tabRelationParam;
                        UserInputDialog inputDialog = new UserInputDialog(INPUT_VALUE, "");
                        if (OK == inputDialog.open()) {
                            String userInput = inputDialog.getUserInput();
                            if (tabRelationParamType.equals("constant") && tabRelationParam.length() != 0) {
                                processVariableField.remove(0);
                            }
                            processVariableField.add(userInput, 0);
                            processVariableField.select(0);
                            tabRelationParamType = "constant";
                            tabRelationParam = processVariableField.getText();
                        } else {
                            processVariableField.setText(old);
                        }
                    } else {
                        if (tabRelationParamType.equals("constant") && tabRelationParam.length() != 0) {
                            if (processVariableField.getSelectionIndex() == 0) {
                                return;
                            }
                            processVariableField.remove(0);
                        }
                        tabRelationParamType = "variable";
                        tabRelationParam = processVariableField.getText();
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");
            tabRelationSubProcessVariablesField = new Combo(composite, SWT.READ_ONLY);
            GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
            processVariableTextData.minimumWidth = 200;
            tabRelationSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabRelationSubProcessVariablesField.setLayoutData(processVariableTextData);
            tabRelationSubProcessVariablesField.setText(tabRelationSubProcessVariable);
            tabRelationSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabRelationSubProcessVariable = tabRelationSubProcessVariablesField.getText();
                }
            });
        }
        Button setButton = new Button(parent, SWT.BUTTON1);
        setButton.setText(Localization.getString("Multiinstance.button.setTypeMultiInstance"));
        setButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                typeMultiInstance = "relation";
            }
        });
    }

    private void createTableViewer(Composite parent) {
        tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.minimumHeight = 200;
        tableViewer.getControl().setLayoutData(data);
        final Table table = tableViewer.getTable();
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
    }

    private void updateTabVariableSubProcessVariables() {
        String subProcessVariable_variable = tabVariableSubProcessVariable;
        String subProcessVariable_group = tabGroupSubProcessVariable;
        String subProcessVariable_relation = tabRelationSubProcessVariable;
        tabVariableSubProcessVariables = getProcessVariablesNames(getSubprocessName());
        if (tabVariableSubProcessVariablesField != null) {
            tabVariableSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabVariableSubProcessVariablesField.setText(subProcessVariable_variable);
        }
        if (tabGroupSubProcessVariablesField != null) {
            tabGroupSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabGroupSubProcessVariablesField.setText(subProcessVariable_group);
        }
        if (tabRelationSubProcessVariablesField != null) {
            tabRelationSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabRelationSubProcessVariablesField.setText(subProcessVariable_relation);
        }
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
                    if (MessageDialog.openQuestion(comp.getShell(), Localization.getString("Mapping.Remove.title"), Localization.getString("Mapping.Remove.message"))) {
                        subprocessVariables.remove(mapping);
                        tableViewer.refresh();
                        setTableInput();
                    }
                }
            }
        });
    }

    private void editVariableMapping(VariableMapping oldMapping) {
        SubprocessVariableDialog dialog = new SubprocessVariableDialog(getProcessVariablesNamesAll(definition.getName()), getProcessVariablesNamesAll(getSubprocessName()),
                oldMapping);
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            VariableMapping mapping = new VariableMapping();
            mapping.setProcessVariable(dialog.getProcessVariable());
            mapping.setSubprocessVariable(dialog.getSubprocessVariable());
            String usage = dialog.getAccess();
            if (isArrayVariable(definition.getName(), mapping.getProcessVariable()) && !isArrayVariable(getSubprocessName(), mapping.getSubprocessVariable())) {
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
        List<VariableMapping> subprocvar = getSubprocessVariables();
        Iterator<VariableMapping> iter = subprocvar.iterator();
        while (iter.hasNext()) {
            VariableMapping vm = iter.next();
            if (vm.getUsage().equals("multiinstance-vars")) {
                iter.remove();
            }
        }
        tableViewer.setInput(subprocvar);
    }

    public List<VariableMapping> getSubprocessVariables() {
        Iterator<VariableMapping> iter = subprocessVariables.iterator();
        while (iter.hasNext()) {
            VariableMapping vm = iter.next();
            if (vm.getUsage().equals("multiinstance-vars")) {
                iter.remove();
            }
        }
        if (tabVariableProcessVariable.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabVariableProcessVariable");
            vm.setSubprocessVariable(tabVariableProcessVariable);
            subprocessVariables.add(vm);
        }
        if (tabVariableSubProcessVariable.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabVariableSubProcessVariable");
            vm.setSubprocessVariable(tabVariableSubProcessVariable);
            subprocessVariables.add(vm);
        }
        if (tabGroupName.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabGroupName");
            if (tabGroupNameType.equals("constant")) {
                vm.setSubprocessVariable(tabGroupName);
            } else {
                vm.setSubprocessVariable("${" + tabGroupName + "}");
            }
            subprocessVariables.add(vm);
        }
        if (tabGroupSubProcessVariable.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabGroupSubProcessVariable");
            vm.setSubprocessVariable(tabGroupSubProcessVariable);
            subprocessVariables.add(vm);
        }
        if (tabRelationName.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabRelationName");
            if (tabRelationNameType.equals("constant")) {
                vm.setSubprocessVariable(tabRelationName);
            } else {
                vm.setSubprocessVariable("${" + tabRelationName + "}");
            }
            subprocessVariables.add(vm);
        }
        if (tabRelationParam.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabRelationParam");
            if (tabRelationParamType.equals("constant")) {
                vm.setSubprocessVariable(tabRelationParam);
            } else {
                vm.setSubprocessVariable("${" + tabRelationParam + "}");
            }
            subprocessVariables.add(vm);
        }
        if (tabRelationSubProcessVariable.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("tabRelationSubProcessVariable");
            vm.setSubprocessVariable(tabRelationSubProcessVariable);
            subprocessVariables.add(vm);
        }
        if (typeMultiInstance.length() != 0) {
            VariableMapping vm = new VariableMapping();
            vm.setUsage("multiinstance-vars");
            vm.setProcessVariable("typeMultiInstance");
            vm.setSubprocessVariable(typeMultiInstance);
            subprocessVariables.add(vm);
        }
        return subprocessVariables;
    }

    private void addVariable(VariableMapping variable) {
        subprocessVariables.add(variable);
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

    private String[] getNameProcessDefinitions() {
        List<String> names = ProcessCache.getAllProcessDefinitionNames();
        return names.toArray(new String[names.size()]);
    }

    private List<String> getProcessVariablesNamesAll(String name) {
        ProcessDefinition definition = ProcessCache.getProcessDefinition(name);
        if (definition != null) {
            return definition.getVariableNames(true);
        }
        return new ArrayList<String>();
    }

    private List<String> getProcessVariablesNames(String name) {
        ProcessDefinition definition = ProcessCache.getProcessDefinition(name);
        if (definition != null) {
            return definition.getVariableNames(true);
        }
        return new ArrayList<String>();
    }

    private List<String> getSelectorVariableNames(String name) {
        ProcessDefinition definition = ProcessCache.getProcessDefinition(name);
        if (definition != null) {
            Map<String, String> mvars = definition.getVariableFormats(false);
            Iterator<String> iter = mvars.values().iterator();
            while (iter.hasNext()) {
                String value = iter.next();
                if (!ListFormat.class.getName().equals(value)) {
                    iter.remove();
                }
            }
            return new ArrayList<String>(mvars.keySet());
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
