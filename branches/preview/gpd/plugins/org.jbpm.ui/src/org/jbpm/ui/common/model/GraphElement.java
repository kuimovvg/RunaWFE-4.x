package ru.runa.bpm.ui.common.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.ElementEntry;
import ru.runa.bpm.ui.common.ElementTypeDefinition;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.custom.DelegableProvider;
import ru.runa.bpm.ui.properties.DelegableClassPropertyDescriptor;
import ru.runa.bpm.ui.properties.DelegableConfPropertyDescriptor;
import ru.runa.bpm.ui.resource.Messages;

@SuppressWarnings("unchecked")
public abstract class GraphElement implements IPropertySource, NotificationMessages {

    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    private GraphElement parent;

    private final List<GraphElement> childs = new ArrayList<GraphElement>();

    private String typeName;

    private Rectangle constraint;

    public Rectangle getConstraint() {
        return constraint;
    }

    public void postCreate() {

    }

    public void setDirty() {
        ProcessDefinition pd = getProcessDefinition();
        if (pd != null) {
            pd.setDirty(true);
        }
    }

    public void setConstraint(Rectangle newConstraint) {
        Rectangle oldConstraint = this.constraint;
        this.constraint = newConstraint;
        firePropertyChange(NODE_BOUNDS_RESIZED, oldConstraint, newConstraint);
    }

    public ProcessDefinition getProcessDefinition() {
        if (parent == null) {
            return this instanceof ProcessDefinition ? (ProcessDefinition) this : null;
        }
        return parent.getProcessDefinition();
    }

    protected void validate() {
        if (this instanceof Delegable) {
            Delegable d = (Delegable) this;
            DelegableProvider provider = CustomizationRegistry.getProvider(delegationClassName);
            if (delegationClassName == null || delegationClassName.length() == 0) {
                addError("delegationClassName.empty");
            } else if (!CustomizationRegistry.isTypeRegisteredForType(d.getDelegationType(), delegationClassName)) {
                if (!CustomizationRegistry.isTypeRegistered(delegationClassName)) {
                    addWarning("delegationClassName.classNotFound");
                } else {
                    addError("delegationClassName.classCastError");
                }
            } else if (!provider.validateValue(d)) {
                addError("decision.invalidConfiguration");
            }
        }
    }

    public void addError(String messageKey, Object... params) {
        getProcessDefinition().addError(this, messageKey, params);
    }

    public void addWarning(String messageKey, Object... params) {
        getProcessDefinition().addWarning(this, messageKey, params);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public ElementTypeDefinition getTypeDefinition() {
        return JpdlVersionRegistry.getElementTypeDefinition(getProcessDefinition().getJpdlVersion(), typeName);
    }

    public GraphElement getParent() {
        return parent;
    }

    public void setParent(GraphElement parent) {
        this.parent = parent;
    }

    public void removeChild(GraphElement child) {
        childs.remove(child);
        firePropertyChange(NODE_REMOVED, child, null);
        firePropertyChange(NODE_CHILDS_CHANGED, null, null);
    }

    public void addChild(GraphElement child) {
        childs.add(child);
        child.setParent(this);
        firePropertyChange(NODE_CHILDS_CHANGED, null, null);
    }

    public void addChild(GraphElement child, int index) {
        childs.add(index, child);
        child.setParent(this);
        firePropertyChange(NODE_CHILDS_CHANGED, null, null);
    }

    public void swapChilds(GraphElement child1, GraphElement child2) {
        Collections.swap(childs, childs.indexOf(child1), childs.indexOf(child2));
        firePropertyChange(NotificationMessages.NODE_CHILDS_CHANGED, null, null);
    }

    public void changeChildIndex(GraphElement child, GraphElement insertBefore) {
        if (insertBefore != null && child != null) {
            childs.remove(child);
            childs.add(childs.indexOf(insertBefore), child);
            firePropertyChange(NotificationMessages.NODE_CHILDS_CHANGED, null, null);
        }
    }

    public <T extends GraphElement> List<T> getChildren(Class<T> type) {
        List<T> items = new ArrayList<T>();
        for (GraphElement element : childs) {
            if (type.isAssignableFrom(element.getClass())) {
                items.add((T) element);
            }
        }
        return items;
    }

    public <T extends GraphElement> List<T> getChildrenRecursive(Class<T> type) {
        List<T> items = new ArrayList<T>();
        for (GraphElement element : childs) {
            if (type.isAssignableFrom(element.getClass())) {
                items.add((T) element);
            }
            items.addAll(element.getChildrenRecursive(type));
        }
        return items;
    }

    public <T extends GraphElement> T getFirstChild(Class<T> type) {
        for (GraphElement element : childs) {
            if (type.isAssignableFrom(element.getClass())) {
                return (T) element;
            }
        }
        return null;
    }

    // Active implementation
    public void addAction(Action action, int index) {
        if (!(this instanceof Active)) {
            throw new IllegalStateException("It's not Active class ... " + this.getClass());
        }
        if (index == -1) {
            addChild(action);
        } else {
            addChild(action, index);
        }
    }

    public int removeAction(Action action) {
        int index = childs.indexOf(action);
        removeChild(action);
        return index;
    }

    public List<Action> getActions() {
        return getChildren(Action.class);
    }

    // Describable
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange(PROPERTY_DESCRIPTION, old, this.getDescription());
    }

