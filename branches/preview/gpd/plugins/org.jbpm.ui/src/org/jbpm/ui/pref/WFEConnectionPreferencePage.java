package ru.runa.bpm.ui.pref;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.dialog.ErrorDialog;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.sync.WFEServerConnector;

public class WFEConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {

    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private Button testButton;

    public WFEConnectionPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
        setTitle(Messages.getString("pref.connection.wfe.title"));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new StringFieldEditor(P_CONNECTION_WFE_PROVIDER_URL, Messages.getString("pref.connection.wfe.server"), getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(P_CONNECTION_LOGIN_MODE, Messages.getString("pref.connection.loginMode"), 2, new String[][] {
                { Messages.getString("pref.connection.loginMode.byLogin"), LOGIN_MODE_LOGIN_PASSWORD },
                { Messages.getString("pref.connection.loginMode.byKerberos"), LOGIN_MODE_KERBEROS } }, getFieldEditorParent()));
        loginEditor = new StringFieldEditor(P_CONNECTION_LOGIN, Messages.getString("pref.connection.login"), getFieldEditorParent());
        passwordEditor = new StringFieldEditor(P_CONNECTION_PASSWORD, Messages.getString("pref.connection.password"), getFieldEditorParent());
        passwordEditor.setEmptyStringAllowed(true);
        boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(DesignerPlugin.getPrefString(P_CONNECTION_LOGIN_MODE));
        loginEditor.setEnabled(enabled, getFieldEditorParent());
        passwordEditor.setEnabled(enabled, getFieldEditorParent());
        addField(loginEditor);
        addField(passwordEditor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_CONNECTION_LOGIN_MODE.equals(fieldEditor.getPreferenceName())) {
                boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(event.getNewValue());
                loginEditor.setEnabled(enabled, getFieldEditorParent());
                passwordEditor.setEnabled(enabled, getFieldEditorParent());
            }
        }
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        testButton = new Button(buttonBar, SWT.PUSH);
        testButton.setText(Messages.getString("button.test.connection"));
        Dialog.applyDialogFont(testButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        testButton.setLayoutData(data);
        testButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    performApply();
                    WFEServerConnector.getInstance().connect();
                    MessageDialog.openInformation(getShell(), Messages.getString("button.test.connection"), Messages.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    ErrorDialog.open(Messages.getString("error.ConnectionFailed"), th);
                }
            }
        });
        testButton.setEnabled(isValid());
        applyDialogFont(buttonBar);
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        testButton.setEnabled(isValid());
    }

}
