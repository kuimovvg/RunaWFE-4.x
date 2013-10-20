package ru.cg.runaex.components_plugin.database_property_editor;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components.bean.component.part.DefaultValueType;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.DescriptorLocalizationFactory;
import ru.cg.runaex.components_plugin.property_editor.database.SelectColumnReferenceDialog;
import ru.cg.runaex.components_plugin.util.VariableUtils;
import ru.runa.gpd.extension.handler.GroovyActionHandlerProvider;

public class SelectDefaultValueDialog extends TitleAreaDialog {
    private ru.cg.runaex.components_plugin.Localization localization;
    private Combo defaultValueTypeField;
    private Text defaultValueField;
    private DefaultValueType defaultValueType;
    private DefaultValue defValue;
    private DefaultValue selectedDefaultValue;
    private Button browseBtn;

    private SelectionAdapter columnReferenceSelectionAdapter = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            IEditorPart actEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            IProject currentProject = null;
            if (actEditor != null) {
                IFile formFile = (IFile) (((IFileEditorInput) (actEditor.getEditorInput())).getFile());
                currentProject = formFile.getProject();
            }
            SelectColumnReferenceDialog dialog = new SelectColumnReferenceDialog(currentProject);
            dialog.open();
            ColumnReference ref = null;

            switch (dialog.getReturnCode()) {
            case Window.OK: {
                ref = dialog.getSelectedColumnReference();
                break;
            }
            case Window.CANCEL: {
                // Do nothing
                break;
            }
            }
            if (ref != null) {
                defaultValueField.setText(ref.toString());
            }
        }
    };

    private SelectionAdapter groovyDialogSelectionAdapter = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            String delegationConfiguration = StringEscapeUtils.unescapeJava(defaultValueField.getText());
            GroovyActionHandlerProvider.ConfigurationDialog dialog = new GroovyActionHandlerProvider.ConfigurationDialog(delegationConfiguration, VariableUtils.getVariables());
            dialog.open();
            String result = null;
            switch (dialog.getReturnCode()) {
            case Window.OK: {
                result = dialog.getResult();
                break;
            }
            case Window.CANCEL: {
                // Do nothing
                break;
            }
            }
            if (result != null) {
                defaultValueField.setText(result);
            }
        }
    };

    public SelectDefaultValueDialog(DefaultValue selectedDefaultValue) {
        super(Display.getCurrent().getActiveShell());
        localization = DescriptorLocalizationFactory.getSelectDefaultValueDialogLocalization();
        this.selectedDefaultValue = selectedDefaultValue;
    }

    @Override
    public void create() {
        super.create();
        // Set the title
        setTitle(localization.get("dialog.title"));

    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 210);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);

        // The text fields will grow with the size of the dialog
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        Label label1 = new Label(parent, SWT.NONE);
        label1.setText(localization.get("dialog.defaultValueType"));

        defaultValueTypeField = new Combo(parent, SWT.READ_ONLY);
        defaultValueTypeField.setLayoutData(gridData);
        defaultValueTypeField.setBounds(50, 50, 150, 65);
        String items[] = { localization.get("dialog.defaultValueType.value.manual"),
//                localization.get("dialog.defaultValueType.value.fromDb"),
                localization.get("dialog.defaultValueType.value.executeGroovy") };
        defaultValueTypeField.setItems(items);

        defaultValueTypeField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                onDefaultValueTypeChange();
            }
        });

        Label label2 = new Label(parent, SWT.NONE);
        label2.setText(localization.get("dialog.defaultValue"));

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout1 = new GridLayout();
        layout1.marginWidth = 0;
        layout1.marginHeight = 0;
        layout1.numColumns = 2;
        composite.setLayout(layout1);
        GridData data = new GridData(GridData.FILL);
        data.horizontalSpan = 1;
        composite.setLayoutData(data);
        // You should not re-use GridData
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.widthHint = 170;
        defaultValueField = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        defaultValueField.setLayoutData(gridData);

        browseBtn = new Button(composite, SWT.PUSH);
        browseBtn.setText("...");
        browseBtn.setEnabled(false);
        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        buttonData.widthHint = 30;
        browseBtn.setLayoutData(buttonData);

        if (selectedDefaultValue != null) {
            if (selectedDefaultValue.getType() != DefaultValueType.NONE) {
                defaultValueTypeField.select(selectedDefaultValue.getType().getValue());
            }
            defaultValueField.setText(selectedDefaultValue.getValue());
        }
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL_HORIZONTAL;
        gridData.horizontalSpan = 3;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.RIGHT;

        parent.setLayoutData(gridData);
        // Create Add button
        // Own method as we need to overview the SelectionAdapter
        createOkButton(parent, OK, localization.get("dialog.select"), true);
        // Add a SelectionListener

        // Create Cancel button
        Button cancelButton = createButton(parent, CANCEL, localization.get("dialog.cancel"), false);
        // Add a SelectionListener
        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(CANCEL);
                close();
            }
        });
    }

    protected Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
        // increment the number of columns in the okButton bar
        ((GridLayout) parent.getLayout()).numColumns++;
        Button okButton = new Button(parent, SWT.PUSH);
        okButton.setText(label);
        okButton.setFont(JFaceResources.getDialogFont());
        okButton.setData(new Integer(id));
        okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (isValidInput()) {
                    okPressed();
                }
            }
        });
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(okButton);
            }
        }
        setButtonLayoutData(okButton);
        return okButton;
    }

    private boolean isValidInput() {
        boolean valid = true;
        int selectionIndex = defaultValueTypeField.getSelectionIndex();
        if (selectionIndex < 0) {
            setErrorMessage(localization.get("dialog.message.enterDefaultValueType"));
            valid = false;
        }
        if (valid && defaultValueField.getText().length() == 0) {
            setErrorMessage(localization.get("dialog.message.enterDefaultValue"));
            valid = false;
        }
        return valid;
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    // Copy textFields because the UI gets disposed
    // and the Text Fields are not accessible any more.
    private void saveInput() {
        defValue = new DefaultValue(defaultValueType, defaultValueField.getText());
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(localization.get("dialog.title"));
    }

    public DefaultValue getDefaultValue() {
        return defValue;
    }

    private void onDefaultValueTypeChange() {
        int selectionIndex = defaultValueTypeField.getSelectionIndex();
        if (selectionIndex >= 0) {
            Listener[] listeners = browseBtn.getListeners(SWT.Selection);
            for (Listener listener : listeners) {
                browseBtn.removeListener(SWT.Selection, listener);
            }

            defaultValueField.setText("");
            switch (selectionIndex) {
            case 0:
                browseBtn.setVisible(false);
                defaultValueField.setEditable(true);
                defaultValueType = DefaultValueType.MANUAL;
                break;
            case 1:
                browseBtn.setVisible(true);
                browseBtn.setEnabled(true);
                browseBtn.addSelectionListener(columnReferenceSelectionAdapter);
                defaultValueField.setEditable(false);
                defaultValueType = DefaultValueType.FROM_DB;
                break;
            case 2:
                browseBtn.setVisible(true);
                browseBtn.setEnabled(true);
                browseBtn.addSelectionListener(groovyDialogSelectionAdapter);
                defaultValueField.setEditable(false);
                defaultValueType = DefaultValueType.EXECUTE_GROOVY;
                break;
            default:
                break;
            }

        }
    }
}
