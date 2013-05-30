package ru.runa.gpd.editor;

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

    public static void createCopyPasteActions(ActionRegistry registry, ProcessEditorBase editor) {
        IAction copyAction = new CopyAction(editor);
        copyAction.setId(ActionFactory.COPY.getId());
        registry.registerAction(copyAction);
        // getSelectionActions().add(copyAction.getId());
        IAction pasteAction = new PasteAction(editor);
        pasteAction.setId(ActionFactory.PASTE.getId());
        registry.registerAction(pasteAction);
        // getSelectionActions().add(pasteAction.getId());
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
    }
    //
    // /**
    // * Adds Actions to the given IMenuManager, which is displayed as the
    // * main-menu of Eclipse. See the corresponding method in the super class.
    // *
    // * @param menubar
    // * the menubar
    // *
    // * @see org.eclipse.ui.part.EditorActionBarContributor
    // */
    // @Override
    // public void contributeToMenu(IMenuManager menubar) {
    // super.contributeToMenu(menubar);
    // IMenuManager editMenu =
    // menubar.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
    //
    // if (editMenu != null) {
    // MenuManager alignments = new
    // MenuManager(Messages.DiagramEditorActionBarContributor_0_xmen);
    // alignments.add(getAction(GEFActionConstants.ALIGN_LEFT));
    // alignments.add(getAction(GEFActionConstants.ALIGN_CENTER));
    // alignments.add(getAction(GEFActionConstants.ALIGN_RIGHT));
    // alignments.add(new Separator());
    // alignments.add(getAction(GEFActionConstants.ALIGN_TOP));
    // alignments.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
    // alignments.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
    // alignments.add(new Separator());
    // alignments.add(getAction(GEFActionConstants.MATCH_WIDTH));
    // alignments.add(getAction(GEFActionConstants.MATCH_HEIGHT));
    // editMenu.insertAfter(ActionFactory.SELECT_ALL.getId(), alignments);
    // }
    //
    // // Create view menu ...
    // MenuManager viewMenu = new
    // MenuManager(Messages.GraphicsActionBarContributor_0_xmen);
    // viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
    // viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
    // // viewMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
    // // viewMenu.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
    //
    // // ... and add it. The position of the view menu differs depending on
    // // which menus exist (see Bugzilla
    // // https://bugs.eclipse.org/bugs/show_bug.cgi?id=381437)
    // if (editMenu != null) {
    // // Edit menu exists --> place view menu directly in front of it
    // menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
    // } else if (menubar.findMenuUsingPath(IWorkbenchActionConstants.M_FILE) !=
    // null) {
    // // File menu exists --> place view menu behind it
    // menubar.insertAfter(IWorkbenchActionConstants.M_FILE, viewMenu);
    // } else {
    // // Add view menu as first entry
    // IContributionItem[] contributionItems = menubar.getItems();
    // if (contributionItems != null && contributionItems.length > 0) {
    // // Any menu exists --> place view menu in front of it it
    // menubar.insertBefore(contributionItems[0].getId(), viewMenu);
    // } else {
    // // No item exists --> simply add view menu
    // menubar.add(viewMenu);
    // }
    // }
    // }

}
