package ru.runa.gpd.editor.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.GEFPaletteEntry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class DesignerPaletteRoot extends PaletteRoot {
    private final ProcessEditorBase editor;

    public DesignerPaletteRoot(ProcessEditorBase editor) {
        this.editor = editor;
        addControls();
    }

    private void addControls() {
        List<PaletteGroup> categories = new ArrayList<PaletteGroup>();
        categories.add(createDefaultControls());
        for (String name : NodeRegistry.getGEFPaletteCategories()) {
            categories.add(createCategory(name));
        }
        addAll(categories);
    }

    private PaletteGroup createCategory(String categoryName) {
        PaletteGroup controls = new PaletteGroup(categoryName);
        controls.setId(categoryName);
        for (NodeTypeDefinition type : NodeRegistry.getGEFPaletteEntriesFor(categoryName).values()) {
            PaletteEntry entry = createEntry(type);
            if (entry != null) {
                controls.add(entry);
            }
        }
        return controls;
    }

    private PaletteEntry createEntry(NodeTypeDefinition elementType) {
        CreationFactory factory = new GEFElementCreationFactory(elementType, editor.getDefinition());
        GEFPaletteEntry entry = elementType.getGEFPaletteEntry();
        PaletteEntry paletteEntry = null;
        if ("node".equals(entry.getType())) {
            paletteEntry = new CreationToolEntry(elementType.getLabel(), null, factory, entry.getImageDescriptor(Language.JPDL.getNotation()), null);
        }
        if ("connection".equals(entry.getType())) {
            paletteEntry = new ConnectionCreationToolEntry(elementType.getLabel(), null, factory, entry.getImageDescriptor(Language.JPDL.getNotation()), null);
        }
        paletteEntry.setId(entry.getId());
        return paletteEntry;
    }

    @SuppressWarnings("unchecked")
    public void refreshActionsVisibility() {
        for (PaletteGroup category : (List<PaletteGroup>) getChildren()) {
            for (PaletteEntry entry : (List<PaletteEntry>) category.getChildren()) {
                if ("3.Action".equals(entry.getId())) {
                    entry.setVisible(editor.getDefinition().isShowActions());
                    return;
                }
            }
        }
    }

    private PaletteGroup createDefaultControls() {
        PaletteGroup controls = new PaletteGroup("Default Tools");
        controls.setId("DefaultTools");
        addSelectionTool(controls);
        return controls;
    }

    private void addSelectionTool(PaletteGroup controls) {
        ToolEntry tool = new SelectionToolEntry();
        tool.setId("Selection");
        controls.add(tool);
        setDefaultEntry(tool);
    }
}
