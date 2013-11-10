package ru.runa.gpd.lang.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.ValidatorParser;

import com.google.common.base.Objects;

public abstract class FormNode extends SwimlanedNode {
    public static final String EMPTY = "";
    public static final String VALIDATION_SUFFIX = "validation.xml";
    public static final String SCRIPT_SUFFIX = "js";
    private String formFileName;
    private String formType;
    private String validationFileName;
    private boolean useJSValidation;
    private String scriptFileName;

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("formExists".equals(name)) {
            return Objects.equal(value, String.valueOf(hasForm()));
        }
        return false;
    }

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
    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new PropertyDescriptor(PROPERTY_FORM_FILE, Localization.getString("FormNode.property.formFile")));
        list.add(new PropertyDescriptor(PROPERTY_FORM_VALIDATION_FILE, Localization.getString("FormNode.property.formValidationFile")));
        list.add(new PropertyDescriptor(PROPERTY_FORM_SCRIPT_FILE, Localization.getString("FormNode.property.formScriptFile")));
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
    public void validate(List<ValidationError> errors) {
        super.validate(errors);
        if (hasFormValidation()) {
            IFile validationFile = IOUtils.getAdjacentFile(getProcessDefinition().getDefinitionFile(), this.validationFileName);
            if (validationFile != null || !validationFile.exists()) {
                errors.add(ValidationError.createLocalizedError(this, "formNode.validationFileNotFound", this.validationFileName));
                return;
            }
        }
        if (hasForm()) {
            FormType formType = FormTypeProvider.getFormType(this.formType);
            IFile formFile = IOUtils.getAdjacentFile(getProcessDefinition().getDefinitionFile(), this.formFileName);
            formType.validate(formFile, this, errors);
        }
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
