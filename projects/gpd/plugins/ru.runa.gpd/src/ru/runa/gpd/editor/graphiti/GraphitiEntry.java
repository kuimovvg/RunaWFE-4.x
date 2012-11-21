package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeature;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.add.AddNodeFeature;
import ru.runa.gpd.editor.graphiti.add.AddTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
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
        IFeature createFeature = createExecutableExtension("create");
        if (createFeature instanceof CreateNodeFeature) {
            CreateNodeFeature createNodeFeature = (CreateNodeFeature) createFeature;
            createNodeFeature.setFeatureProvider(provider);
            createNodeFeature.setNodeDefinition(nodeTypeDefinition);
        }
        if (createFeature instanceof CreateTransitionFeature) {
            ((CreateTransitionFeature) createFeature).setFeatureProvider(provider);
        }
        return createFeature;
    }

    public IAddFeature createAddFeature(DiagramFeatureProvider provider) {
        IAddFeature addFeature = createExecutableExtension("add");
        if (addFeature instanceof AddNodeFeature) {
            AddNodeFeature addNodeFeature = (AddNodeFeature) addFeature;
            addNodeFeature.setFeatureProvider(provider);
        }
        if (addFeature instanceof AddTransitionFeature) {
            ((AddTransitionFeature) addFeature).setFeatureProvider(provider);
        }
        return addFeature;
    }
}
