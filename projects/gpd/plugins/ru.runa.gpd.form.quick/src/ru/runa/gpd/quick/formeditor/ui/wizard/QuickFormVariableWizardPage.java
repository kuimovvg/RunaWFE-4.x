package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;

public class QuickFormVariableWizardPage extends WizardPage {
	private ComboViewer tagType;
	private Combo variableCombo;
	private ProcessDefinition processDefinition;
	private QuickFormGpdVariable variableDef;
	private String paramValue;
	
	protected QuickFormVariableWizardPage(ProcessDefinition processDefinition, QuickFormGpdVariable variableDef) {
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
		if (tagType.getCombo().getText().length() == 0) {
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
        List<SelectItem> types = new ArrayList<SelectItem>();        
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();
        
        for(String value : freemarkerConfiguration.getTagsName()) {
        	if (MethodTag.hasTag(value)) {
                MethodTag tag = MethodTag.getTagNotNull(value);
                types.add(new SelectItem(tag.name, value));
                continue;
        	}
        }
        
        tagType = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
       
        tagType.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagType.setContentProvider(ArrayContentProvider.getInstance());
        tagType.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof SelectItem) {
                	SelectItem current = (SelectItem) element;

                	return current.getLabel();
                }
                return "";
            }
        });
        tagType.setInput(types.toArray(new SelectItem[types.size()]));
        tagType.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent  e) {
            	IStructuredSelection selection = (IStructuredSelection) e.getSelection();
            	SelectItem selectItem = (SelectItem)selection.getFirstElement();
                
            	variableDef.setTagName(selectItem.getValue().toString());
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
        List<String> names = processDefinition.getVariableNames(true);
    	variableCombo.setItems(names.toArray(new String[names.size()]));
        variableCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                verifyContentsValid();
            }
        });
	}
	
	private void createParamField(Composite parent) {
		Map<String, MethodTag> methodTags = MethodTag.getAll();
		if(methodTags != null) {
			for(MethodTag methodTag : methodTags.values()) {
				if(methodTag.name.equals(tagType.getCombo().getText())) {
					for(int i = 1; i < methodTag.params.size(); i++) {
						Param param = methodTag.params.get(i);
						
						Label label = new Label(parent, SWT.NONE);
				        label.setText(param.label);
				        if(param.isCombo() || param.isVarCombo()) {
				        	final ComboViewer comboParam = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
				        	comboParam.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				        	comboParam.setContentProvider(ArrayContentProvider.getInstance());
				        	comboParam.setLabelProvider(new LabelProvider() {
				                @Override
				                public String getText(Object element) {
				                    if (element instanceof SelectItem) {
				                    	SelectItem current = (SelectItem) element;

				                    	return current.getLabel();
				                    }
				                    return "";
				                }
				            });
				        	
				        	if(param.optionalValues != null) {
				        		List<SelectItem> selectItems = new ArrayList<SelectItem>(param.optionalValues.size());
				        		for(OptionalValue optionalValue : param.optionalValues) {
				        			SelectItem selectItem = new SelectItem(optionalValue.value, optionalValue.name);
				        			selectItems.add(selectItem);
					        	}
				        		comboParam.setInput(selectItems);
				        	}
				        	
				        	if (paramValue != null) {
				        		List<SelectItem> selectItems = (List<SelectItem>) comboParam.getInput();
				            	for(SelectItem selectItem : selectItems) {
				            		if(paramValue.equals(selectItem.getValue())) {
				            			comboParam.getCombo().setText(selectItem.getLabel());
				            			break;
				            		}
				            	}
						    }
				        	comboParam.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
								@Override
								protected void onSelectionChanged(SelectionChangedEvent e) throws Exception {
									IStructuredSelection selection = (IStructuredSelection) e.getSelection();
				                	SelectItem selectItem = (SelectItem)selection.getFirstElement();				                    
				                	paramValue = selectItem.getValue().toString();
				                    verifyContentsValid();									
								}
				            });
				        } else {
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
				}
			}
		}
	}
	
	private void populateValues() {
        if(variableDef != null && variableDef.getTagName() != null) {
        	SelectItem[] selectItems = (SelectItem[]) tagType.getInput();
        	for(SelectItem selectItem : selectItems) {
        		if(variableDef.getTagName().equals(selectItem.getValue())) {
        			tagType.getCombo().setText(selectItem.getLabel());
        			break;
        		}
        	}     	
        }    
		if(variableDef != null && variableDef.getName() != null) {
        	variableCombo.setText(variableDef.getName());
        }
		if(variableDef != null && variableDef.getParams() != null && variableDef.getParams().length > 0) {
			paramValue = variableDef.getParams()[0];
		}
	}
	
	public String getTagType() {
		SelectItem[] selectItems = (SelectItem[]) tagType.getInput();
    	for(SelectItem selectItem : selectItems) {
    		if(tagType.getCombo().getText().equals(selectItem.getLabel())) {
    			return selectItem.getValue().toString();
    		}
    	}
    	
		return "";
	}
	
	public Variable getVariable() {
		if(variableCombo.getText() != null && !variableCombo.getText().isEmpty()) {
			return processDefinition.getVariable(variableCombo.getText(), true);
		}
		
		return null;
	}
	
	public String getParamValue() {
		return paramValue;
	}	
	
	public static class SelectItem {
		private String label;
		private Object value;
		
		public SelectItem(String label, Object value) {
			this.label = label;
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}
}
