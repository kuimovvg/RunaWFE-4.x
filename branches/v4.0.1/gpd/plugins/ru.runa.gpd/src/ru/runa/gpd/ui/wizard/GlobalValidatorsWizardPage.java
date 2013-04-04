package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.decision.BSHTypeSupport;
import ru.runa.gpd.extension.decision.BSHValidationModel;
import ru.runa.gpd.extension.decision.BSHValidationModel.Expr;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.JavaHighlightTextStyling;
import ru.runa.gpd.ui.dialog.ChooseVariableDialog;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ParametersComposite;
import ru.runa.gpd.ui.wizard.ValidatorWizard.ValidatorInfoControl;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.wfe.var.format.StringFormat;

public class GlobalValidatorsWizardPage extends WizardPage {
    private TableViewer validatorsTableViewer;
    private Button deleteButton;
    private ValidatorInfoControl infoGroup;
    private Map<String, Map<String, ValidatorConfig>> fieldConfigs;
    private List<ValidatorConfig> validatorConfigs;
    private final List<Variable> allVariables = new ArrayList<Variable>();
    private final List<String> bshVariableNames = new ArrayList<String>();

    protected GlobalValidatorsWizardPage(String pageName, List<Variable> variables, List<Swimlane> swimlanes) {
        super(pageName);
        this.allVariables.addAll(variables);
        for (Swimlane swimlane : swimlanes) {
            this.allVariables.add(new Variable(swimlane.getName(), StringFormat.class.getName(), swimlane.isPublicVisibility(), null));
        }
        for (Variable variable : allVariables) {
            if (!variable.getName().contains(" ")) {
                bshVariableNames.add(variable.getName());
            }
        }
        setTitle(Localization.getString("ValidatorWizardPage.globalpage.title"));
        setDescription(Localization.getString("ValidatorWizardPage.globalpage.description"));
    }

    public void init(Map<String, Map<String, ValidatorConfig>> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
        List<ValidatorConfig> validatorConfigs;
        if (fieldConfigs.containsKey(ValidatorConfig.GLOBAL_FIELD_ID)) {
            validatorConfigs = new ArrayList<ValidatorConfig>(fieldConfigs.get(ValidatorConfig.GLOBAL_FIELD_ID).values());
        } else {
            validatorConfigs = new ArrayList<ValidatorConfig>();
        }
        this.validatorConfigs = validatorConfigs;
        if (validatorsTableViewer != null) {
            validatorsTableViewer.setInput(validatorConfigs);
            validatorsTableViewer.refresh(true);
        }
    }

