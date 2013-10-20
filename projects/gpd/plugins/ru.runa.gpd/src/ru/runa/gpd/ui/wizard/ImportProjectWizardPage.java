package ru.runa.gpd.ui.wizard;

import java.io.File;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;

import ru.runa.gpd.Localization;

public class ImportProjectWizardPage extends WizardFileSystemResourceExportPage1 {
    private final static String STORE_DESTINATION_NAMES_ID = "WizardParExportPage1.STORE_DESTINATION_NAMES_ID";

    private Button exportToFileButton;

    private String fileName;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    protected ImportProjectWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ImportProjectWizardPage.page.title"));
        setDescription(Localization.getString("ImportProjectWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group exportGroup = new Group(sashForm, SWT.NONE);
        exportGroup.setLayout(new GridLayout(1, false));
        exportGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDestinationGroup(exportGroup);

        restoreWidgetValues();
        giveFocusToDestination();
        setControl(pageControl);
        setPageComplete(getDestinationValue() != null);
    }

    @Override
    protected String getDestinationLabel() {
        return Localization.getString("ImportProjectWizardPage.label.file");
    }

    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.wba", "*.*" });
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();
        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
            this.setFileName(dialog.getFileName());
            this.setSelectedDirFileName(dialog.getFilterPath());
            this.setSelectedFileNames(dialog.getFileNames());
        }
    }

    @Override
    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }
            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
        }
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
                return; // ie.- no settings stored
            }
            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++) {
                addDestinationItem(directoryNames[i]);
            }
        }
    }

    public String getSelectedDirFileName() {
        return selectedDirFileName;
    }

    public void setSelectedDirFileName(String selectedDirFileName) {
        this.selectedDirFileName = selectedDirFileName;
    }

    public String[] getSelectedFileNames() {
        return selectedFileNames;
    }

    public void setSelectedFileNames(String[] selectedFileNames) {
        this.selectedFileNames = selectedFileNames;
    }

    public String getCorrectFileName() {
        if (fileName != null) {
            this.fileName = fileName.replace(".wba", "");
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
