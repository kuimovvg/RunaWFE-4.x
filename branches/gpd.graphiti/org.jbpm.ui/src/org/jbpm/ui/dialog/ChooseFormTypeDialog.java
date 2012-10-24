package org.jbpm.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.forms.FormType;
import org.jbpm.ui.forms.FormTypeProvider;
import org.jbpm.ui.pref.PrefConstants;
import org.jbpm.ui.resource.Messages;

public class ChooseFormTypeDialog extends Dialog {
    public final static String EDITOR_INTERNAL = Messages.getString("ChooseFormTypeDialog.editor.internal");
    public final static String EDITOR_EXTERNAL = Messages.getString("ChooseFormTypeDialog.editor.external");

    private Combo typeCombo;
    private Combo editorTypeCombo;

    private String type;
    private String editorType;

    public ChooseFormTypeDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public Control createDialogArea(final Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);

        final Composite composite = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);

        typeCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData typeComboData = new GridData(GridData.FILL_HORIZONTAL);
        typeComboData.minimumWidth = 200;
        typeCombo.setLayoutData(typeComboData);
        for (FormType formType : FormTypeProvider.getRegisteredFormTypes()) {
            typeCombo.add(formType.getName());
        }
        typeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateVisibility();
            }
        });

        Label openInLabel = new Label(composite, SWT.NONE);
        openInLabel.setText(Messages.getString("ChooseFormTypeDialog.openInEditor"));

        editorTypeCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        editorTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editorTypeCombo.add(EDITOR_INTERNAL);
        if (DesignerPlugin.getPrefBoolean(PrefConstants.P_FORM_USE_EXTERNAL_EDITOR)) {
            editorTypeCombo.add(EDITOR_EXTERNAL);
        }
        editorTypeCombo.select(0);
        editorTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateVisibility();
            }
        });

        return area;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        typeCombo.select(0);
        updateVisibility();
        return control;
    }

    private void updateVisibility() {
        String name = typeCombo.getItem(typeCombo.getSelectionIndex());
        type = FormTypeProvider.getFormTypeByName(name).getType();
        editorType = editorTypeCombo.getItem(editorTypeCombo.getSelectionIndex());
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("FormTypeForm.selectType"));
    }

    public String getType() {
        return type;
    }

    public String getEditorType() {
        return editorType;
    }
}
