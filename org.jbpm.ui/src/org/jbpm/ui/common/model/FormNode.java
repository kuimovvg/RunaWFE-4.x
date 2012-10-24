package org.jbpm.ui.common.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.forms.FormType;
import org.jbpm.ui.forms.FormTypeProvider;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.IOUtils;
import org.jbpm.ui.validation.ValidatorParser;

public abstract class FormNode extends SwimlanedNode {
    public static final String EMPTY = "";
    public static final String VALIDATION_SUFFIX = "validation.xml";
    public static final String SCRIPT_SUFFIX = "js";

    private String formFileName;

    private String formType;

    private String validationFileName;

    private boolean useJSValidation;

    private String scriptFileName;

    public String getFormType() {
        return formType;
    }

    public void setFormType(String type) {
        this.formType = type;
    }

    public boolean hasForm() {
        return formFileName != null && formFileName.length() > 0;
    }
    public boolean hasFormValidation() {
        return validationFileName != null && validationFileName.length() > 0;
    }
    public boolean hasFormScript() {
        return scriptFileName != null && scriptFileName.length() > 0;
    }

    public String getFormFileName() {
        return formFileName;
    }

    public void setFormFileName(String formFile) {
        String old = this.formFileName;
        this.formFileName = formFile.trim();
        firePropertyChange(PROPERTY_FORM_FILE, old, this.formFileName);
    }

    public String getValidationFileName() {
        return validationFileName;
    }

    public void setValidationFileName(String validationFile) {
        String old = this.validationFileName;
        this.validationFileName = validationFile.trim();
        firePropertyChange(PROPERTY_FORM_VALIDATION_FILE, old, this.validationFileName);
    }

    public boolean isUseJSValidation() {
        return useJSValidation;
    }

    public void setUseJSValidation(boolean useJSValidation) {
        boolean old = this.useJSValidation;
        this.useJSValidation = useJSValidation;
        firePropertyChange(PROPERTY_FORM_JS_VALIDATION, old, this.useJSValidation);
    }
    
    public String getScriptFileName() {
        return scriptFileName;
    }
    
    public void setScriptFileName(String scriptFile) {
        String old = this.scriptFileName;
        this.scriptFileName = scriptFile;
        firePropertyChange(PROPERTY_FORM_SCRIPT_FILE, old, this.scriptFileName);
    }
    
    @Override
    public boolean canSetNameTo(String name) {
        if (!super.canSetNameTo(name)) {
            return false;
        }
        return name != null && name.trim().length() > 0;
    }
    
    @Override
    public void setName(String name) {
        if (getName() != null && canSetNameTo(name)) {
            try {
                IOUtils.renameFormFiles(this, name);
            } catch (CoreException e) {
                DesignerLogger.logErrorWithoutDialog("Unable rename form files", e);
            }
        }
        super.setName(name);
    }

    @Override
    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new PropertyDescriptor(PROPERTY_FORM_FILE, Messages.getString("FormNode.property.formFile")));
        list.add(new PropertyDescriptor(PROPERTY_FORM_VALIDATION_FILE, Messages.getString("FormNode.property.formValidationFile")));
        list.add(new PropertyDescriptor(PROPERTY_FORM_SCRIPT_FILE, Messages.getString("FormNode.property.formScriptFile")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_FORM_FILE.equals(id)) {
            return safeStringValue(getFormFileName());
        }
        if (PROPERTY_FORM_VALIDATION_FILE.equals(id)) {
            return safeStringValue(getValidationFileName());
        }
        if (PROPERTY_FORM_SCRIPT_FILE.equals(id)) {
            return safeStringValue(getScriptFileName());
        }
        return super.getPropertyValue(id);
    }

    @Override
    protected void validate() {
        super.validate();
        if (!hasForm()) {
            // We do not have a form, don't check validation file
            return;
        }
        if (!hasFormValidation()) {
            addError("formNode.validationFileNotExist");
            return;
        }
        IFile validationFile = IOUtils.getAdjacentFile(getProcessDefinition().getDefinitionFile(), this.validationFileName);
        if (!validationFile.exists()) {
            addError("formNode.validationFileNotFound");
            return;
        }
        FormType formType = FormTypeProvider.getFormType(this.formType);
        IFile formFile = IOUtils.getAdjacentFile(getProcessDefinition().getDefinitionFile(), this.formFileName);
        formType.validate(formFile, this);
    }
    
    public Set<String> getValidationVariables(IFolder processFolder) throws Exception {
        if (!hasFormValidation()) {
            return new HashSet<String>();
        }
        IFile validationFile = IOUtils.getAdjacentFile(processFolder, this.validationFileName);
        return ValidatorParser.parseValidatorConfigs(validationFile).keySet();
    }
    
    public Map<String, Integer> getFormVariables(IFolder definitionFile) throws Exception {
        if (!hasForm()) {
            return new HashMap<String, Integer>();
        }
        FormType formType = FormTypeProvider.getFormType(this.formType);
        IFile formFile = IOUtils.getAdjacentFile(definitionFile, this.formFileName);
        return formType.getFormVariableNames(formFile, this);
    }

    @Override
    public boolean isExclusive() {
        return true;
    }
}
