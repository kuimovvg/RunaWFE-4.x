package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.google.common.base.Preconditions;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;

public class QuickFormVariableWizard extends Wizard implements INewWizard {
    private QuickFormVariableWizardPage page;
    private ProcessDefinition processDefinition;
    private int editIndex = -1;
    private List<QuickFormGpdVariable> quickFormVariableDefs;

    public QuickFormVariableWizard(ProcessDefinition processDefinition, List<QuickFormGpdVariable> templatedVariableDefs, int editIndex) {
        this.processDefinition = processDefinition;
        this.quickFormVariableDefs = templatedVariableDefs;
        this.editIndex = editIndex;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        if (editIndex != -1) {
            page = new QuickFormVariableWizardPage(processDefinition, quickFormVariableDefs.get(editIndex));
        } else {
            page = new QuickFormVariableWizardPage(processDefinition, null);
        }

        addPage(page);
    }

    @Override
    public boolean performFinish() {
        QuickFormGpdVariable variableDef = null;
        if (editIndex != -1) {
            variableDef = quickFormVariableDefs.get(editIndex);
        } else {
            variableDef = new QuickFormGpdVariable();
        }
        variableDef.setTagName(page.getTagType());
        Variable variable = page.getVariable();
        Preconditions.checkNotNull(variable, "Variable is null");
        variableDef.setName(variable.getName());
        variableDef.setFormat(variable.getFormat());
        variableDef.setFormatLabel(variable.getFormatLabel());
        variableDef.setJavaClassName(variable.getJavaClassName());
        variableDef.setDescription(variable.getDescription());
        if (page.getParamValue() != null && !page.getParamValue().isEmpty()) {
            List<String> param = new ArrayList<String>();
            param.add(page.getParamValue());
            variableDef.setParams(param.toArray(new String[0]));
        }
        if (editIndex == -1) {
            quickFormVariableDefs.add(variableDef);
        }
        return true;
    }

}
