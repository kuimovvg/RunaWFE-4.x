package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.runa.gpd.Localization;

public class ChangeDbConfigurationFileDialog extends Dialog {
    private Button selectBtn;
    public static final String[] FILTER_EXT = new String[] { "*.xml" };
    private Text filePathField;
    private String filePath;

    public ChangeDbConfigurationFileDialog() {
        super(Display.getCurrent().getActiveShell());
    }

    protected Point getInitialSize() {
        return new Point(450, 100);
    }

    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 3;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Dialog.applyDialogFont(composite);
        Label label = new Label(composite, SWT.NONE);
        label.setText(Localization.getString("label.process.display.dbConfigFilePath"));
        filePathField = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 170;
        filePathField.setLayoutData(data);
        filePathField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                selectBtn.setEnabled(filePathField.getText().length() != 0);
                String path = filePathField.getText();
                filePath = path != null && !path.isEmpty() ? path : null;
            }
        });
        Button button = new Button(composite, SWT.PUSH);
        button.setText(Localization.getString("ChangeDbConfigurationFileDialog.browseBtn"));
        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        buttonData.widthHint = 30;
        button.setLayoutData(buttonData);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                dialog.setFilterExtensions(FILTER_EXT);
                String path = dialog.open();
                if (path != null) {
                    filePathField.setText(path);
                }
            }
        });

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        ((GridData) parent.getLayoutData()).horizontalAlignment = SWT.CENTER;
        selectBtn = createButton(parent, IDialogConstants.OK_ID, Localization.getString("ChangeDbConfigurationFileDialog.ok"), true);
        selectBtn.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, Localization.getString("ChangeDbConfigurationFileDialog.cancel"), false);
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("ChangeDbConfigurationFileDialog.title"));
    }

    public String getDBConfigFilePath() {
        return filePath;
    }
}
