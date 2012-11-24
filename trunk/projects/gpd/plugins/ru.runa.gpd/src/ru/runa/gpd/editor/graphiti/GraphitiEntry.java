package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IUpdateFeature;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.add.AddNodeFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateNodeFeature;
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
        if (feature instanceof CreateNodeFeature) {
            CreateNodeFeature createNodeFeature = (CreateNodeFeature) feature;
            createNodeFeature.setFeatureProvider(provider);
            createNodeFeature.setNodeDefinition(nodeTypeDefinition);
        }
        if (feature instanceof CreateTransitionFeature) {
            ((CreateTransitionFeature) feature).setFeatureProvider(provider);
        }
        return feature;
    }

    public IAddFeature createAddFeature(DiagramFeatureProvider provider) {
        IAddFeature feature = createExecutableExtension("add");
        if (feature instanceof AddNodeFeature) {
            AddNodeFeature addNodeFeature = (AddNodeFeature) feature;
            addNodeFeature.setFeatureProvider(provider);
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
