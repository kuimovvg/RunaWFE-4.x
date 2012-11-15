package ru.runa.gpd.lang;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.osgi.framework.Bundle;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.figure.GridSupportLayer;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ltk.VariableRenameProvider;
import ru.runa.gpd.ui.dialog.ErrorDialog;

@SuppressWarnings("unchecked")
public class NodeTypeDefinition {
    private final String name;
    private final String label;
    private final IConfigurationElement configElement;
    private final IConfigurationElement gefEntry;
    private final GEFPaletteEntry paletteEntry;

    public NodeTypeDefinition(IConfigurationElement configElement) throws CoreException {
        this.configElement = configElement;
        this.name = configElement.getAttribute("name");
        this.label = configElement.getAttribute("label");
        IConfigurationElement[] entries = configElement.getChildren("gefEntry");
        if (entries.length > 0) {
            gefEntry = entries[0];
            IConfigurationElement[] paletteEntries = gefEntry.getChildren("gefPaletteEntry");
            if (paletteEntries.length > 0) {
                paletteEntry = new GEFPaletteEntry(paletteEntries[0]);
            } else {
                paletteEntry = null;
            }
        } else {
            gefEntry = null;
            paletteEntry = null;
        }
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

    public String getLabel() {
        return label;
    }

    public GEFPaletteEntry getGEFPaletteEntry() {
        return paletteEntry;
    }

    private <T> T createExecutableExtension(IConfigurationElement element, String propertyName) {
        try {
            if (element == null || element.getAttribute(propertyName) == null) {
                return null;
            }
            return (T) element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            PluginLogger.logError("Unable to create element '" + name + "'(unable to load property='" + propertyName + "')", e);
            return null;
        }
    }

    public <T extends GraphElement> T createElement() {
        GraphElement element = createExecutableExtension(configElement, "model");
        element.setTypeName(name);
        return (T) element;
    }

    public Class<? extends GraphElement> getModelClass() {
        try {
            Bundle bundle = Platform.getBundle(configElement.getDeclaringExtension().getNamespaceIdentifier());
            return (Class<? extends GraphElement>) bundle.loadClass(configElement.getAttribute("model"));
        } catch (Exception e) {
            ErrorDialog.open(e);
            return null;
        }
    }

    private EditPart createEditPart(String propertyName, GraphElement element) {
        EditPart editPart = createExecutableExtension(gefEntry, propertyName);
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
        T figure = createExecutableExtension(gefEntry, "figure");
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
