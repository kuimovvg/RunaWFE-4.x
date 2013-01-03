package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.handler.action.ParamDef;
import ru.runa.gpd.handler.action.ParamDefGroup;
import ru.runa.gpd.validation.FormatMapping;
import ru.runa.gpd.validation.FormatMappingParser;

public class ParamDefWizardPage extends WizardPage {
    private ParamDefGroup paramDefGroup;
    private ParamDef paramDef;
    private Text name;
    private Text label;
    private Combo typeCombo;
    private Button paramIsVariableButton;

    public ParamDefWizardPage(ParamDefGroup paramDefGroup, ParamDef paramDef) {
        super(Localization.getString("ParamDefWizardPage.page.title"));
        setTitle(Localization.getString("ParamDefWizardPage.page.title"));
        setDescription(Localization.getString("ParamDefWizardPage.page.description"));
        this.paramDefGroup = paramDefGroup;
        this.paramDef = paramDef;
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createNameField(composite);
        createLabelField(composite);
        createVariableTypeField(composite);
        createVariableButtonField(composite);
        if (paramDef != null) {
            if (paramDef.getName() != null) {
                name.setText(paramDef.getName());
            }
            if (paramDef.getLabel() != null) {
                label.setText(paramDef.getLabel());
            }
            if (paramDef.getFormatFilters().size() > 0) {
                typeCombo.setText(paramDef.getFormatFilters().iterator().next());
            }
            paramIsVariableButton.setSelection(paramDef.isUseVariable());
        }
        setControl(composite);
        Dialog.applyDialogFont(composite);
        if (paramDef == null) {
            setPageComplete(false);
        }
        name.setFocus();
    }

    private void createNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("ParamDefWizardPage.page.name"));
        name = new Text(parent, SWT.BORDER);
        name.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createLabelField(Composite parent) {
        Label uiLabel = new Label(parent, SWT.NONE);
        uiLabel.setText(Localization.getString("ParamDefWizardPage.page.label"));
        label = new Text(parent, SWT.BORDER);
        label.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createVariableTypeField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("ParamDefWizardPage.page.varType"));
        List<String> typeMappingList = new ArrayList<String>();
        Map<String, FormatMapping> formatMappings = FormatMappingParser.getFormatMappings();
        for (FormatMapping mapping : formatMappings.values()) {
            typeMappingList.add(mapping.getName());
        }
        typeCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        typeCombo.setItems(typeMappingList.toArray(new String[typeMappingList.size()]));
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createVariableButtonField(Composite parent) {
        paramIsVariableButton = new Button(parent, SWT.CHECK | SWT.RIGHT);
        paramIsVariableButton.setText(Localization.getString("ParamDefWizardPage.page.type"));
    }

    private void verifyContentsValid() {
        if (isNameEmpty()) {
            setErrorMessage(Localization.getString("error.paramDef.no_param_name"));
            setPageComplete(false);
        } else if (!isNameValid() && paramDef == null) {
            setErrorMessage(Localization.getString("error.paramDef.param_exist"));
            setPageComplete(false);
        }
        if (isLabelEmpty()) {
            setErrorMessage(Localization.getString("error.paramDef.no_param_label"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            return ""; //$NON-NLS-1$
        }
        return name.getText().trim();
    }

    public String getLabel() {
        if (label == null) {
            return ""; //$NON-NLS-1$
        }
        return label.getText().trim();
    }

    public String getType() {
        return typeCombo.getText();
    }

    public boolean isVariable() {
        return paramIsVariableButton.getSelection();
    }

    private boolean isNameEmpty() {
        return name.getText().length() == 0;
    }

    private boolean isLabelEmpty() {
        return label.getText().length() == 0;
    }

    private boolean isNameValid() {
        boolean duplicate = false;
        for (ParamDef paramDef : paramDefGroup.getParameters()) {
            if (paramDef.getName().equals(name.getText())) {
                duplicate = true;
            }
        }
        return duplicate;
    }

    public ParamDefGroup getParamDefGroup() {
        return paramDefGroup;
    }

    public ParamDef getParamDef() {
        return paramDef;
    }
}
