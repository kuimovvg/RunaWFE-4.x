package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefGroup;

public class BotTaskParamDefWizardPage extends WizardPage {
    private ParamDefGroup paramDefGroup;
    private ParamDef paramDef;
    private Text name;
    private Combo typeCombo;

    public BotTaskParamDefWizardPage(ParamDefGroup paramDefGroup, ParamDef paramDef) {
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
        createVariableTypeField(composite);
        if (paramDef != null) {
            if (paramDef.getName() != null) {
                name.setText(paramDef.getName());
            }
            if (paramDef.getFormatFilters().size() > 0) {
                typeCombo.setText(paramDef.getFormatFilters().iterator().next());
            }
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

    private void createVariableTypeField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("ParamDefWizardPage.page.type"));
        List<String> types = new ArrayList<String>();
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            types.add(artifact.getVariableClassName());
        }
        typeCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        typeCombo.setItems(types.toArray(new String[types.size()]));
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void verifyContentsValid() {
        if (isNameEmpty()) {
            setErrorMessage(Localization.getString("error.paramDef.no_param_name"));
            setPageComplete(false);
        } else if (!isNameValid() && paramDef == null) {
            setErrorMessage(Localization.getString("error.paramDef.param_exist"));
            setPageComplete(false);
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            return "";
        }
        return name.getText().trim();
    }

    public String getType() {
        return typeCombo.getText();
    }

    private boolean isNameEmpty() {
        return name.getText().length() == 0;
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
