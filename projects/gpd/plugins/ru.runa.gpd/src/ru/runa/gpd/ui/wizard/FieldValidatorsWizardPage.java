package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.wizard.ValidatorWizard.DefaultParamsComposite;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ValidatorInfoControl;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.wfe.user.Executor;

public class FieldValidatorsWizardPage extends WizardPage {
    private TabFolder tabFolder;
    private TableViewer variablesTableViewer;
    private TableViewer swimlanesTableViewer;
    private Label warningLabel;
    private TableViewer validatorsTableViewer;
    private ValidatorInfoControl infoGroup;
    private final List<Variable> variables;
    private final List<Swimlane> swimlanes;
    private String warningMessage = "";
    private Map<String, Map<String, ValidatorConfig>> fieldConfigs;

    protected FieldValidatorsWizardPage(String pageName, List<Variable> variables, List<Swimlane> swimlanes) {
        super(pageName);
        this.variables = variables;
        this.swimlanes = swimlanes;
        setTitle(Localization.getString("ValidatorWizardPage.fieldpage.title"));
        setDescription(Localization.getString("ValidatorWizardPage.fieldpage.description"));
    }

    public void init(Map<String, Map<String, ValidatorConfig>> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
        if (variablesTableViewer != null) {
            variablesTableViewer.refresh(true);
            swimlanesTableViewer.refresh(true);
            updateSelection();
            validatorsTableViewer.refresh(true);
            updateValidatorSelection();
        }
        List<String> undefinedValidators = new ArrayList<String>();
        for (String fieldName : fieldConfigs.keySet()) {
            Map<String, ValidatorConfig> configs = fieldConfigs.get(fieldName);
            for (String validatorName : configs.keySet()) {
                if (!ValidatorDefinitionRegistry.getValidatorDefinitions().containsKey(validatorName)) {
                    undefinedValidators.add(validatorName);
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
                updateSelection();
            }
        });
        variablesTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                String variableName = getCurrentVariableName();
                if (fieldConfigs.containsKey(variableName)) {
                    removeField(variableName);
                } else {
                    addField(variableName);
                }
                variablesTableViewer.refresh(true);
                updateSelection();
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
                updateSelection();
            }
        });
        swimlanesTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                String variableName = getCurrentVariableName();
                if (fieldConfigs.containsKey(variableName)) {
                    removeField(variableName);
                } else {
                    addField(variableName);
                }
                swimlanesTableViewer.refresh(true);
                updateSelection();
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
        validatorsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
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
        variablesTableViewer.setInput(variables);
        swimlanesTableViewer.setInput(swimlanes);
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

    private void updateSelection() {
        Map<String, ValidatorConfig> validators = fieldConfigs.get(getCurrentVariableName());
        validatorsTableViewer.getTable().setEnabled(validators != null);
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

    public class DefaultValidatorInfoControl extends ValidatorInfoControl {
        public DefaultValidatorInfoControl(Composite parent) {
            super(parent);
            parametersComposite = new DefaultParamsComposite(this, SWT.NONE);
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            return getFieldValidator(variableName, definition) != null;
        }
    }

    private NamedGraphElement getCurrentSelection() {
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

    private void updateValidatorsInput(NamedGraphElement variableOrSwimlane) {
        if (variableOrSwimlane != null) {
            String varType;
            if (variableOrSwimlane instanceof Variable) {
                varType = VariableFormatRegistry.getInstance().getArtifactNotNull(((Variable) variableOrSwimlane).getFormat()).getVariableClassName();
            } else {
                // swimlane
                varType = Executor.class.getName();
            }
            validatorsTableViewer.setInput(ValidationUtil.getFieldValidatorDefinitions(varType));
        }
    }

    private TableViewer createTableViewer(Composite parent, int width, int height, String text) {
        TableViewer tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = height;
        tableViewer.getControl().setLayoutData(data);
        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        TableColumn tableColumn = new TableColumn(table, SWT.CENTER);
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
}
