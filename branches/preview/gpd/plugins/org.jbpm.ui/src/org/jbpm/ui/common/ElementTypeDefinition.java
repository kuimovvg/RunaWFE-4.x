package ru.runa.bpm.ui.common;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.ElementSerializer;
import ru.runa.bpm.ui.common.figure.GridSupportLayer;
import ru.runa.bpm.ui.common.figure.NodeFigure;
import ru.runa.bpm.ui.common.figure.TransitionFigure;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.dialog.ErrorDialog;
import ru.runa.bpm.ui.editor.ltk.VariableRenameProvider;
import org.osgi.framework.Bundle;

public class ElementTypeDefinition implements IElementTypeDefinition {
    private final String name;
    private final IConfigurationElement configElement;
    private ElementSerializer serializer;
    private final ElementEntry entry;

    public ElementTypeDefinition(IConfigurationElement configElement) throws CoreException {
        this.configElement = configElement;
        this.name = configElement.getAttribute("name");
        IConfigurationElement[] serializers = configElement.getChildren("elementSerializer");
        if (serializers.length > 0) {
            this.serializer = (ElementSerializer) serializers[0].createExecutableExtension("class");
        }
        IConfigurationElement[] entries = configElement.getChildren("entry");
        if (entries.length > 0) {
            entry = new ElementEntry(entries[0]);
        } else {
            entry = null;
        }
    }

    public ElementSerializer getSerializer() {
        return serializer;
    }

    public boolean hasVariableRenameProvider() {
        return getVariableRenameConfigurationElement() != null;
    }

    private IConfigurationElement getVariableRenameConfigurationElement() {
        IConfigurationElement[] renameProviders = configElement.getChildren("onVariableRenamed");
        if (renameProviders.length > 0) {
            return renameProviders[0];
        }
        return null;
    }

    public VariableRenameProvider<?> createVariableRenameProvider() {
        try {
            return (VariableRenameProvider<?>) getVariableRenameConfigurationElement().createExecutableExtension("provider");
        } catch (CoreException e) {
            ErrorDialog.open(e);
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public ElementEntry getEntry() {
        return entry;
    }

    public String getEntryLabel() {
        if (entry != null) {
            return entry.getLabel();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private <T> T createExecutableExtension(String propertyName) {
        try {
            if (configElement.getAttribute(propertyName) == null) {
                return null;
            }
            return (T) configElement.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            DesignerLogger.logError("Unable to create element '" + name + "'(unable to load property='" + propertyName + "')", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends GraphElement> T createElement() {
        GraphElement element = createExecutableExtension("model");
        element.setTypeName(name);
        return (T) element;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends GraphElement> getModelClass() {
        try {
            Bundle bundle = Platform.getBundle(configElement.getDeclaringExtension().getNamespaceIdentifier());
            return bundle.loadClass(configElement.getAttribute("model"));
        } catch (Exception e) {
            ErrorDialog.open(e);
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
