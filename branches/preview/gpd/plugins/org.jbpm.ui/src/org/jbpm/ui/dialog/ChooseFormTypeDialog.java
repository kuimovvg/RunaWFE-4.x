package ru.runa.bpm.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.forms.FormType;
import ru.runa.bpm.ui.forms.FormTypeProvider;
import ru.runa.bpm.ui.forms.XSNFormType;
import ru.runa.bpm.ui.pref.PrefConstants;
import ru.runa.bpm.ui.resource.Messages;

public class ChooseFormTypeDialog extends Dialog {

    public final static String INFOPATH_EDITOR_PREFERENCE_ID = "infopath.editor.filepath";
    public final static String EDITOR_INTERNAL = Messages.getString("ChooseFormTypeDialog.editor.internal");
    public final static String EDITOR_INFOPATH = Messages.getString("ChooseFormTypeDialog.editor.InfoPath");
    public final static String EDITOR_EXTERNAL = Messages.getString("ChooseFormTypeDialog.editor.external");

    private Combo typeCombo;
    private Combo editorTypeCombo;
    private Button infoPathChoosePathButton;
    private String infoPathFilePath;

    private String type;
    private String editorType;

    public ChooseFormTypeDialog(Shell parentShell) {
        super(parentShell);
        infoPathFilePath = PlatformUI.getPreferenceStore().getString(INFOPATH_EDITOR_PREFERENCE_ID);
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
        editorTypeCombo.add(EDITOR_INFOPATH);
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
    protected void createButtonsForButtonBar(final Composite parent) {
        infoPathChoosePathButton = createButton(parent, 197, "MS InfoPath", false);
        infoPathChoosePathButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ExternalEditorDialog dialog = new ExternalEditorDialog(parent.getShell(), infoPathFilePath);
                if (dialog.open() == Window.OK) {
                    infoPathFilePath = dialog.getPath();
                    PlatformUI.getPreferenceStore().setValue(INFOPATH_EDITOR_PREFERENCE_ID, infoPathFilePath);
                    updateVisibility();
                }
            }
        });
        super.createButtonsForButtonBar(parent);
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
        if (XSNFormType.NAME.equals(type)) {
            this.getButton(OK).setEnabled(infoPathFilePath.length() > 0);
            infoPathChoosePathButton.setEnabled(true);
            editorTypeCombo.select(1);
            editorTypeCombo.setEnabled(false);
        } else {
            this.getButton(OK).setEnabled(true);
            infoPathChoosePathButton.setEnabled(false);
            editorTypeCombo.setEnabled(true);
        }
    }

    @Override
    protected void okPressed() {
        if (!XSNFormType.NAME.equals(type) && EDITOR_INFOPATH.equals(editorType)) {
            ErrorDialog errorDialog = new ErrorDialog(Messages.getString("ChooseFormTypeDialog.InfoPathWarning"), null);
            errorDialog.open();
            return;
        }
        super.okPressed();
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
