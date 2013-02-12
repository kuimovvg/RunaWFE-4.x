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
    private Text nameText;
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
                nameText.setText(paramDef.getName());
            }
            if (paramDef.getFormatFilters().size() > 0) {
                String type = paramDef.getFormatFilters().get(0);
                String label = VariableFormatRegistry.getInstance().getArtifactNotNull(type).getLabel();
                typeCombo.setText(label);
            }
        }
        setControl(composite);
        Dialog.applyDialogFont(composite);
        if (paramDef == null) {
            setPageComplete(false);
        }
        nameText.setFocus();
    }

    private void createNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("ParamDefWizardPage.page.name"));
        nameText = new Text(parent, SWT.BORDER);
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createVariableTypeField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("ParamDefWizardPage.page.type"));
        List<String> types = new ArrayList<String>();
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            types.add(artifact.getLabel());
        }
        typeCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        typeCombo.setItems(types.toArray(new String[types.size()]));
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void verifyContentsValid() {
        if (nameText.getText().length() == 0) {
            setErrorMessage(Localization.getString("error.paramDef.no_param_name"));
            setPageComplete(false);
        } else if (isDuplicated()) {
            setErrorMessage(Localization.getString("error.paramDef.param_exist"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    @Override
    public String getName() {
        if (nameText == null) {
            return "";
        }
        return nameText.getText().trim();
    }

    public String getType() {
        return VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(typeCombo.getText()).getName();
    }

    private boolean isDuplicated() {
        if (paramDef != null) {
            return false;
        }
        for (ParamDef p : paramDefGroup.getParameters()) {
            if (paramDef != p && p.getName().equals(nameText.getText())) {
                return true;
            }
        }
        return false;
    }

    public ParamDefGroup getParamDefGroup() {
        return paramDefGroup;
    }

    public ParamDef getParamDef() {
        return paramDef;
    }
}
