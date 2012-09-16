package ru.runa.bpm.ui.common.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.bpm.ui.SharedImages;
import ru.runa.bpm.ui.properties.FormatClassPropertyDescriptor;
import ru.runa.bpm.ui.resource.Messages;

import ru.runa.wf.web.forms.format.StringFormat;

public class Variable extends NamedGraphElement {

    private String format;
    private boolean publicVisibility;
    private String defaultValue;

    public Variable(String name, String format, boolean publicVisibility, String defaultValue) {
        super(name);
        this.format = format;
        this.publicVisibility = publicVisibility;
        this.defaultValue = defaultValue;
    }

    public Variable(Variable variable) {
        this(variable.getName(), variable.getFormat(), variable.isPublicVisibility(), variable.getDefaultValue());
    }

    public Variable(String name) {
        this(name, StringFormat.class.getName(), false, null);
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    @Override
    protected boolean canSetNameTo(String name) {
        if (name.trim().length() == 0) {
            return false;
        }
        return !getProcessDefinition().getVariableNames(true).contains(name);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        String old = this.format;
        this.format = format;
        firePropertyChange(PROPERTY_FORMAT, old, this.format);
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
        list.add(new FormatClassPropertyDescriptor(PROPERTY_FORMAT, Messages.getString("Variable.property.format"), this));
        list.add(new PropertyDescriptor(PROPERTY_PUBLIC_VISIBILITY, Messages.getString("Variable.property.publicVisibility")));
        list.add(new PropertyDescriptor(PROPERTY_DEFAULT_VALUE, Messages.getString("Variable.property.defaultValue")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_FORMAT.equals(id)) {
            return format == null ? "" : format;
        }
        if (PROPERTY_PUBLIC_VISIBILITY.equals(id)) {
            return publicVisibility ? Messages.getString("message.yes") : Messages.getString("message.no");
        }
        if (PROPERTY_DEFAULT_VALUE.equals(id)) {
            return defaultValue == null ? "" : defaultValue;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_FORMAT.equals(id)) {
            setFormat((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/variable.gif");
    }
}
