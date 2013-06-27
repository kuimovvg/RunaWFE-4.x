package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.property.FormatClassPropertyDescriptor;
import ru.runa.gpd.util.VariableUtils;

public class Variable extends NamedGraphElement {
    private String scriptingName;
    private String formatClassName;
    private boolean publicVisibility;
    private String defaultValue;

    protected Variable(String format, boolean publicVisibility, String defaultValue) {
        this(null, format, publicVisibility, defaultValue);
    }

    public Variable(String name, String format, boolean publicVisibility, String defaultValue) {
        super(name);
        setFormatClassName(format);
        this.publicVisibility = publicVisibility;
        this.defaultValue = defaultValue;
    }

    public Variable(Variable variable) {
        this(variable.getName(), variable.getFormatClassName(), variable.isPublicVisibility(), variable.getDefaultValue());
        setScriptingName(variable.getScriptingName());
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public void setScriptingName(String nameForScripting) {
        this.scriptingName = nameForScripting;
    }

    @Override
    public void setName(String name) {
        if (name.trim().length() == 0 || getProcessDefinition().getVariableNames(true).contains(name)) {
            return;
        }
        super.setName(name);
        setScriptingName(VariableUtils.generateNameForScripting(getProcessDefinition(), name, null));
    }

    public String getFormatClassName() {
        return formatClassName;
    }

    public String getJavaClassName() {
        return VariableFormatRegistry.getInstance().getArtifactNotNull(formatClassName).getJavaClassName();
    }

    public void setFormatClassName(String formatClassName) {
        String old = this.formatClassName;
        this.formatClassName = formatClassName;
        firePropertyChange(PROPERTY_FORMAT, old, this.formatClassName);
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(boolean publicVisibility) {
        boolean old = this.publicVisibility;
        this.publicVisibility = publicVisibility;
        firePropertyChange(PROPERTY_PUBLIC_VISIBILITY, old, this.publicVisibility);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        String old = this.defaultValue;
        this.defaultValue = defaultValue;
        firePropertyChange(PROPERTY_DEFAULT_VALUE, old, this.defaultValue);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
        list.add(new FormatClassPropertyDescriptor(PROPERTY_FORMAT, Localization.getString("Variable.property.format"), this));
        list.add(new PropertyDescriptor(PROPERTY_PUBLIC_VISIBILITY, Localization.getString("Variable.property.publicVisibility")));
        list.add(new PropertyDescriptor(PROPERTY_DEFAULT_VALUE, Localization.getString("Variable.property.defaultValue")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_FORMAT.equals(id)) {
            return formatClassName == null ? "" : formatClassName;
        }
        if (PROPERTY_PUBLIC_VISIBILITY.equals(id)) {
            return publicVisibility ? Localization.getString("message.yes") : Localization.getString("message.no");
        }
        if (PROPERTY_DEFAULT_VALUE.equals(id)) {
            return defaultValue == null ? "" : defaultValue;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_FORMAT.equals(id)) {
            setFormatClassName((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/variable.gif");
    }
}
