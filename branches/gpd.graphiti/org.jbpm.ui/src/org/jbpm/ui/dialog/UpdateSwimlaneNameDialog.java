package org.jbpm.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.resource.Messages;

public class UpdateSwimlaneNameDialog extends Dialog {

    private String name;
    private final ProcessDefinition definition;
    private final boolean createMode;
    private Button renameInVarButton;
    private boolean proceedRefactoring;

    public UpdateSwimlaneNameDialog(ProcessDefinition definition, boolean createMode) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        this.name = definition.getNextSwimlaneName();
        this.createMode = createMode;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        final Label labelTitle = new Label(area, SWT.NO_BACKGROUND);
        final GridData labelData = new GridData();
        labelTitle.setLayoutData(labelData);
        labelTitle.setText(Messages.getString(createMode ? "SwimlaneWizard.create.message" : "SwimlaneWizard.update.message"));

        final Composite composite = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());

        Label labelName = new Label(composite, SWT.NONE);
        labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelName.setText(Messages.getString("property.name") + ":");
        final Text nameField = new Text(composite, SWT.BORDER);
        GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
        nameTextData.minimumWidth = 200;
        nameField.setText(name);
        nameField.setLayoutData(nameTextData);
        // nameField.addKeyListener(new VariableNameChecker(nameField));
        nameField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                name = nameField.getText().replaceAll(" ", "_");
                updateButtons();
            }
        });

        if (!createMode) {
            renameInVarButton = new Button(area, SWT.CHECK);
            renameInVarButton.setLayoutData(new GridData());
            renameInVarButton.setText(Messages.getString("SwimlaneWizard.renameInVariables"));
            renameInVarButton.setSelection(true);
            renameInVarButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateButtons();
                }
            });
        }
        if (createMode) {
            nameField.selectAll();
        }
        return area;
    }

    private void updateButtons() {
        boolean allowCreation = !definition.getVariableNames(true).contains(name);
        allowCreation &= VariableNameChecker.isNameValid(name);
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString(createMode ? "SwimlaneWizard.create.title" : "SwimlaneWizard.update.title"));
    }

    public String getName() {
        return name;
    }

    public boolean isProceedRefactoring() {
        return proceedRefactoring;
    }

    @Override
    protected void okPressed() {
        proceedRefactoring = renameInVarButton != null ? renameInVarButton.getSelection() : false;
        super.okPressed();
    }
}
