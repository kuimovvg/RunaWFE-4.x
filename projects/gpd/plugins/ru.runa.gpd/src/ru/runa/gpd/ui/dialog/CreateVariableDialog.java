package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ru.runa.gpd.handler.CustomizationRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.LocalizationRegistry;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class CreateVariableDialog extends Dialog {
    private String name;
    private String type;
    private boolean publicVisibility;
    private final ProcessDefinition definition;
    private final boolean createMode;
    private String defaultValue;

    public CreateVariableDialog(ProcessDefinition definition, Variable variable) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        this.createMode = (variable == null);
        if (variable != null) {
            this.type = variable.getFormat();
            this.publicVisibility = variable.isPublicVisibility();
            this.defaultValue = variable.getDefaultValue();
        } else {
            this.type = StringFormat.class.getName();
        }
        this.name = definition.getNextVariableName();
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
            // nameField.addKeyListener(new VariableNameChecker(nameField));
            nameField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    name = nameField.getText().replaceAll(" ", "_");
                    updateButtons();
                }
            });
            nameField.setFocus();
            nameField.selectAll();
        }
        final Label labelType = new Label(composite, SWT.NONE);
        labelType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelType.setText(Localization.getString("Variable.property.format") + ":");
        Set<String> typeNames = CustomizationRegistry.getHandlerClasses("FORMAT");
        List<String> typeMappingList = new ArrayList<String>();
        final Map<String, String> reverseMapping = new HashMap<String, String>();
        for (String typeName : typeNames) {
            if (LocalizationRegistry.showType(typeName)) {
                String typeMapping = LocalizationRegistry.getTypeName(typeName);
                typeMappingList.add(typeMapping);
                reverseMapping.put(typeMapping, typeName);
            }
        }
        final Combo typeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        typeCombo.setItems(typeMappingList.toArray(new String[typeMappingList.size()]));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        typeCombo.setLayoutData(gridData);
        typeCombo.setText(LocalizationRegistry.getTypeName(type));
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                type = reverseMapping.get(typeCombo.getText());
                updateButtons();
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
        final Text defaultValueField = new Text(composite, SWT.BORDER);
        defaultValueField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        defaultValueField.setText(defaultValue != null ? defaultValue : "");
        defaultValueField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                defaultValue = defaultValueField.getText();
                updateButtons();
            }
        });
        return area;
    }

    private void updateButtons() {
        boolean allowCreation = !definition.getVariableNames(true).contains(name) && name.length() > 0;
        allowCreation = allowCreation || !createMode;
        allowCreation &= VariableNameChecker.isNameValid(name);
        if (defaultValue != null && defaultValue.length() > 0) {
            try {
                VariableFormat format = (VariableFormat) Class.forName(type).newInstance();
                format.parse(new String[] { defaultValue });
            } catch (Exception e) {
                allowCreation = false;
            }
        }
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

    public String getType() {
        return type;
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
