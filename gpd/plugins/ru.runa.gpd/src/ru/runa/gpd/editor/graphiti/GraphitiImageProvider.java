package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.ui.platform.AbstractImageProvider;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class GraphitiImageProvider extends AbstractImageProvider {
    @Override
    protected void addAvailableImages() {
        for (String category : NodeRegistry.getGEFPaletteCategories()) {
            for (NodeTypeDefinition definition : NodeRegistry.getGEFPaletteEntriesFor(category).values()) {
                addImageFilePath(definition.getName(), "icons/bpmn/palette/" + definition.getName());
            }
        }
    }
}
