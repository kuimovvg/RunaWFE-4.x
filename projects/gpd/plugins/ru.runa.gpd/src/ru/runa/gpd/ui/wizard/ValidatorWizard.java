package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import ru.runa.gpd.validation.ValidatorDialog;
import ru.runa.gpd.validation.ValidatorParser;

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
                Dialogs.error("Check validator config", config.getMessage());
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
        fieldValidatorsPage = new FieldValidatorsWizardPage(formNode.getProcessDefinition());
        globalValidatorsPage = new GlobalValidatorsWizardPage(formNode.getProcessDefinition());
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

    public static class DefaultParamsComposite extends ParametersComposite {
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
        protected void build(Map<String, Param> defParams, Map<String, String> configParams) {
            for (String name : defParams.keySet()) {
                Param param = defParams.get(name);
                Label label = new Label(this, SWT.NONE);
                label.setText(param.getLabel());
                label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                TypedUserInputCombo combo = new TypedUserInputCombo(this, configParams.get(name));
                combo.setTypeClassName(param.getType());
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
    }
}
