package ru.runa.gpd.editor;

import java.util.List;

import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

public class GraphicalEditorContributor extends ActionBarContributor {
    public static KeyHandler createKeyHandler(ActionRegistry registry) {
        KeyHandler keyHandler = new KeyHandler();
        keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), registry.getAction(ActionFactory.DELETE.getId()));
        keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), registry.getAction(GEFActionConstants.DIRECT_EDIT));
        keyHandler.put(KeyStroke.getPressed((char) 1, 'a', SWT.CTRL), registry.getAction(ActionFactory.SELECT_ALL.getId()));
        keyHandler.put(KeyStroke.getPressed((char) 3, 'c', SWT.CTRL), registry.getAction(ActionFactory.COPY.getId()));
        keyHandler.put(KeyStroke.getPressed((char) 22, 'v', SWT.CTRL), registry.getAction(ActionFactory.PASTE.getId()));
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

    /**
     * Creates and initialises all Actions. See the corresponding method in the
     * super class.
     * 
     * @see org.eclipse.gef.ui.actions.ActionBarContributor
     */
    @Override
    protected void buildActions() {
        addRetargetAction((RetargetAction) ActionFactory.UNDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.REDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.SELECT_ALL.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.DELETE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.COPY.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.PASTE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction((RetargetAction) ActionFactory.PRINT.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        addRetargetAction(new ZoomInRetargetAction());
        addRetargetAction(new ZoomOutRetargetAction());
        //        addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
        //        addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
    }

    /**
     * Global action keys are already declared with
     * {@link #addRetargetAction(RetargetAction)}. See the corresponding method
     * in the super class.
     * 
     * @see org.eclipse.gef.ui.actions.ActionBarContributor
     */
    @Override
    protected void declareGlobalActionKeys() {
    }

    /**
     * Adds Actions to the given IToolBarManager, which is displayed above the
     * editor. See the corresponding method in the super class.
     * 
     * @param tbm
     *            the {@link IToolBarManager}
     * 
     * @see org.eclipse.ui.part.EditorActionBarContributor
     */
    @Override
    public void contributeToToolBar(IToolBarManager tbm) {
        tbm.add(getAction(ActionFactory.UNDO.getId()));
        tbm.add(getAction(ActionFactory.REDO.getId()));
        tbm.add(new Separator());
        tbm.add(getAction(ActionFactory.COPY.getId()));
        tbm.add(getAction(ActionFactory.PASTE.getId()));
        //        tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
        //        tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
    }
}
