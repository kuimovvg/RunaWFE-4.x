package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class QuickFormVariabliesToDisplayWizardPage extends WizardPage {

	private ProcessDefinition processDefinition;
	private List<String> selectedVariables = new ArrayList<String>();
	private List<String> initialVariables = new ArrayList<String>();
	
	protected QuickFormVariabliesToDisplayWizardPage(ProcessDefinition processDefinition, List<QuickFormGpdVariable> quickFormVariableDefs) {
		super(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.title"));
        setTitle(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.title"));
        setDescription(Messages.getString("QuickFormVariabliesToDisplayWizardPage.page.description"));
		this.processDefinition = processDefinition;
		if(quickFormVariableDefs != null && quickFormVariableDefs.size() > 0) {
			for(QuickFormGpdVariable variable : quickFormVariableDefs) {
				selectedVariables.add(variable.getName());
				initialVariables.add(variable.getName());
			}
		}		
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		createView(parent);        
	}
	
	private void createView(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        createVariableCheckboxes(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
    }
	
	private void createVariableCheckboxes(final Composite parent) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        final Composite checkboxesArea = new Composite(scrolledComposite, SWT.NONE);
        
		checkboxesArea.setLayout(new GridLayout(1, true));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        checkboxesArea.setLayoutData(gridData);
        
		List<String> names = processDefinition.getVariableNames(true);
		
		for(String name : names) {
			createCheckbox(checkboxesArea, name);	        
		}

        scrolledComposite.setContent(checkboxesArea);
        scrolledComposite.setMinSize(checkboxesArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));        
	}
	
	private Button createCheckbox(final Composite parent, final String name) {
		final Button variableCheckbox = new Button(parent, SWT.CHECK);
        variableCheckbox.setSelection(selectedVariables.contains(name));
        variableCheckbox.setEnabled(!selectedVariables.contains(name));
        variableCheckbox.setText(name);
        variableCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
        	@Override
            protected void onSelection(SelectionEvent e) throws Exception {
        		if(selectedVariables.contains(name)) {
        			selectedVariables.remove(name);
        		} else {
        			selectedVariables.add(name);
        		}        		
        		setPageComplete(true);
        	}
        });
        
        return variableCheckbox;
	}
	
	public List<String> getSelectedVariables() {
		selectedVariables.removeAll(initialVariables);
		return selectedVariables;
	}
}
