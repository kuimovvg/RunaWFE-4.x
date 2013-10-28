package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.quick.resource.Messages;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class TemplatedFormVariableWizardPage extends WizardPage {
	private Combo tagType;
	private Combo variableCombo;
	private ProcessDefinition processDefinition;
	private QuickFormGpdVariable variableDef;
	//private String tempTagType;
	private String paramValue;
	
	protected TemplatedFormVariableWizardPage(ProcessDefinition processDefinition, QuickFormGpdVariable variableDef) {
		super(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setTitle(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setDescription(Messages.getString("TemplatedFormVariableWizardPage.page.description"));
        this.processDefinition = processDefinition;
        this.variableDef = new QuickFormGpdVariable();
        if(variableDef != null) {
        	this.variableDef.setTagName(variableDef.getTagName());
            this.variableDef.setName(variableDef.getName());
            this.variableDef.setFormat(variableDef.getFormat());
    		this.variableDef.setFormatLabel(variableDef.getFormatLabel());
    		this.variableDef.setJavaClassName(variableDef.getJavaClassName());
    		this.variableDef.setDescription(variableDef.getDescription());
    		this.variableDef.setParams(variableDef.getParams());
        }        
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		rebuildView(parent);        
	}
	
	private void rebuildView(Composite parent) {
        for (Control control : parent.getChildren()) {
            control.dispose();
        }
        
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createTagTypeField(composite);
        createVariablesField(composite);
        populateValues();
        createParamField(composite);
        verifyContentsValid();
        setControl(composite);
        Dialog.applyDialogFont(composite);
        parent.layout(true, true);
        if (variableDef == null) {
            setPageComplete(false);
        }
    }
	
	private void verifyContentsValid() {
		if (tagType.getText().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_tag"));
            setPageComplete(false);
        } else if (variableCombo.getText().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_variable"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }
	
	private void createTagTypeField(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.tag"));
        List<String> types = new ArrayList<String>();        
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
        
        for(String value : freemarkerConfiguration.getTagsName()) {
        	types.add(value);
        }
        
        tagType = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        tagType.setItems(types.toArray(new String[types.size()]));
        tagType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        /*if(tempTagType != null) {
        	tagType.setText(tempTagType);
        } */    
        tagType.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	//tempTagType = tagType.getText();
            	variableDef.setTagName(tagType.getText());
            	rebuildView(parent.getParent());           	
                verifyContentsValid();
            }
        });
    }
	
	private void createVariablesField(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.var"));
        variableCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        List<String> names = processDefinition.getVariableNames(false);
    	variableCombo.setItems(names.toArray(new String[names.size()]));
        variableCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                verifyContentsValid();
            }
        });
	}
	
	private void createParamField(Composite parent) {
		if(tagType.getText() != null && !tagType.getText().isEmpty() && "DisplayVariable".equals(tagType.getText())) {
			/*FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
			FreemarkerTagGpdWrap freemarkerTagGpdWrap = null;
			try {
				freemarkerTagGpdWrap = freemarkerConfiguration.getTag(tagType.getText());
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for(int i = 1; i < freemarkerTagGpdWrap.getArgumentsSize(); i++) {
				Label label = new Label(parent, SWT.NONE);
		        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.param"));
				final Text text = new Text(parent, SWT.NONE);
			     text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			     if (paramValue != null) {
			    	 text.setText(paramValue);
			     }
			     
			     text.addModifyListener(new LoggingModifyTextAdapter() {
			        @Override
			        protected void onTextChanged(ModifyEvent e) throws Exception {
			        	paramValue = text.getText();
			            verifyContentsValid();
			        }
			     });
			}*/	
			Label label = new Label(parent, SWT.NONE);
	        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.param"));
			final Text text = new Text(parent, SWT.NONE);
		     text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		     if (paramValue != null) {
		    	 text.setText(paramValue);
		     }
		     
		     text.addModifyListener(new LoggingModifyTextAdapter() {
		        @Override
		        protected void onTextChanged(ModifyEvent e) throws Exception {
		        	paramValue = text.getText();
		            verifyContentsValid();
		        }
		     });
		}
	}
	
	private void populateValues() {
        if(variableDef != null && variableDef.getTagName() != null) {
        	tagType.setText(variableDef.getTagName());
        }    
		if(variableDef != null && variableDef.getName() != null) {
        	variableCombo.setText(variableDef.getName());
        }
		if(variableDef != null && variableDef.getParams() != null && variableDef.getParams().length > 0) {
			paramValue = variableDef.getParams()[0];
		}
	}
	
	public String getTagType() {
		return tagType.getText();
	}
	
	public Variable getVariable() {
		if(variableCombo.getText() != null && !variableCombo.getText().isEmpty()) {
			return processDefinition.getVariable(variableCombo.getText(), false);
		}
		
		return null;
	}
	
	public String getParamValue() {
		return paramValue;
	}
}
