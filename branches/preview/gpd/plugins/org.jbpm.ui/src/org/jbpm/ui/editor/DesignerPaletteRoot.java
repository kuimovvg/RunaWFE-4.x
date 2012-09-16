package ru.runa.bpm.ui.editor;

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
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.ElementEntry;
import ru.runa.bpm.ui.common.ElementTypeDefinition;

public class DesignerPaletteRoot extends PaletteRoot {

    private final DesignerEditor editor;

    private final String jpdlVersion;
    private final String notation;

    public DesignerPaletteRoot(DesignerEditor editor) {
        this.editor = editor;
        this.jpdlVersion = editor.getDefinition().getJpdlVersion();
        this.notation = editor.getDefinition().getNotation();
        addControls();
    }

    private void addControls() {
        List<PaletteGroup> categories = new ArrayList<PaletteGroup>();
        categories.add(createDefaultControls());
        for (String name : JpdlVersionRegistry.getPaletteCategories(jpdlVersion)) {
            categories.add(createCategory(name));
        }
        addAll(categories);
    }

    private PaletteGroup createCategory(String categoryName) {
        PaletteGroup controls = new PaletteGroup(categoryName);
        controls.setId(categoryName);
        for (ElementTypeDefinition type : JpdlVersionRegistry.getPaletteEntriesFor(jpdlVersion, categoryName).values()) {
            PaletteEntry entry = createEntry(type);
            if (entry != null) {
                controls.add(entry);
            }
        }
        return controls;
    }

    private PaletteEntry createEntry(ElementTypeDefinition elementType) {
        CreationFactory factory = new GEFElementCreationFactory(elementType.getName(), editor.getDefinition());
        ElementEntry entry = elementType.getEntry();
        PaletteEntry paletteEntry = null;
        if ("node".equals(entry.getType())) {
            paletteEntry = new CreationToolEntry(entry.getLabel(), null, factory, entry.getImageDescriptor(notation), null);
        }
        if ("connection".equals(entry.getType())) {
            paletteEntry = new ConnectionCreationToolEntry(entry.getLabel(), null, factory, entry.getImageDescriptor(notation), null);
        }
        paletteEntry.setId(entry.getId());
        return paletteEntry;
    }

    @SuppressWarnings("unchecked")
    public void refreshActionsVisibility() {
        for (PaletteGroup category : (List<PaletteGroup>) getChildren()) {
            for (PaletteEntry entry : (List<PaletteEntry>) category.getChildren()) {
                if ("ru.runa.bpm.ui.palette.3.Action".equals(entry.getId())) {
                    entry.setVisible(editor.getDefinition().isShowActions());
                    return;
                }
            }
        }
    }

    private PaletteGroup createDefaultControls() {
        PaletteGroup controls = new PaletteGroup("Default Tools");
        controls.setId("ru.runa.bpm.palette.DefaultTools");
        addSelectionTool(controls);
        return controls;
    }

    private void addSelectionTool(PaletteGroup controls) {
        ToolEntry tool = new SelectionToolEntry();
        tool.setId("ru.runa.bpm.ui.palette.Selection");
        controls.add(tool);
        setDefaultEntry(tool);
    }

}