    // Delegable
    private String delegationClassName;

    private String delegationConfiguration = "";

    public String getDelegationClassName() {
        return delegationClassName;
    }

    public void setDelegationClassName(String delegationClassName) {
        String old = getDelegationClassName();
        this.delegationClassName = delegationClassName;
        firePropertyChange(NotificationMessages.PROPERTY_CLASS, old, this.delegationClassName);
    }

    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    public void setDelegationConfiguration(String delegationConfiguration) {
        if (delegationConfiguration == null) {
            delegationConfiguration = "";
        }
        if (!this.delegationConfiguration.equals(delegationConfiguration)) {
            String old = this.delegationConfiguration;
            this.delegationConfiguration = delegationConfiguration;
            firePropertyChange(PROPERTY_CONFIGURATION, old, this.delegationConfiguration);
        }
    }

    // IPropertySource
    protected void firePropertyChange(String propName, Object old, Object newValue) {
        if (!PluginConstants.NON_GUI_THREAD_NAME.equals(Thread.currentThread().getName())) {
            listeners.firePropertyChange(propName, old, newValue);
        }
        if (!PROPERTY_DIRTY.equals(propName)) {
            setDirty();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.removePropertyChangeListener(pcl);
    }

    protected void removeAllPropertyChangeListeners() {
        listeners = new PropertyChangeSupport(this);
    }

    public Object getEditableValue() {
        return this;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public final IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
        if (this instanceof NamedGraphElement) {
            if (((NamedGraphElement) this).canNameBeSetFromProperties()) {
                descriptors.add(new TextPropertyDescriptor(PROPERTY_NAME, Messages.getString("property.name")));
            } else {
                descriptors.add(new PropertyDescriptor(PROPERTY_NAME, Messages.getString("property.name")));
            }
        }
        // if (this instanceof Describable) {
        descriptors.add(new TextPropertyDescriptor(PROPERTY_DESCRIPTION, Messages.getString("property.description")));
        // }
        if (this instanceof Delegable) {
            String type = ((Delegable) this).getDelegationType();
            descriptors.add(new DelegableClassPropertyDescriptor(PROPERTY_CLASS, Messages.getString("property.delegation.class"), type));
            descriptors.add(new DelegableConfPropertyDescriptor(PROPERTY_CONFIGURATION, (Delegable) this, Messages
                    .getString("property.delegation.configuration")));
        }
        descriptors.addAll(getCustomPropertyDescriptors());
        return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
    }

    protected List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        return new ArrayList<IPropertyDescriptor>();
    }

    protected String safeStringValue(String canBeNull) {
        return canBeNull == null ? "" : canBeNull;
    }

    public Object getPropertyValue(Object id) {
        if (PROPERTY_CLASS.equals(id)) {
            return safeStringValue(getDelegationClassName());
        } else if (PROPERTY_CONFIGURATION.equals(id)) {
            return safeStringValue(getDelegationConfiguration());
        } else if (PROPERTY_DESCRIPTION.equals(id)) {
            return safeStringValue(getDescription());
        }
        return null;
    }

    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_CLASS.equals(id)) {
            setDelegationClassName((String) value);
        } else if (PROPERTY_CONFIGURATION.equals(id)) {
            setDelegationConfiguration((String) value);
        } else if (PROPERTY_DESCRIPTION.equals(id)) {
            setDescription((String) value);
        }
    }

    public Image getEntryImage() {
        ElementEntry entry = getTypeDefinition().getEntry();
        if (entry != null) {
            return entry.getImage(getProcessDefinition().getNotation());
        }
        return null;
    }

}
