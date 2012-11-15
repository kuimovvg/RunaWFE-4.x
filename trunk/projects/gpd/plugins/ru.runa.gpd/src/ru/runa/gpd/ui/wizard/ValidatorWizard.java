package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.DateInputDialog;
import ru.runa.gpd.ui.dialog.NumberInputDialog;
import ru.runa.gpd.ui.dialog.RegexInputDialog;
import ru.runa.gpd.ui.dialog.TimeInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDialog;
import ru.runa.gpd.validation.ValidatorParser;
import ru.runa.gpd.validation.ValidatorDefinition.Param;

public class ValidatorWizard extends Wizard {

    protected FieldValidatorsWizardPage fieldValidatorsPage;

    protected GlobalValidatorsWizardPage globalValidatorsPage;

    private final IFile validationFile;

    private final FormNode formNode;

    private Map<String, Map<String, ValidatorConfig>> fieldConfigs;

    public ValidatorWizard(IFile validationFile, FormNode formNode) {
        this.validationFile = validationFile;
        this.formNode = formNode;
        this.fieldConfigs = ValidatorParser.parseValidatorConfigs(validationFile);
        setWindowTitle(Localization.getString("ValidatorWizard.wizard.title"));
        setDefaultPageImageDescriptor(SharedImages.getImageDescriptor("/icons/FormValidation.png"));
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        if (!formNode.hasForm()) {
            ((ValidatorDialog) getContainer()).getResetToDefaultsButton().setEnabled(false);
        }
        ((ValidatorDialog) getContainer()).getResetToDefaultsButton().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    fieldConfigs = ValidationUtil.getInitialFormValidators(validationFile, formNode);
                    initPages();
                } catch (Exception e) {
                    PluginLogger.logError("Extracting variables from form error", e);
                }
            }
        });
    }

    private void initPages() {
        fieldValidatorsPage.init(fieldConfigs);
        globalValidatorsPage.init(fieldConfigs);
    }

    @Override
    public boolean performFinish() {
        // regenerate validator.xml
        fieldValidatorsPage.performFinish();
        globalValidatorsPage.performFinish();
        List<ValidatorConfig> validatorConfigs = new ArrayList<ValidatorConfig>(fieldConfigs.get(ValidatorConfig.GLOBAL_FIELD_ID).values());
        for (ValidatorConfig config : validatorConfigs) {
            if (!config.check()) {
                MessageDialog.openError(getShell(), "Check validator config", config.getMessage());
                return false;
            }
        }
        // remove configs with deleted variables
        Set<String> variableNames = new HashSet<String>();
        variableNames.addAll(fieldConfigs.keySet());
        variableNames.removeAll(formNode.getProcessDefinition().getVariableNames(true));
        for (String varName : variableNames) {
            if (!ValidatorConfig.GLOBAL_FIELD_ID.equals(varName)) {
                fieldConfigs.remove(varName);
            }
        }
        ValidatorParser.writeValidatorXml(validationFile, fieldConfigs);
        return true;
    }

    @Override
    public void addPages() {
        List<Variable> allVariables = formNode.getProcessDefinition().getVariablesList();
        List<Swimlane> swimlanes = formNode.getProcessDefinition().getSwimlanes();
        fieldValidatorsPage = new FieldValidatorsWizardPage("Field validators", allVariables, swimlanes);
        globalValidatorsPage = new GlobalValidatorsWizardPage("Global validators", allVariables, swimlanes);
        initPages();
        addPage(fieldValidatorsPage);
        addPage(globalValidatorsPage);
    }

    public static abstract class ParametersComposite extends Composite {

        public ParametersComposite(Composite parent, int style) {
            super(parent, style);
        }

        protected abstract void clear();

        protected abstract void build(Map<String, Param> defParams, Map<String, String> configParams);

        protected abstract void updateConfigParams(Collection<String> paramNames, ValidatorConfig config);

    }

    public static abstract class ValidatorInfoControl extends Composite {

        protected ValidatorDefinition definition;

        protected ValidatorConfig config;

        protected ParametersComposite parametersComposite;

        private final Label descriptionLabel;

        protected Text errorMessageText;

        public ValidatorInfoControl(Composite parent) {
            super(parent, SWT.BORDER);

            this.setLayout(new GridLayout(1, true));
            descriptionLabel = new Label(this, SWT.NONE);
            descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            descriptionLabel.setText("_\n_");

            Label errorLabel = new Label(this, SWT.NONE);
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            errorLabel.setText(Localization.getString("ValidatorsWizardPage.ErrorMessage"));

            errorMessageText = new Text(this, SWT.BORDER);
            errorMessageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        protected abstract boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config);

        public void setConfig(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            this.setEnabled(enableUI(variableName, definition, config));
            if (config != null) {
                saveConfig();
                this.config = config;
                this.definition = definition;

                descriptionLabel.setText(definition.getDescription());
                errorMessageText.setText(config.getMessage());

                parametersComposite.clear();
                parametersComposite.build(definition.getParams(), config.getParams());
                errorMessageText.setFocus();
            }
            setVisible(config != null);
        }

        public void saveConfig() {
            if (config != null) {
                // save input data to config
                config.setMessage(errorMessageText.getText());
                parametersComposite.updateConfigParams(definition.getParams().keySet(), config);
                config = null;
            }
        }
    }

    public static class DefaultParamsComposite extends ParametersComposite implements SelectionListener {

        private static final String DATA_KEY = "userInput";

        private static final String TYPE_KEY = "inputType";

        private static final String INPUT_VALUE = Localization.getString("BSH.InputValue");

        private final Map<String, Combo> inputCombos = new HashMap<String, Combo>();

        private static Map<String, UserInputDialog> dialogClassesForTypes = new HashMap<String, UserInputDialog>();
        static {
            dialogClassesForTypes.put(Param.STRING_TYPE, new UserInputDialog(INPUT_VALUE, ""));
            dialogClassesForTypes.put(Param.REGEX_TYPE, new RegexInputDialog(""));
            dialogClassesForTypes.put(Param.DATE_TYPE, new DateInputDialog(""));
            dialogClassesForTypes.put(Param.TIME_TYPE, new TimeInputDialog(""));
            dialogClassesForTypes.put(Param.NUMBER_TYPE, new NumberInputDialog(""));
        }

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
        protected void build(Map<String, Param> defParams, Map<String, String> configParams) {
            for (String name : defParams.keySet()) {
                Param param = defParams.get(name);

                Label label = new Label(this, SWT.NONE);
                label.setText(param.getDisplayName());
                label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                combo.addSelectionListener(this);

                String textData = configParams.get(name);
                if (Param.BOOLEAN_TYPE.equals(param.getType())) {
                    combo.add(""); // default
                    combo.add("true");
                    combo.add("false");
                } else {
                    if (textData != null) {
                        combo.add(textData);
                        combo.setData(DATA_KEY, textData);
                    }
                    combo.add(""); // default
                    combo.add(INPUT_VALUE);
                }
                if (textData != null) {
                    combo.setText(textData);
                }
                combo.setData(TYPE_KEY, param.getType());

                inputCombos.put(name, combo);
            }
            this.pack(true);
        }

        @Override
        protected void updateConfigParams(Collection<String> paramNames, ValidatorConfig config) {
            for (String name : paramNames) {
                Combo combo = inputCombos.get(name);
                String text = combo.getText();
                if (text.length() != 0) {
                    config.getParams().put(name, text);
                } else {
                    config.getParams().remove(name);
                }
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            Combo combo = (Combo) e.widget;
            if (!INPUT_VALUE.equals(combo.getItem(combo.getSelectionIndex()))) {
                return;
            }
            String oldUserInput = (String) combo.getData(DATA_KEY);
            String type = (String) combo.getData(TYPE_KEY);
            UserInputDialog inputDialog = dialogClassesForTypes.get(type);
            inputDialog.setInitialValue(oldUserInput);
            if (Window.OK == inputDialog.open()) {
                String userInput = inputDialog.getUserInput();
                if (oldUserInput != null) {
                    combo.remove(0);
                }
                combo.setData(DATA_KEY, userInput);
                combo.add(userInput, 0);
            }
            combo.select(0);
        }
    }

}
