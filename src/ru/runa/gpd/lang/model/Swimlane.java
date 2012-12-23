package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.handler.HandlerArtifact;
import ru.runa.gpd.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.orgfunction.OrgFunctionsRegistry;
import ru.runa.wfe.handler.assign.DefaultAssignmentHandler;

public class Swimlane extends NamedGraphElement implements Delegable {
    private static final String DELEGATION_CLASS_NAME = DefaultAssignmentHandler.class.getName();
    private boolean publicVisibility;

    public Swimlane() {
        setDelegationClassName(DELEGATION_CLASS_NAME);
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ASSIGNMENT;
    }

    @Override
    public void setName(String name) {
        if (getProcessDefinition().getSwimlaneByName(name) != null) {
            return;
        }
        super.setName(name);
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(boolean publicVisibility) {
        boolean old = this.publicVisibility;
        this.publicVisibility = publicVisibility;
        firePropertyChange(PROPERTY_PUBLIC_VISIBILITY, old, this.publicVisibility);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor>();
        list.add(new PropertyDescriptor(PROPERTY_PUBLIC_VISIBILITY, Localization.getString("Variable.property.publicVisibility")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_PUBLIC_VISIBILITY.equals(id)) {
            return publicVisibility ? Localization.getString("message.yes") : Localization.getString("message.no");
        }
        return super.getPropertyValue(id);
    }

    @Override
    protected void validate() {
        super.validate();
        try {
            OrgFunctionDefinition definition = OrgFunctionsRegistry.parseSwimlaneConfiguration(getDelegationConfiguration());
            if (definition != null) {
                List<String> errors = definition.getErrors(getProcessDefinition());
                for (String errorKey : errors) {
                    addError(errorKey);
                }
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith(OrgFunctionDefinition.MISSED_DEFINITION)) {
                addWarning("orgfunction.missed");
            } else {
                addError("orgfunction.broken");
            }
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/swimlane.gif");
    }
}
