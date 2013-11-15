package ru.runa.gpd.quick.formeditor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.quick.extension.QuickTemplateArtifact;
import ru.runa.gpd.quick.extension.QuickTemplateRegister;
import ru.runa.gpd.quick.formeditor.ui.wizard.BrowserWizard;
import ru.runa.gpd.quick.formeditor.ui.wizard.QuickFormVariableWizard;
import ru.runa.gpd.quick.formeditor.util.PresentationVariableUtils;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.quick.resource.Messages;
import ru.runa.gpd.quick.tag.FormHashModelGpdWrap;
import ru.runa.gpd.quick.tag.FreemarkerProcessorGpdWrap;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class QuickFormEditor extends EditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
	private static final int NUMBER_NAME_COLUMN = 1;
	public static final int CLOSED = 198;
	public static final String ID = "ru.runa.gpd.quick.formeditor.QuickFormEditor";
	private Composite editorComposite;
	private TableViewer tableViewer;
	private ProcessDefinition processDefinition;
	private QuickForm quickForm;
	private FormNode formNode;
	private IFile quickFormFile;
	private boolean dirty;
	private String prevTemplateFileName;
	private Combo selectTemplateCombo;

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
		    byte[] bytes = QuickFormXMLUtil.convertQuickFormToXML((IFolder) quickFormFile.getParent(), quickForm);
			updateFile(quickFormFile, bytes);
	        if (formNode != null) {
	            formNode.setDirty();
	        }
	        setDirty(false);
		} catch (Exception e) {
			PluginLogger.logError("Error on saving template form: '" + quickForm.getName() + "'", e);
		}
	}

    protected void updateFile(IFile file, byte[] contentBytes) throws CoreException {
        InputStream content = new ByteArrayInputStream(contentBytes);
        if (!file.exists()) {
            file.create(content, true, null);
        } else {
            file.setContents(content, true, true, null);
        }
    }

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
        setInput(input);
        
        quickFormFile = ((FileEditorInput) input).getFile();
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) quickFormFile.getParent());
        this.processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        quickForm = QuickFormXMLUtil.getQuickFormFromXML(quickFormFile, processDefinition);
        if (quickFormFile.getName().startsWith(ParContentProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            String subprocessId = quickFormFile.getName().substring(0, quickFormFile.getName().indexOf("."));
            processDefinition = processDefinition.getEmbeddedSubprocessById(subprocessId);
            Preconditions.checkNotNull(processDefinition, "embedded subpocess");
        }
        for (FormNode formNode : processDefinition.getChildren(FormNode.class)) {
            if (input.getName().equals(formNode.getFormFileName())) {
                this.formNode = formNode;
                setPartName(formNode.getName());
                break;
            }
        }
        
        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        
        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == QuickFormEditor.CLOSED && quickFormFile.exists()) {
                    String op = "create";
                    try {
                        if (!formNode.hasFormValidation()) {
                            String fileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                            IFile validationFile = ValidationUtil.createNewValidationUsingForm(quickFormFile, fileName, formNode);
                            formNode.setValidationFileName(validationFile.getName());
                        } else {
                            op = "update";
                            ValidationUtil.updateValidation(quickFormFile, formNode);
                        }
                    } catch (Exception e) {
                        PluginLogger.logError("Failed to " + op + " form validation", e);
                    }
                }
            }
        });
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		editorComposite = new Composite(parent, SWT.NONE);
        editorComposite.setLayout(new GridLayout());
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        rebuildView(editorComposite);
	}
	
	private void rebuildView(Composite composite) {
        for (Control control : composite.getChildren()) {
            control.dispose();
        }
        
        Composite selectTemplateComposite = new Composite(composite, SWT.NONE);
        selectTemplateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        selectTemplateComposite.setLayout(new GridLayout(2, false));

        Label label = new Label(selectTemplateComposite, SWT.NONE);
        label.setText(Messages.getString("editor.list.label"));
        
        selectTemplateCombo = new Combo(selectTemplateComposite, SWT.BORDER | SWT.READ_ONLY);
        for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
            if (artifact.isEnabled()) {
                selectTemplateCombo.add(artifact.getLabel());
            }
        }
        selectTemplateCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	String label = selectTemplateCombo.getText();
            	String filename = null;
            	for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
                    if (label != null && artifact.isEnabled() && label.equalsIgnoreCase(artifact.getLabel()) ) {
                    	filename = artifact.getFileName();
                    	break;
                    }
                }
            	
            	if(filename != null && filename.trim().length() > 0) {
            		Bundle bundle = QuickTemplateRegister.getBundle(filename);
            		String templateHtml = QuickFormXMLUtil.getTemplateFromRegister(bundle, filename);
            		quickForm.setDelegationClassName(filename);
            		quickForm.setDelegationConfiguration(templateHtml);
            	}
            	
            	if(StringUtils.isNotEmpty(prevTemplateFileName)) {
        			IFile confFile = ((IFolder) quickFormFile.getParent()).getFile(prevTemplateFileName);
                    if (confFile.exists()) {
                    	confFile.delete(true, null);
                    }
        		}
        		
                prevTemplateFileName = filename;
            	setDirty(true);
            }
        });
        selectTemplateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if(!Strings.isNullOrEmpty(quickForm.getDelegationClassName())) {
        	for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
                if (artifact.isEnabled() && artifact.getFileName().equals(quickForm.getDelegationClassName())) {
                	selectTemplateCombo.setText(artifact.getLabel());
                	prevTemplateFileName = artifact.getFileName();
                	break;
                }
            }
        }
        
        Composite tableParamComposite = new Composite(composite, SWT.NONE);
        tableParamComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableParamComposite.setLayout(new GridLayout(2, false));
        
        
        tableViewer = new TableViewer(tableParamComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        getSite().setSelectionProvider(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Messages.getString("editor.table.column.tag"), Messages.getString("editor.table.column.var"),
        		Messages.getString("editor.table.column.type"),  Messages.getString("editor.table.column.rule") };
        int[] columnWidths = new int[] { 150, 200, 100, 100, 100 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        Composite buttonsBar = new Composite(tableParamComposite, SWT.NONE);
        GridData gridDataButton = new GridData(GridData.FILL_HORIZONTAL);
        gridDataButton.minimumWidth = 100;
        tableParamComposite.setLayoutData(gridDataButton);
        tableParamComposite.setLayout(new GridLayout(2, false));
        setTableInput();
        
        buttonsBar.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        buttonsBar.setLayoutData(gridData);
        addButton(buttonsBar, "editor.button.add", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	QuickFormVariableWizard wizard = new QuickFormVariableWizard(processDefinition, quickForm.getQuickFormGpdVariable(), -1);
                CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                if (dialog.open() == Window.OK) {
                    setTableInput();
                    setDirty(true);
                }
            }
        });
        addButton(buttonsBar, "editor.button.edit", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                String[] row = (String[]) selection.getFirstElement();
                if (row == null) {
                    return;
                }
                for (QuickFormGpdVariable variableDef : quickForm.getQuickFormGpdVariable()) {
                    if (variableDef.getName().equals(row[NUMBER_NAME_COLUMN])) {
                    	QuickFormVariableWizard wizard = new QuickFormVariableWizard(processDefinition, quickForm.getQuickFormGpdVariable(), quickForm.getQuickFormGpdVariable().indexOf(variableDef));
                        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput();
                            setDirty(true);
                        }
                        break;
                    }
                }
            }
        });
        addButton(buttonsBar, "editor.button.delete", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                String[] row = (String[]) selection.getFirstElement();
                if (row == null) {
                    return;
                }
                for (QuickFormGpdVariable variableDef : quickForm.getQuickFormGpdVariable()) {
                    if (variableDef.getName().equals(row[NUMBER_NAME_COLUMN])) {
                    	quickForm.getQuickFormGpdVariable().remove(variableDef);
                        setTableInput();
                        setDirty(true);
                        break;
                    }
                }
            }
        });
        addButton(buttonsBar, "editor.button.up", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                String[] row = (String[]) selection.getFirstElement();
                if (row == null) {
                    return;
                }
                for (QuickFormGpdVariable variableDef : quickForm.getQuickFormGpdVariable()) {
                    if (variableDef.getName().equals(row[NUMBER_NAME_COLUMN])) {
                    	int index = quickForm.getQuickFormGpdVariable().indexOf(variableDef);
                    	if(index > 0) {
                    		quickForm.getQuickFormGpdVariable().remove(index);
                    		quickForm.getQuickFormGpdVariable().add(index - 1, variableDef);                    		
                    		setTableInput();
                            setDirty(true);
                            break;
                    	}                        
                    }
                }
            }
        });
        addButton(buttonsBar, "editor.button.down", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                String[] row = (String[]) selection.getFirstElement();
                if (row == null) {
                    return;
                }
                for (QuickFormGpdVariable variableDef : quickForm.getQuickFormGpdVariable()) {
                    if (variableDef.getName().equals(row[NUMBER_NAME_COLUMN])) {
                    	int index = quickForm.getQuickFormGpdVariable().indexOf(variableDef);
                    	if(index < quickForm.getQuickFormGpdVariable().size() - 1) {
                    		quickForm.getQuickFormGpdVariable().remove(index);
                    		quickForm.getQuickFormGpdVariable().add(index + 1, variableDef);                    		
                    		setTableInput();
                            setDirty(true);
                            break;
                    	}                        
                    }
                }
            }
        });
        addButton(buttonsBar, "editor.button.rule", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            }
        }).setEnabled(false);
        addButton(buttonsBar, "editor.button.show", new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	String label = selectTemplateCombo.getText();
            	String filename = null;
            	for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
                    if (label != null && artifact.isEnabled() && label.equalsIgnoreCase(artifact.getLabel()) ) {
                    	filename = artifact.getFileName();
                    	break;
                    }
                }
            	
            	if(filename != null && filename.trim().length() > 0) {
            		Bundle bundle = QuickTemplateRegister.getBundle(filename);
            		String templateHtml = QuickFormXMLUtil.getTemplateFromRegister(bundle, filename);
            		
            		Map<String, Object> variables = new HashMap<String, Object>();
                    variables.put("variables", quickForm.getQuickFormGpdVariable());
                    variables.put("task", "");
                    MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(variables, null);
                    FormHashModelGpdWrap model = new FormHashModelGpdWrap(null, variableProvider, null);
                    
                    String out = FreemarkerProcessorGpdWrap.process(templateHtml, model);
                    
                    variables = new HashMap<String, Object>();
                    variableProvider = new MapDelegableVariableProvider(variables, null);                  
                    
                    for(QuickFormGpdVariable quickFormGpdVariable : quickForm.getQuickFormGpdVariable()) {
                    	String defaultValue = PresentationVariableUtils.getPresentationValue(quickFormGpdVariable.getFormat()); 
                    	Object value = TypeConversionUtil.convertTo(ClassLoaderUtil.loadClass(quickFormGpdVariable.getJavaClassName()), defaultValue);                    	
                    	variables.put(quickFormGpdVariable.getName(), value);
                    	VariableDefinition variableDefinition = new VariableDefinition(false, quickFormGpdVariable.getName(), quickFormGpdVariable.getName());
                    	variableDefinition.setFormat(quickFormGpdVariable.getFormat());
                    	WfVariable wfVariable = new WfVariable(variableDefinition, value);
                    	variableProvider.addVariable(wfVariable);
                    }

                    model = new FormHashModelGpdWrap(null, variableProvider, null);
                    out = FreemarkerProcessorGpdWrap.process(out, model);
                    
            		BrowserWizard wizard = new BrowserWizard(out);
                	CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                    if (dialog.open() == Window.OK) {
                    }
            	}            	
            }
        });
        
        composite.layout(true, true);
    }
	
	private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
        	String[] data = (String[]) element;
        	if(index == 0) {
        		if (MethodTag.hasTag(data[index])) {
                    MethodTag tag = MethodTag.getTagNotNull(data[index]);
                    return tag.name;
            	}
        	}
            
            return data[index];
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
	
	protected Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Messages.getString(buttonKey));
        button.setLayoutData(gridData);
        button.addSelectionListener(selectionListener);
        return button;
    }

	@Override
	public void setFocus() {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (PropertyNames.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }		
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		EditorUtils.closeEditorIfRequired(event, quickFormFile, this);		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {		
	}
	
	private void setTableInput() {
        List<String[]> input = new ArrayList<String[]>(quickForm.getQuickFormGpdVariable().size());
        for (QuickFormGpdVariable variableDef : quickForm.getQuickFormGpdVariable()) {
        	input.add(new String[] { variableDef.getTagName(), variableDef.getName(), variableDef.getFormatLabel(), ""});
        }
        tableViewer.setInput(input);
    }
	
    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }
    
    @Override
    public void dispose() {
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }
}
