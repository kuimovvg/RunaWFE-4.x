package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.ui.dialog.TimeInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ParametersComposite;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ValidatorInfoControl;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;

public class FieldValidatorsWizardPage extends WizardPage {
    private TabFolder tabFolder;
    private TableViewer variablesTableViewer;
    private TableViewer swimlanesTableViewer;
    private Label warningLabel;
    private TableViewer validatorsTableViewer;
    private ValidatorInfoControl infoGroup;
    private final ProcessDefinition processDefinition;
    private String warningMessage = "";
    private Map<String, Map<String, ValidatorConfig>> fieldConfigs;

    protected FieldValidatorsWizardPage(ProcessDefinition processDefinition) {
        super("Field validators");
        this.processDefinition = processDefinition;
        setTitle(Localization.getString("ValidatorWizardPage.fieldpage.title"));
        setDescription(Localization.getString("ValidatorWizardPage.fieldpage.description"));
    }

    public void init(Map<String, Map<String, ValidatorConfig>> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
        if (variablesTableViewer != null) {
            variablesTableViewer.refresh(true);
            swimlanesTableViewer.refresh(true);
            updateVariableSelection();
            validatorsTableViewer.refresh(true);
            updateValidatorSelection();
        }
        List<String> undefinedValidators = new ArrayList<String>();
        for (String fieldName : fieldConfigs.keySet()) {
            if (!ValidatorConfig.GLOBAL_FIELD_ID.equals(fieldName)) {
                Map<String, ValidatorConfig> configs = fieldConfigs.get(fieldName);
                for (String validatorName : configs.keySet()) {
                    if (!ValidatorDefinitionRegistry.getValidatorDefinitions().containsKey(validatorName)) {
                        undefinedValidators.add(validatorName);
                    }
                }
            }
        }
        if (undefinedValidators.size() > 0) {
            warningMessage = undefinedValidators.toString();
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(2, false));
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabFolder = new TabFolder(mainComposite, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 300;
        tabFolder.setLayoutData(data);
        variablesTableViewer = createTableViewer(tabFolder, 200, 300, null);
        variablesTableViewer.setLabelProvider(new VariableTableLabelProvider());
        variablesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateVariableSelection();
            }
        });
        variablesTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                String variableName = getCurrentVariableName();
                if (fieldConfigs.containsKey(variableName)) {
                    removeField(variableName);
                } else {
                    addField(variableName);
                }
                variablesTableViewer.refresh(true);
                updateVariableSelection();
            }
        });
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(Localization.getString("FieldValidatorsWizardPage.Variables"));
        tabItem1.setControl(variablesTableViewer.getControl());
        swimlanesTableViewer = createTableViewer(tabFolder, 200, 300, null);
        swimlanesTableViewer.setLabelProvider(new VariableTableLabelProvider());
        swimlanesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateVariableSelection();
            }
        });
        swimlanesTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                String variableName = getCurrentVariableName();
                if (fieldConfigs.containsKey(variableName)) {
                    removeField(variableName);
                } else {
                    addField(variableName);
                }
                swimlanesTableViewer.refresh(true);
                updateVariableSelection();
            }
        });
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(Localization.getString("FieldValidatorsWizardPage.Swimlanes"));
        tabItem2.setControl(swimlanesTableViewer.getControl());
        Composite right = new Composite(mainComposite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 300;
        right.setLayoutData(data);
        right.setLayout(new GridLayout(1, true));
        validatorsTableViewer = createTableViewer(right, 300, 100, Localization.getString("FieldValidatorsWizardPage.Validators"));
        validatorsTableViewer.setLabelProvider(new ValidatorDefinitionTableLabelProvider());
        GridData groupData = new GridData(GridData.FILL_BOTH);
        groupData.minimumHeight = 200;
        infoGroup = new DefaultValidatorInfoControl(right);
        infoGroup.setLayoutData(groupData);
        infoGroup.setVisible(false);
        validatorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateValidatorSelection();
            }
        });
        validatorsTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
                if (configs.containsKey(getCurrentDefinition().getName())) {
                    removeFieldValidator(getCurrentDefinition());
                } else {
                    addFieldValidator(getCurrentDefinition());
                }
                validatorsTableViewer.refresh(true);
                updateValidatorSelection();
            }
        });
        variablesTableViewer.setInput(processDefinition.getVariables(true, false));
        swimlanesTableViewer.setInput(processDefinition.getSwimlanes());
        warningLabel = new Label(mainComposite, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        warningLabel.setLayoutData(data);
        warningLabel.setForeground(ColorConstants.red);
        warningLabel.setText(warningMessage);
        mainComposite.pack(true);
        setControl(mainComposite);
    }

    public void performFinish() {
        infoGroup.saveConfig();
    }

    private void updateVariableSelection() {
        Map<String, ValidatorConfig> validators = fieldConfigs.get(getCurrentVariableName());
        validatorsTableViewer.getTable().setEnabled(validators != null);
        infoGroup.setVisible(false);
        updateValidatorsInput(getCurrentSelection());
    }

    private void updateValidatorSelection() {
        ValidatorDefinition vd = getCurrentDefinition();
        if (vd != null) {
            ValidatorConfig config = getFieldValidator(getCurrentVariableName(), vd);
            if (config == null) {
                config = vd.create("");
            }
            infoGroup.setConfig(getCurrentVariableName(), vd, config);
        }
    }

    private class DefaultValidatorInfoControl extends ValidatorInfoControl {
        public DefaultValidatorInfoControl(Composite parent) {
            super(parent);
            parametersComposite = new DefaultParamsComposite(this, SWT.NONE);
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            return getFieldValidator(variableName, definition) != null;
        }
    }

    private Variable getCurrentSelection() {
        if (tabFolder.getSelectionIndex() == 0) {
            return (Variable) ((StructuredSelection) variablesTableViewer.getSelection()).getFirstElement();
        } else {
            return (Swimlane) ((StructuredSelection) swimlanesTableViewer.getSelection()).getFirstElement();
        }
    }

    private String getCurrentVariableName() {
        NamedGraphElement variable = getCurrentSelection();
        return variable != null ? variable.getName() : null;
    }

    private ValidatorDefinition getCurrentDefinition() {
        return (ValidatorDefinition) ((StructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
    }

    private void updateValidatorsInput(Variable variable) {
        if (variable != null) {
            validatorsTableViewer.setInput(ValidationUtil.getFieldValidatorDefinitions(variable));
        }
    }

    private TableViewer createTableViewer(Composite parent, int width, int height, String text) {
        TableViewer tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = height;
        tableViewer.getControl().setLayoutData(data);
        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        if (text != null) {
            table.setHeaderVisible(true);
            tableColumn.setText(text);
        }
        tableColumn.setWidth(width);
        tableViewer.setContentProvider(new ArrayContentProvider());
        return tableViewer;
    }

    private void addField(String variableName) {
        Map<String, ValidatorConfig> validators = new HashMap<String, ValidatorConfig>();
        ValidatorDefinition requiredDef = ValidationUtil.getValidatorDefinition(ValidatorDefinition.REQUIRED_VALIDATOR_NAME);
        validators.put(requiredDef.getName(), requiredDef.create(Localization.getString("Validation.DefaultRequired")));
        fieldConfigs.put(variableName, validators);
    }

    private void removeField(String variableName) {
        fieldConfigs.remove(variableName);
    }

    private void addFieldValidator(ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
        configs.put(definition.getName(), definition.create(""));
    }

    private void removeFieldValidator(ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
        configs.remove(definition.getName());
    }

    private ValidatorConfig getFieldValidator(String variableName, ValidatorDefinition definition) {
        Map<String, ValidatorConfig> configs = fieldConfigs.get(variableName);
        return configs.get(definition.getName());
    }

    static final String UNCHECKED_IMG = "icons/unchecked.gif";
    static final String CHECKED_IMG = "icons/checked.gif";

    private class VariableTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            NamedGraphElement variable = (NamedGraphElement) element;
            return variable.getName();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            NamedGraphElement variable = (NamedGraphElement) element;
            String imagePath = fieldConfigs.containsKey(variable.getName()) ? CHECKED_IMG : UNCHECKED_IMG;
            return SharedImages.getImage(imagePath);
        }
    }

    private class ValidatorDefinitionTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            ValidatorDefinition variable = (ValidatorDefinition) element;
            return variable.getLabel();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            ValidatorDefinition definition = (ValidatorDefinition) element;
            Map<String, ValidatorConfig> configs = fieldConfigs.get(getCurrentVariableName());
            String imagePath;
            if (configs != null) {
                imagePath = configs.containsKey(definition.getName()) ? CHECKED_IMG : UNCHECKED_IMG;
            } else {
                imagePath = UNCHECKED_IMG;
            }
            return SharedImages.getImage(imagePath);
        }
    }

    private class DefaultParamsComposite extends ParametersComposite {
        private final Map<String, Combo> inputCombos = new HashMap<String, Combo>();

        public DefaultParamsComposite(Composite parent, int style) {
            super(parent, style);
            this.setLayoutData(new GridData(GridData.FILL_BOTH));
            this.setLayout(new GridLayout(2, true));
        }

        @Override
        protected void clear() {
            for (Control control : getChildren()) {
                control.dispose();
            }
            inputCombos.clear();
            this.pack(true);
        }

        @Override
        protected void build(ValidatorDefinition definition, Map<String, String> configParams) {
            for (Map.Entry<String, Param> entry : definition.getParams().entrySet()) {
                Label label = new Label(this, SWT.NONE);
                label.setText(entry.getValue().getLabel());
                label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                String initialValue = configParams.get(entry.getKey());
                // TODO: enable feature: use variables for comparison
//                if (initialValue != null && VariableUtils.isVariableNameWrapped(initialValue)) {
//                    initialValue = VariableUtils.unwrapVariableName(initialValue);
//                }
                TypedUserInputCombo combo = new TypedUserInputCombo(this, initialValue);
//                for (Variable variable : processDefinition.getVariables(true, true, entry.getValue().getType())) {
//                    combo.add(variable.getName());
//                }
                // TODO workaround for time validator
                Class<? extends UserInputDialog> userInputDialogClass = null;
                if ("time".equals(definition.getName()) && Date.class.getName().equals(entry.getValue().getType())) {
                    userInputDialogClass = TimeInputDialog.class;
                }
                if (userInputDialogClass != null) {
                    combo.setTypeClassName(entry.getValue().getType(), userInputDialogClass);
                } else {
                    combo.setTypeClassName(entry.getValue().getType());
                }
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                inputCombos.put(entry.getKey(), combo);
            }
            this.pack(true);
        }

        @Override
        protected void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config) {
            for (Map.Entry<String, Param> entry : definition.getParams().entrySet()) {
                Combo combo = inputCombos.get(entry.getKey());
                String text = combo.getText();
                if (text.length() != 0) {
                    if (processDefinition.getVariableNames(true, true, entry.getValue().getType()).contains(text)) {
                        text = VariableUtils.wrapVariableName(text);
                    }
                    config.getParams().put(entry.getKey(), text);
                } else {
                    config.getParams().remove(entry.getKey());
                }
            }
        }
    }
}
