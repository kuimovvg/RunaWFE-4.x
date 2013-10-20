package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import ru.runa.gpd.Localization;

@SuppressWarnings("restriction")
public class NewProcessProjectWizardPage extends WizardNewProjectCreationPage {
    public static final String[] FILTER_EXT = new String[] { "*.xml" };
    private Text filePath;

    public NewProcessProjectWizardPage(String string) {
        super(string);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        initializeDialogUnits(parent);
        Composite control = (Composite) getControl();
        Composite composite = new Composite(control, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 3;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        init(composite);
        Dialog.applyDialogFont(composite);
    }

    private void init(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.process.display.dbConfigFilePath"));
        filePath = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 170;
        filePath.setLayoutData(data);
        Button button = new Button(parent, SWT.PUSH);
        button.setText(IDEWorkbenchMessages.ProjectLocationSelectionDialog_browseLabel);
        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        buttonData.widthHint = 30;
        button.setLayoutData(buttonData);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                dialog.setFilterExtensions(FILTER_EXT);
                String path = dialog.open();
                if (path != null) {
                    filePath.setText(path);
                }
            }
        });
    }

    public String getDBConfigFilePath() {
        String path = filePath.getText();
        return path != null && !path.isEmpty() ? path : null;
    }
}