    public List<ValidatorConfig> getValidatorConfigs() {
        return validatorConfigs;
    }

    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        Composite valComposite = new Composite(mainComposite, SWT.NONE);
        valComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        valComposite.setLayout(new GridLayout(2, false));
        validatorsTableViewer = new TableViewer(valComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 200;
        validatorsTableViewer.getControl().setLayoutData(data);
        Table table = validatorsTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText(Localization.getString("GlobalValidatorsWizardPage.SelectedValidators"));
        tableColumn.setWidth(500);
        Composite buttonsBar = new Composite(valComposite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        buttonsBar.setLayoutData(gridData);
        buttonsBar.setLayout(new GridLayout(1, true));
        gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        addButton(buttonsBar, "button.add", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ValidatorConfig config = ValidatorDefinitionRegistry.getGlobalDefinition().create(ValidatorConfig.GLOBAL_FIELD_ID);
                config.setMessage(Localization.getString("GlobalValidatorsWizardPage.defaultValidationMessage"));
                validatorConfigs.add(config);
                validatorsTableViewer.refresh(true);
                validatorsTableViewer.setSelection(new StructuredSelection(config));
            }
        });
        deleteButton = addButton(buttonsBar, "button.delete", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                if (config == null) {
                    infoGroup.setVisible(false);
                    return;
                }
                validatorConfigs.remove(config);
                validatorsTableViewer.refresh(true);
            }
        });
        deleteButton.setEnabled(false);
        validatorsTableViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ValidatorConfig) element).getMessage();
            }
        });
        validatorsTableViewer.setContentProvider(new ArrayContentProvider());
        validatorsTableViewer.setInput(validatorConfigs);
        validatorsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ValidatorConfig config = (ValidatorConfig) ((IStructuredSelection) validatorsTableViewer.getSelection()).getFirstElement();
                deleteButton.setEnabled(config != null);
                ValidatorDefinition def = null;
                if (config != null) {
                    def = ValidationUtil.getValidatorDefinition(config.getType());
                }
                infoGroup.setConfig(ValidatorConfig.GLOBAL_FIELD_ID, def, config);
            }
        });
        infoGroup = new DefaultValidatorInfoControl(mainComposite);
        infoGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        infoGroup.setVisible(false);
        mainComposite.pack(true);
        setControl(mainComposite);
    }

    protected Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Localization.getString(buttonKey));
        button.addSelectionListener(selectionListener);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

    public void performFinish() {
        infoGroup.saveConfig();
        Map<String, ValidatorConfig> globalConfigsMap = new HashMap<String, ValidatorConfig>(validatorConfigs.size());
        int discrimination = 1;
        for (ValidatorConfig config : validatorConfigs) {
            globalConfigsMap.put(config.getType() + discrimination++, config);
        }
        fieldConfigs.put(ValidatorConfig.GLOBAL_FIELD_ID, globalConfigsMap);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // reloading variables
            ((BSHParamsComposite) infoGroup.parametersComposite).updateUsedVariables();
        }
    }

    public class DefaultValidatorInfoControl extends ValidatorInfoControl {
        public DefaultValidatorInfoControl(Composite parent) {
            super(parent);
            parametersComposite = new BSHParamsComposite(this, SWT.NONE);
            errorMessageText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    config.setMessage(errorMessageText.getText());
                    validatorsTableViewer.refresh(config, true);
                }
            });
        }

        @Override
        protected boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            return (config != null);
        }
    }

    public class BSHParamsComposite extends ParametersComposite {
        private final StyledText bshCodeText;
        private final Combo comboBoxVar1;
        private final Combo comboBoxOp;
        private final Combo comboBoxVar2;
        private final TabFolder tabFolder;
        private final List<String> variableNames = new ArrayList<String>();
        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public BSHParamsComposite(ValidatorInfoControl parent, int style) {
            super(parent, style);
            this.setLayoutData(new GridData(GridData.FILL_BOTH));
            this.setLayout(new GridLayout(1, true));
            tabFolder = new TabFolder(parent, SWT.NULL);
            tabFolder.setLayout(new GridLayout());
            tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabFolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (tabFolder.getSelectionIndex() == 1) {
                        toBSHCode();
                    }
                }
            });
            TabItem[] tabs = new TabItem[2];
            tabs[0] = new TabItem(tabFolder, SWT.NULL);
            tabs[0].setText(Localization.getString("BSHEditor.title.constructor"));
            Composite constrComposite = new Composite(tabFolder, SWT.BORDER);
            constrComposite.setLayout(new GridLayout(3, false));
            constrComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabs[0].setControl(constrComposite);
            comboBoxVar1 = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxVar1.setLayoutData(getComboGridData());
            updateUsedVariables();
            comboBoxVar1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String varName = comboBoxVar1.getItem(comboBoxVar1.getSelectionIndex());
                    Variable variable = getVariableByName(varName);
                    comboBoxVar1.setData(variable);
                    refreshCombos();
                }
            });
            comboBoxOp = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxOp.setLayoutData(getComboGridData());
            comboBoxOp.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                }
            });
            comboBoxVar2 = new Combo(constrComposite, SWT.READ_ONLY);
            comboBoxVar2.setLayoutData(getComboGridData());
            tabs[1] = new TabItem(tabFolder, SWT.NULL);
            tabs[1].setText(Localization.getString("BSHEditor.title.bsh"));
            Composite bshComposite = new Composite(tabFolder, SWT.BORDER);
            bshComposite.setLayout(new GridLayout());
            bshComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            tabs[1].setControl(bshComposite);
            Hyperlink hl3 = new Hyperlink(bshComposite, SWT.NONE);
            hl3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            hl3.setText(Localization.getString("button.insert_variable"));
            hyperlinkGroup.add(hl3);
            bshCodeText = new StyledText(bshComposite, SWT.BORDER | SWT.MULTI);
            bshCodeText.setLayoutData(new GridData(GridData.FILL_BOTH));
            bshCodeText.addLineStyleListener(new JavaHighlightTextStyling(bshVariableNames));
            hl3.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    ChooseVariableDialog dialog = new ChooseVariableDialog(bshVariableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        bshCodeText.insert(variableName);
                        bshCodeText.setFocus();
                        bshCodeText.setCaretOffset(bshCodeText.getCaretOffset() + variableName.length());
                    }
                }
            });
        }

        private void updateUsedVariables() {
            variableNames.clear();
            for (String varName : fieldConfigs.keySet()) {
                if (!varName.contains(" ")) {
                    variableNames.add(varName);
                }
            }
            variableNames.remove(ValidatorConfig.GLOBAL_FIELD_ID);
            // reload combo 1
            comboBoxVar1.removeAll();
            for (String varName : variableNames) {
                comboBoxVar1.add(varName);
            }
        }

        private GridData getComboGridData() {
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 100;
            return gridData;
        }

        private void refreshCombos() {
            Variable variable = (Variable) comboBoxVar1.getData();
            List<Operation> operations = Operation.getAll(BSHTypeSupport.getByFormat(variable.getFormat()));
            comboBoxOp.setItems(new String[0]);
            for (Operation operation : operations) {
                comboBoxOp.add(operation.getVisibleName());
            }
            comboBoxVar2.setItems(new String[0]);
            List<String> combo2Names = getCombo2VariableNames(variable);
            for (String var2Name : combo2Names) {
                comboBoxVar2.add(var2Name);
            }
        }

        private List<String> getCombo2VariableNames(Variable variable1) {
            List<String> vars = new ArrayList<String>();
            BSHTypeSupport typeSupport1 = BSHTypeSupport.getByFormat(variable1.getFormat());
            for (Variable variable : allVariables) {
                BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());
                // formats are equals, variable not selected in the first combo
                if ((typeSupport1 == typeSupport) && (variable1 != variable) && (variableNames.contains(variable1.getName()))) {
                    if (variable.getName().indexOf(" ") < 0) {
                        vars.add(variable.getName());
                    }
                }
            }
            return vars;
        }

        private Variable getVariableByName(String variableName) {
            for (Variable variable : allVariables) {
                if (variable.getName().equals(variableName)) {
                    return variable;
                }
            }
            return null;
        }

        private void toBSHCode() {
            if ((comboBoxVar1.getText().length() > 0) && (comboBoxOp.getText().length() > 0) && (comboBoxVar2.getText().length() > 0)) {
                Variable var1 = (Variable) comboBoxVar1.getData();
                String operationName = comboBoxOp.getItem(comboBoxOp.getSelectionIndex());
                Variable var2 = getVariableByName(comboBoxVar2.getText());
                BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(var1.getFormat());
                Operation operation = Operation.getByName(operationName, typeSupport);
                String bsh = operation.generateCode(var1, var2);
                bshCodeText.setText(bsh);
            } else {
                // don't change code
            }
        }

        @Override
        protected void clear() {
        }

        @Override
        protected void build(Map<String, Param> defParams, Map<String, String> configParams) {
            String textData = configParams.get(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            if (textData == null) {
                textData = "";
            }
            try {
                Expr expr = BSHValidationModel.fromCode(textData, allVariables);
                if (expr != null) {
                    Variable variable = expr.getVar1();
                    if (variableNames.contains(variable.getName())) {
                        comboBoxVar1.setText(variable.getName());
                        comboBoxVar1.setData(variable);
                        refreshCombos();
                        comboBoxOp.setText(expr.getOperation().getVisibleName());
                        comboBoxVar2.setText(expr.getVar2().getName());
                        textData = expr.generateCode();
                    }
                }
            } catch (Exception e) {
                tabFolder.setSelection(1);
            }
            bshCodeText.setText(textData);
        }

        @Override
        protected void updateConfigParams(Collection<String> paramNames, ValidatorConfig config) {
            toBSHCode();
            String textData = bshCodeText.getText().trim();
            if (textData.length() != 0) {
                config.getParams().put(ValidatorDefinition.EXPRESSION_PARAM_NAME, textData);
            } else {
                config.getParams().remove(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            }
        }
    }
}
