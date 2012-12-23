package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IUpdateFeature;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.add.AddElementFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.editor.graphiti.layout.ElementLayoutFeature;
import ru.runa.gpd.editor.graphiti.update.UpdateFeature;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class GraphitiEntry {
    private final NodeTypeDefinition nodeTypeDefinition;
    private final IConfigurationElement element;
    private final Dimension defaultSize;
    private final boolean fixedSize;

    public GraphitiEntry(NodeTypeDefinition nodeTypeDefinition, IConfigurationElement element) {
        this.nodeTypeDefinition = nodeTypeDefinition;
        this.element = element;
        this.defaultSize = new Dimension(GEFConstants.GRID_SIZE * Integer.parseInt(element.getAttribute("width")), GEFConstants.GRID_SIZE
                * Integer.parseInt(element.getAttribute("height")));
        this.fixedSize = Boolean.parseBoolean(element.getAttribute("fixedSize"));
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

    public IFeature createCreateFeature(DiagramFeatureProvider provider) {
        IFeature feature = createExecutableExtension("create");
        if (feature instanceof CreateElementFeature) {
            CreateElementFeature createElementFeature = (CreateElementFeature) feature;
            createElementFeature.setFeatureProvider(provider);
            createElementFeature.setNodeDefinition(nodeTypeDefinition);
        }
        if (feature instanceof CreateTransitionFeature) {
            ((CreateTransitionFeature) feature).setFeatureProvider(provider);
        }
        return feature;
    }

    public IAddFeature createAddFeature(DiagramFeatureProvider provider) {
        IAddFeature feature = createExecutableExtension("add");
        if (feature instanceof AddElementFeature) {
            ((AddElementFeature) feature).setFeatureProvider(provider);
        }
        if (feature instanceof AddTransitionFeature) {
            ((AddTransitionFeature) feature).setFeatureProvider(provider);
        }
        return feature;
    }

    public IUpdateFeature createUpdateFeature(DiagramFeatureProvider provider) {
        IUpdateFeature feature = createExecutableExtension("update");
        if (feature instanceof UpdateFeature) {
            UpdateFeature updateFeature = (UpdateFeature) feature;
            updateFeature.setFeatureProvider(provider);
        }
        return feature;
    }

    public ILayoutFeature createLayoutFeature(DiagramFeatureProvider provider) {
        ILayoutFeature feature = createExecutableExtension("layout");
        if (feature instanceof ElementLayoutFeature) {
            ((ElementLayoutFeature) feature).setFeatureProvider(provider);
        }
        return feature;
    }

    public Dimension getDefaultSize() {
        return defaultSize.getCopy();
    }

    public boolean isFixedSize() {
        return fixedSize;
    }
}
