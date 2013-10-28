package ru.runa.gpd.quick.formeditor.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class BrowserWizard extends Wizard implements INewWizard  {
	private BrowserWizardPage page;
	private String htmlContent;
	
	public BrowserWizard(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {		
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	@Override
    public void addPages() {
        super.addPages();
        page = new BrowserWizardPage("", htmlContent);
        addPage(page);
    }

}
