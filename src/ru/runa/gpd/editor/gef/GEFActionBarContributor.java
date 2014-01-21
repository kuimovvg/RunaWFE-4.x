package ru.runa.gpd.editor.gef;

import java.util.List;

import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

import ru.runa.gpd.editor.CopyAction;
import ru.runa.gpd.editor.PasteAction;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.ProcessEditorContributor;
import ru.runa.gpd.editor.SelectAllAction;

public class GEFActionBarContributor extends ProcessEditorContributor {

    @Override
    protected KeyHandler createKeyHandler(ActionRegistry registry) {
        KeyHandler keyHandler = super.createKeyHandler(registry);
        keyHandler.put(KeyStroke.getPressed((char) 26, 'z', SWT.CTRL), registry.getAction(ActionFactory.UNDO.getId()));
        keyHandler.put(KeyStroke.getPressed((char) 25, 'y', SWT.CTRL), registry.getAction(ActionFactory.REDO.getId()));
        return keyHandler;
    }

    public static void createCustomGEFActions(ActionRegistry registry, ProcessEditorBase editor, List<String> selectionActionIds) {
        IAction copyAction = new CopyAction(editor);
        copyAction.setId(ActionFactory.COPY.getId());
        registry.registerAction(copyAction);
        selectionActionIds.add(copyAction.getId());
        IAction pasteAction = new PasteAction(editor);
        pasteAction.setId(ActionFactory.PASTE.getId());
        registry.registerAction(pasteAction);
        //        IAction leftAlignmentAction = new AlignmentAction((IWorkbenchPart) editor, PositionConstants.LEFT);
        //        registry.registerAction(leftAlignmentAction);
        //        selectionActionIds.add(leftAlignmentAction.getId());
        //        IAction topAlignmentAction = new AlignmentAction((IWorkbenchPart) editor, PositionConstants.TOP);
        //        registry.registerAction(topAlignmentAction);
        //        selectionActionIds.add(topAlignmentAction.getId());
        SelectAllAction selectAllAction = new SelectAllAction(editor);
        registry.registerAction(selectAllAction);
    }

    @Override
    protected void buildActions() {
        super.buildActions();
        addRetargetAction((RetargetAction) ActionFactory.UNDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.REDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
    }
    
    @Override
    public void contributeToToolBar(IToolBarManager tbm) {
        tbm.add(getAction(ActionFactory.UNDO.getId()));
        tbm.add(getAction(ActionFactory.REDO.getId()));
        tbm.add(new Separator());
        tbm.add(getAction(ActionFactory.COPY.getId()));
        tbm.add(getAction(ActionFactory.PASTE.getId()));
        super.contributeToToolBar(tbm);
    }
    
}
