package ru.runa.gpd.editor.gef;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.figure.GridSupportLayer;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GefEntry {
    private final IConfigurationElement element;

    public GefEntry(IConfigurationElement element) {
        this.element = element;
    }

    private <T> T createExecutableExtension(String propertyName) {
        try {
            if (element == null || element.getAttribute(propertyName) == null) {
                return null;
            }
            return (T) element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            PluginLogger.logError("Unable to create element '" + this + "'(unable to load property='" + propertyName + "')", e);
            return null;
        }
    }

    private EditPart createEditPart(String propertyName, GraphElement element) {
        EditPart editPart = createExecutableExtension(propertyName);
        if (editPart != null) {
            editPart.setModel(element);
        }
        return editPart;
    }

    public EditPart createGraphicalEditPart(GraphElement element) {
        return createEditPart("graphicalEditPart", element);
    }

    public EditPart createTreeEditPart(GraphElement element) {
        return createEditPart("treeEditPart", element);
    }

    public <T extends IFigure> T createFigure(ProcessDefinition definition) {
        T figure = createExecutableExtension("figure");
        if (figure instanceof NodeFigure) {
            ((NodeFigure) figure).init(definition.isBPMNNotation());
        }
        if (figure instanceof TransitionFigure) {
            ((TransitionFigure) figure).init(definition.isBPMNNotation());
        }
        if (figure instanceof GridSupportLayer) {
            ((GridSupportLayer) figure).setDefinition(definition);
        }
        return figure;
    }
}
