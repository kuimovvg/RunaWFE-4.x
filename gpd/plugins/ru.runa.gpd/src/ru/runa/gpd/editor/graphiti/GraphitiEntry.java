package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IUpdateFeature;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.add.AddGraphElementFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateGraphElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.editor.graphiti.update.UpdateFeature;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class GraphitiEntry {
    private final NodeTypeDefinition nodeTypeDefinition;
    private final IConfigurationElement element;

    public GraphitiEntry(NodeTypeDefinition nodeTypeDefinition, IConfigurationElement element) {
        this.nodeTypeDefinition = nodeTypeDefinition;
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

    public IFeature createCreateFeature(DiagramFeatureProvider provider) {
        IFeature feature = createExecutableExtension("create");
        if (feature instanceof CreateGraphElementFeature) {
            CreateGraphElementFeature createGraphElementFeature = (CreateGraphElementFeature) feature;
            createGraphElementFeature.setFeatureProvider(provider);
            createGraphElementFeature.setNodeDefinition(nodeTypeDefinition);
        }
        if (feature instanceof CreateTransitionFeature) {
            ((CreateTransitionFeature) feature).setFeatureProvider(provider);
        }
        return feature;
    }

    public IAddFeature createAddFeature(DiagramFeatureProvider provider) {
        IAddFeature feature = createExecutableExtension("add");
        if (feature instanceof AddGraphElementFeature) {
            ((AddGraphElementFeature) feature).setFeatureProvider(provider);
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
}
