package ru.runa.bpm.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.bpm.ui.resource.Messages;

public class UpdateMappingDialog extends Dialog {

    private String type = "";

    private String name = "";

    private final boolean updateMode;

    protected UpdateMappingDialog(Shell parentShell, boolean updateMode) {
        super(parentShell);
        this.updateMode = updateMode;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        final Label labelTitle = new Label(area, SWT.NO_BACKGROUND);
        final GridData labelData = new GridData();
        labelTitle.setLayoutData(labelData);
        labelTitle.setText(Messages.getString(updateMode ? "Mapping.update.message" : "Mapping.add.message"));

        final Composite composite = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        GridData nameData = new GridData();
        composite.setLayoutData(nameData);

        Label labelType = new Label(composite, SWT.NONE);
        labelType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelType.setText(Messages.getString("Mapping.Type") + ":");
        final Text typeField = new Text(composite, SWT.BORDER);
        if (updateMode) {
            typeField.setEditable(false);
        }
        GridData typeTextData = new GridData(GridData.FILL_HORIZONTAL);
        typeTextData.minimumWidth = 400;
        typeField.setText(type);
        typeField.setLayoutData(typeTextData);
        typeField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                type = typeField.getText();
                updateButtons();
            }
        });
        Label labelName = new Label(composite, SWT.NONE);
        labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelName.setText(Messages.getString("property.name") + ":");
        final Text nameField = new Text(composite, SWT.BORDER);
        GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
        nameTextData.minimumWidth = 200;
        nameField.setText(name);
        nameField.setLayoutData(nameTextData);
        nameField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                name = nameField.getText();
                updateButtons();
            }
        });
        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(name.length() > 0 && type.length() > 0);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString(updateMode ? "Mapping.update.title" : "Mapping.add.title"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
