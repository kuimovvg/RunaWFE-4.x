package ru.runa.gpd.editor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;

import ru.runa.gpd.Localization;

public class PasteAction extends SelectionAction {
    private final ProcessEditorBase editor;
    
    public PasteAction(ProcessEditorBase editor) {
        super(editor);
        this.editor = editor;
        setText(Localization.getString("button.paste"));
    }

    @Override
    public boolean calculateEnabled() {
        return createCommand().canExecute();
    }

    private Command createCommand() {
        return new CopyGraphCommand(editor.getDefinition(), (IFolder) editor.getDefinitionFile().getParent());
    }

    @Override
    public void run() {
        execute(createCommand());
    }
}
