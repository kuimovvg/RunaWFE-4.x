package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

public class MultiInstanceDialog extends SubprocessDialog {
    private static final String TYPE_VARIABLE = "variable";
    private static final String TYPE_CONSTANT = "constant";
    private static final String TYPE_GROUP = "group";
    private static final String TYPE_RELATION = "relation";
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
    private CTabFolder typeTabFolder;

    public MultiInstanceDialog(MultiSubprocess multiSubprocess) {
        super(multiSubprocess);
        Iterator<VariableMapping> iterator = variableMappings.iterator();
        while (iterator.hasNext()) {
            VariableMapping mapping = iterator.next();
            if (mapping.getUsage().equals("multiinstance-vars")) {
                if (mapping.getProcessVariableName().equals("tabVariableProcessVariable")) {
                    tabVariableProcessVariable = mapping.getSubprocessVariableName();
                } else if (mapping.getProcessVariableName().equals("tabVariableSubProcessVariable")) {
                    tabVariableSubProcessVariable = mapping.getSubprocessVariableName();
                } else if (mapping.getProcessVariableName().equals("tabGroupName")) {
                    tabGroupName = mapping.getSubprocessVariableName();
                    if (VariableUtils.isVariableNameWrapped(tabGroupName)) {
                        tabGroupNameType = TYPE_VARIABLE;
                        tabGroupName = VariableUtils.unwrapVariableName(tabGroupName);
                    } else {
                        tabGroupNameType = TYPE_CONSTANT;
                    }
                } else if (mapping.getProcessVariableName().equals("tabGroupSubProcessVariable")) {
                    tabGroupSubProcessVariable = mapping.getSubprocessVariableName();
                } else if (mapping.getProcessVariableName().equals("tabRelationName")) {
                    tabRelationName = mapping.getSubprocessVariableName();
                    if (VariableUtils.isVariableNameWrapped(tabRelationName)) {
                        tabRelationNameType = TYPE_VARIABLE;
                        tabRelationName = VariableUtils.unwrapVariableName(tabRelationName);
                    } else {
                        tabRelationNameType = TYPE_CONSTANT;
                    }
                } else if (mapping.getProcessVariableName().equals("tabRelationParam")) {
                    tabRelationParam = mapping.getSubprocessVariableName();
                    if (VariableUtils.isVariableNameWrapped(tabRelationParam)) {
                        tabRelationParamType = TYPE_VARIABLE;
                        tabRelationParam = VariableUtils.unwrapVariableName(tabRelationParam);
                    } else {
                        tabRelationParamType = TYPE_CONSTANT;
                    }
                } else if (mapping.getProcessVariableName().equals("tabRelationSubProcessVariable")) {
                    tabRelationSubProcessVariable = mapping.getSubprocessVariableName();
                } else if (mapping.getProcessVariableName().equals("typeMultiInstance")) {
                    typeMultiInstance = mapping.getSubprocessVariableName();
                }
                iterator.remove();
            }
        }
    }

    @Override
    protected void onSubprocessChanged() {
        super.onSubprocessChanged();
        updateTabVariableSubProcessVariables();
    }
    
    @Override
    protected void createConfigurationComposite(Composite composite) {
        Label label2 = new Label(composite, SWT.NO_BACKGROUND);
        label2.setLayoutData(new GridData());
        label2.setText(Localization.getString("Multiinstance.TypeMultiInstance"));

        updateTabVariableSubProcessVariables();
        typeTabFolder = new CTabFolder(composite, SWT.TOP | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 100;
        typeTabFolder.setLayoutData(gridData);
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
        typeTabFolder.addSelectionListener(new LoggingSelectionAdapter() {
            
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                int newPageIndex = typeTabFolder.indexOf((CTabItem) e.item);
                if (newPageIndex == 0) {
                    typeMultiInstance = TYPE_VARIABLE;
                } else if (newPageIndex == 1) {
                    typeMultiInstance = TYPE_GROUP;
                } else if (newPageIndex == 2) {
                    typeMultiInstance = TYPE_RELATION;
                }
            }
        });
        if (typeMultiInstance.equals(TYPE_RELATION)) {
            typeTabFolder.setSelection(2);
        } else if (typeMultiInstance.equals(TYPE_GROUP)) {
            typeTabFolder.setSelection(1);
        } else {
            typeTabFolder.setSelection(0);
        }
        
    }
    
