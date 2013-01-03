package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.handler.action.ParamDef;
import ru.runa.gpd.handler.action.ParamDefGroup;

public class ParamDefWizard extends Wizard implements INewWizard {
    private ParamDefWizardPage page;
    private ParamDefGroup paramDefGroup;
    private ParamDef paramDef;

    public ParamDefWizard(ParamDefGroup paramDefGroup, ParamDef paramDef) {
        this.paramDefGroup = paramDefGroup;
        this.paramDef = paramDef;
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new ParamDefWizardPage(paramDefGroup, paramDef);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        if (page.getParamDef() != null) {
            paramDefGroup.getParameters().remove(paramDef);
        }
        ParamDef paramDef = new ParamDef(page.getName(), page.getLabel());
        paramDef.getFormatFilters().add(page.getType());
        paramDef.setUseVariable(page.isVariable());
        paramDefGroup.getParameters().add(paramDef);
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }
}
