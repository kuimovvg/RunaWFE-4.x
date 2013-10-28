package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;

public class TemplatedFormVariableWizard extends Wizard implements INewWizard {
	private TemplatedFormVariableWizardPage page;
	private ProcessDefinition processDefinition;
	private int editIndex = -1;
	private List<QuickFormGpdVariable> templatedVariableDefs;
	
	public TemplatedFormVariableWizard(ProcessDefinition processDefinition, List<QuickFormGpdVariable> templatedVariableDefs, int editIndex) {
		this.processDefinition = processDefinition;
		this.templatedVariableDefs = templatedVariableDefs;
		this.editIndex = editIndex;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {		
	}
	
	@Override
    public void addPages() {
        super.addPages();
        if(editIndex != -1) {
        	page = new TemplatedFormVariableWizardPage(processDefinition, templatedVariableDefs.get(editIndex));
        } else {
        	page = new TemplatedFormVariableWizardPage(processDefinition, null);
        }
        
        addPage(page);
    }

	@Override
	public boolean performFinish() {
		
		QuickFormGpdVariable variableDef = null;
		if(editIndex != -1) {
			variableDef = templatedVariableDefs.get(editIndex);			
		} else {
			variableDef = new QuickFormGpdVariable();
		}		
		variableDef.setTagName(page.getTagType());
		variableDef.setName(page.getVariable().getName());
		variableDef.setFormat(page.getVariable().getFormat());
		variableDef.setFormatLabel(page.getVariable().getFormatLabel());
		variableDef.setJavaClassName(page.getVariable().getJavaClassName());
		variableDef.setDescription(page.getVariable().getDescription());
		if(page.getParamValue() != null && !page.getParamValue().isEmpty()) {
			List<String> param = new ArrayList<String>();
			param.add(page.getParamValue());
			variableDef.setParams(param.toArray(new String[0]));
		}
		
		if(editIndex == -1) {
			templatedVariableDefs.add(variableDef);
		}		
		
		return true;
	}

}