    private void createTabVariable(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        {
            Label labelProcessVariable = new Label(composite, SWT.NONE);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.ProcessVariableName") + ":");
            List<String> tabVariableProcessVariables = getSelectorVariableNames(definition.getName());
            final Combo processVariableField = new Combo(composite, SWT.READ_ONLY);
            processVariableField.setItems(tabVariableProcessVariables.toArray(new String[tabVariableProcessVariables.size()]));
            processVariableField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
            tabVariableSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabVariableSubProcessVariablesField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            tabVariableSubProcessVariablesField.setText(tabVariableSubProcessVariable);
            tabVariableSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabVariableSubProcessVariable = tabVariableSubProcessVariablesField.getText();
                }
            });
        }
    }

    private void createTabGroup(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Multiinstance.GroupName") + ":");
            final List<String> groupVariableNames = getProcessVariablesNames(definition.getName(), String.class.getName(), Group.class.getName());
            String lastUserInputValue = TYPE_CONSTANT.equals(tabGroupNameType) ? tabGroupName : null;
            final TypedUserInputCombo groupCombo = new TypedUserInputCombo(composite, lastUserInputValue);
            groupCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            groupCombo.setShowEmptyValue(false);
            for (String variableName : groupVariableNames) {
                groupCombo.add(variableName);
            }
            groupCombo.setTypeClassName(String.class.getName());
            groupCombo.setText(tabGroupName);
            groupCombo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String selected = groupCombo.getText();
                    if (!TypedUserInputCombo.INPUT_VALUE.equals(selected)) {
                        tabGroupNameType = groupVariableNames.contains(selected) ? TYPE_VARIABLE : TYPE_CONSTANT;
                        tabGroupName = selected;
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");
            tabGroupSubProcessVariablesField = new Combo(composite, SWT.READ_ONLY);
            tabGroupSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabGroupSubProcessVariablesField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            tabGroupSubProcessVariablesField.setText(tabGroupSubProcessVariable);
            tabGroupSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabGroupSubProcessVariable = tabGroupSubProcessVariablesField.getText();
                }
            });
        }
    }

    private void createTabRelation(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Relation.Name") + ":");
            final List<String> relationVariableNames = getProcessVariablesNames(definition.getName(), String.class.getName());
            String lastUserInputValue = TYPE_CONSTANT.equals(tabRelationNameType) ? tabRelationName : null;
            final TypedUserInputCombo relationNameCombo = new TypedUserInputCombo(composite, lastUserInputValue);
            relationNameCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : relationVariableNames) {
                relationNameCombo.add(variableName);
            }
            relationNameCombo.setShowEmptyValue(false);
            relationNameCombo.setTypeClassName(String.class.getName());
            relationNameCombo.setText(tabRelationName);
            relationNameCombo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String selected = relationNameCombo.getText();
                    if (!TypedUserInputCombo.INPUT_VALUE.equals(selected)) {
                        tabRelationNameType = relationVariableNames.contains(selected) ? TYPE_VARIABLE : TYPE_CONSTANT;
                        tabRelationName = selected;
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Relation.Parameter") + ":");
            final List<String> relationParamVariableNames = getProcessVariablesNames(definition.getName(), String.class.getName(), Executor.class.getName());
            String lastUserInputValue = TYPE_CONSTANT.equals(tabRelationParamType) ? tabRelationParam : null;
            final TypedUserInputCombo relationParamCombo = new TypedUserInputCombo(composite, lastUserInputValue);
            relationParamCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : relationParamVariableNames) {
                relationParamCombo.add(variableName);
            }
            relationParamCombo.setShowEmptyValue(false);
            relationParamCombo.setTypeClassName(String.class.getName());
            relationParamCombo.setText(tabRelationParam);
            relationParamCombo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String selected = relationParamCombo.getText();
                    if (!TypedUserInputCombo.INPUT_VALUE.equals(selected)) {
                        tabRelationParamType = relationParamVariableNames.contains(selected) ? TYPE_VARIABLE : TYPE_CONSTANT;
                        tabRelationParam = selected;
                    }
                }
            });
        }
        {
            Label labelProcessVariable = new Label(composite, SWT.READ_ONLY);
            labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            labelProcessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");
            tabRelationSubProcessVariablesField = new Combo(composite, SWT.READ_ONLY);
            tabRelationSubProcessVariablesField.setItems(tabVariableSubProcessVariables.toArray(new String[tabVariableSubProcessVariables.size()]));
            tabRelationSubProcessVariablesField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            tabRelationSubProcessVariablesField.setText(tabRelationSubProcessVariable);
            tabRelationSubProcessVariablesField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    tabRelationSubProcessVariable = tabRelationSubProcessVariablesField.getText();
                }
            });
        }
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

    @Override
    public List<VariableMapping> getVariableMappings(boolean includeMetadata) {
        List<VariableMapping> variableMappings = super.getVariableMappings(includeMetadata);
        if (includeMetadata) {
            if (tabVariableProcessVariable.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabVariableProcessVariable");
                mapping.setSubprocessVariableName(tabVariableProcessVariable);
                variableMappings.add(mapping);
            }
            if (tabVariableSubProcessVariable.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabVariableSubProcessVariable");
                mapping.setSubprocessVariableName(tabVariableSubProcessVariable);
                variableMappings.add(mapping);
            }
            if (tabGroupName.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabGroupName");
                if (tabGroupNameType.equals(TYPE_CONSTANT)) {
                    mapping.setSubprocessVariableName(tabGroupName);
                } else {
                    mapping.setSubprocessVariableName(VariableUtils.wrapVariableName(tabGroupName));
                }
                variableMappings.add(mapping);
            }
            if (tabGroupSubProcessVariable.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabGroupSubProcessVariable");
                mapping.setSubprocessVariableName(tabGroupSubProcessVariable);
                variableMappings.add(mapping);
            }
            if (tabRelationName.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabRelationName");
                if (tabRelationNameType.equals(TYPE_CONSTANT)) {
                    mapping.setSubprocessVariableName(tabRelationName);
                } else {
                    mapping.setSubprocessVariableName(VariableUtils.wrapVariableName(tabRelationName));
                }
                variableMappings.add(mapping);
            }
            if (tabRelationParam.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabRelationParam");
                if (tabRelationParamType.equals(TYPE_CONSTANT)) {
                    mapping.setSubprocessVariableName(tabRelationParam);
                } else {
                    mapping.setSubprocessVariableName(VariableUtils.wrapVariableName(tabRelationParam));
                }
                variableMappings.add(mapping);
            }
            if (tabRelationSubProcessVariable.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("tabRelationSubProcessVariable");
                mapping.setSubprocessVariableName(tabRelationSubProcessVariable);
                variableMappings.add(mapping);
            }
            if (typeMultiInstance.length() != 0) {
                VariableMapping mapping = new VariableMapping();
                mapping.setUsage("multiinstance-vars");
                mapping.setProcessVariableName("typeMultiInstance");
                mapping.setSubprocessVariableName(typeMultiInstance);
                variableMappings.add(mapping);
            }
        }
        return variableMappings;
    }

    private List<String> getProcessVariablesNames(String name, String... typeClassNameFilters) {
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(name);
        if (definition != null) {
            return definition.getVariableNames(true, typeClassNameFilters);
        }
        return new ArrayList<String>();
    }

    private List<String> getSelectorVariableNames(String processName) {
        ProcessDefinition definition = ProcessCache.getFirstProcessDefinition(processName);
        if (definition != null) {
            return definition.getVariableNames(false, List.class.getName());
        }
        return new ArrayList<String>();
    }

}
