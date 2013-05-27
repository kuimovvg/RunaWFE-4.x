package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TypedUserInputCombo;
import ru.runa.wfe.var.format.StringFormat;

public class UpdateVariableDialog extends Dialog {
    private String name;
    private VariableFormatArtifact type;
    private boolean publicVisibility;
    private final ProcessDefinition definition;
    private final boolean createMode;
    private String defaultValue;
    private TypedUserInputCombo defaultValueField;

    public UpdateVariableDialog(ProcessDefinition definition, Variable variable) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        this.createMode = (variable == null);
        if (variable != null) {
            setType(variable.getFormat());
            this.publicVisibility = variable.isPublicVisibility();
            this.defaultValue = variable.getDefaultValue();
        } else {
            setType(StringFormat.class.getName());
        }
        this.name = definition.getNextVariableName();
    }

    public void setType(String type) {
        this.type = VariableFormatRegistry.getInstance().getArtifactNotNull(type);
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(1, false));
        Label labelTitle = new Label(area, SWT.NO_BACKGROUND);
        labelTitle.setLayoutData(new GridData());
        labelTitle.setText(createMode ? Localization.getString("Variable.property.inputAttributes") : Localization.getString("Variable.property.inputFormat"));
        Composite composite = new Composite(area, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData());
        if (createMode) {
            Label labelName = new Label(composite, SWT.NONE);
            labelName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            labelName.setText(Localization.getString("property.name") + ":");
            final Text nameField = new Text(composite, SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            nameField.setLayoutData(gridData);
            nameField.setText(name);
            nameField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    name = nameField.getText().replaceAll(" ", "_");
                    updateState();
                }
            });
            nameField.setFocus();
            nameField.selectAll();
        }
        Label labelType = new Label(composite, SWT.NONE);
        labelType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelType.setText(Localization.getString("Variable.property.format") + ":");
        final Combo typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            if (artifact.isEnabled()) {
                typeCombo.add(artifact.getLabel());
            }
        }
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        typeCombo.setLayoutData(gridData);
        typeCombo.setText(type.getLabel());
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                type = VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(typeCombo.getText());
                updateState();
                defaultValueField.setTypeClassName(type.getVariableClassName());
            }
        });
        final Label labelVisibility = new Label(composite, SWT.NONE);
        labelVisibility.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelVisibility.setText(Localization.getString("Variable.property.publicVisibility") + ":");
        final Combo comboVisibility = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        comboVisibility.setItems(new String[] { Localization.getString("message.no"), Localization.getString("message.yes") });
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        comboVisibility.setLayoutData(gridData);
        comboVisibility.setText(comboVisibility.getItem(publicVisibility ? 1 : 0));
        comboVisibility.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                publicVisibility = comboVisibility.getSelectionIndex() == 1;
            }
        });
        Label labelDefaultValue = new Label(composite, SWT.NONE);
        labelDefaultValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelDefaultValue.setText(Localization.getString("Variable.property.defaultValue") + ":");
        defaultValueField = new TypedUserInputCombo(composite, defaultValue);
        defaultValueField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        defaultValueField.setTypeClassName(type.getVariableClassName());
        defaultValueField.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent event) {
                defaultValue = defaultValueField.getText();
            }
        });
        return area;
    }

    private void updateState() {
        boolean allowCreation = !definition.getVariableNames(true).contains(name) && name.length() > 0;
        allowCreation = allowCreation || !createMode;
        allowCreation &= VariableNameChecker.isNameValid(name);
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(createMode ? Localization.getString("Variable.property.newVariable") : Localization.getString("Variable.property.editVariable"));
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return type.getName();
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
