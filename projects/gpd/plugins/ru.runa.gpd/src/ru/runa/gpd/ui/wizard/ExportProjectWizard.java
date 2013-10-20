package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class ExportProjectWizard extends Wizard implements IExportWizard {

	private IStructuredSelection selection;
    private ExportProjectWizardPage page;

    public ExportProjectWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ExportProjectWizard");
        if (section == null) {
            section = workbenchSettings.addNewSection("ExportProjectWizard");
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setWindowTitle(Localization.getString("ExportProjectWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        page = new ExportProjectWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        return page.finish();
    }
}
