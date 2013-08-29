package ru.runa.gpd.ui.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.VariableNameChecker;
import ru.runa.gpd.util.VariableUtils;

public class VariableNamePage extends ContentWizardPage {
    private final ProcessDefinition definition;
    private String variableName;
    private String variableDesc;
    private Text scriptingNameField;

    public VariableNamePage(ProcessDefinition definition, Variable variable) {
        this.definition = definition;
        this.variableName = variable != null && variable.getName() != null ? variable.getName() : definition.getNextVariableName();
        this.variableDesc = variable != null && variable.getDescription() != null ? variable.getDescription() : "";
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        final Text nameField = new Text(composite, SWT.BORDER);
        nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameField.setText(variableName);
        nameField.addKeyListener(new VariableNameChecker());
        nameField.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                variableName = nameField.getText();
                verifyContentIsValid();
                scriptingNameField.setText(VariableUtils.generateNameForScripting(definition, variableName, null));
            }
        });
        Label label = new Label(composite, SWT.NONE);
        label.setText(Localization.getString("VariableNamePage.scriptingName.label"));
        scriptingNameField = new Text(composite, SWT.BORDER);
        scriptingNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scriptingNameField.setEditable(false);
        scriptingNameField.setText(VariableUtils.generateNameForScripting(definition, variableName, null));
        label = new Label(composite, SWT.NONE);
        label.setText(Localization.getString("VariableNamePage.description.label"));
        final Text descriptionField = new Text(composite, SWT.BORDER);
        descriptionField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descriptionField.setText(variableDesc);
        descriptionField.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                variableDesc = descriptionField.getText();
            }
        });
        nameField.setFocus();
        nameField.selectAll();
    }

    @Override
    protected void verifyContentIsValid() {
        if (variableName.length() == 0) {
            setErrorMessage(Localization.getString("VariableNamePage.error.empty"));
        } else if (definition.getVariableNames(true).contains(variableName)) {
            setErrorMessage(Localization.getString("VariableNamePage.error.duplicated"));
        } else if (!VariableNameChecker.isValid(variableName)) {
            setErrorMessage(Localization.getString("VariableNamePage.error.forbiddenCharacters"));
        } else {
            setErrorMessage(null);
        }
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableDesc() {
        return variableDesc;
    }
}
