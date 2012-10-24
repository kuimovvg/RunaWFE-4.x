package org.jbpm.ui.common.part.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.NamedGraphElement;
import org.jbpm.ui.common.model.NotificationMessages;

public abstract class ElementTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener, NotificationMessages {

    @Override
    public GraphElement getModel() {
        return (GraphElement) super.getModel();
    }

    @Override
    public void activate() {
        if (!isActive()) {
            getModel().addPropertyChangeListener(this);
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            getModel().removePropertyChangeListener(this);
            super.deactivate();
        }
    }

    @Override
    protected void refreshVisuals() {
        if (getModel() instanceof NamedGraphElement) {
            setWidgetText(((NamedGraphElement) getModel()).getName());
        }
        setWidgetImage(getModel().getEntryImage());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (NODE_CHILDS_CHANGED.equals(messageId)) {
            refreshChildren();
        } else if (PROPERTY_NAME.equals(messageId)) {
            refreshVisuals();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key) {
        if (GraphElement.class.isAssignableFrom(key)) {
            GraphElement element = getModel();
            if (key.isAssignableFrom(element.getClass())) {
                return element;
            }
        }
        return super.getAdapter(key);
    }

}
