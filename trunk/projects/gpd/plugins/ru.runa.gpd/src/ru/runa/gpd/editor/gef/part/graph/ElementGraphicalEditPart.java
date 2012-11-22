package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.PropertyNames;

public abstract class ElementGraphicalEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener, PropertyNames {
    private static final Border TOOL_TIP_BORDER = new MarginBorder(0, 2, 0, 2);

    @Override
    public GraphElement getModel() {
        return (GraphElement) super.getModel();
    }

    @Override
    protected IFigure createFigure() {
        return getModel().getTypeDefinition().getGefEntry().createFigure(getModel().getProcessDefinition());
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

    protected String getTooltipMessage() {
        return null;
    }

    protected void updateTooltip(IFigure figure) {
        String tooltipMessage = getTooltipMessage();
        if (tooltipMessage == null || tooltipMessage.length() == 0) {
            figure.setToolTip(null);
            return;
        }
        if (figure.getToolTip() == null) {
            Label tooltip = new Label();
            tooltip.setBorder(TOOL_TIP_BORDER);
            figure.setToolTip(tooltip);
        }
        ((Label) figure.getToolTip()).setText(tooltipMessage);
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

    public String getAssociatedViewId() {
        return null; // PropertiesView.ID;
    }
}
