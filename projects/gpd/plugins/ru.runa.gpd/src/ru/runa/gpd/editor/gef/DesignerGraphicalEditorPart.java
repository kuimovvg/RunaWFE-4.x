package ru.runa.gpd.editor.gef;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.SelectAllFiguresAction;
import ru.runa.gpd.editor.gef.command.CopyGraphCommand;
import ru.runa.gpd.editor.gef.part.graph.ActionGraphicalEditPart;
import ru.runa.gpd.editor.gef.part.graph.ProcessDefinitionGraphicalEditPart;
import ru.runa.gpd.editor.gef.part.graph.TransitionGraphicalEditPart;
import ru.runa.gpd.lang.model.GraphElement;

public class DesignerGraphicalEditorPart extends GraphicalEditorWithFlyoutPalette {
    private KeyHandler commonKeyHandler;
    private final ProcessEditorBase editor;
    private IStructuredSelection selection;
    private MoveViewportThread moveViewportThread;
    private DesignerPaletteRoot paletteRoot;

    public DesignerGraphicalEditorPart(ProcessEditorBase editor) {
        this.editor = editor;
        DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
        setEditDomain(defaultEditDomain);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createActions() {
        IAction copyAction = new CopyAction(this, Localization.getString("button.copy"));
        copyAction.setId("copy");
        new ActionHandler(copyAction);
        getActionRegistry().registerAction(copyAction);
        getSelectionActions().add(copyAction.getId());
        IAction pasteAction = new PasteAction(this, Localization.getString("button.paste"));
        pasteAction.setId("paste");
        getActionRegistry().registerAction(pasteAction);
        getSelectionActions().add(pasteAction.getId());
        super.createActions();
        getActionRegistry().registerAction(new SelectAllFiguresAction(editor));
        activateHandler(copyAction.getId());
        activateHandler(pasteAction.getId());
        activateHandler(ActionFactory.SELECT_ALL.getId());
        activateHandler(ActionFactory.UNDO.getId());
        activateHandler(ActionFactory.REDO.getId());
        IContextService ctx = (IContextService) getSite().getService(IContextService.class);
        ctx.activateContext("gefEditor.context");
    }

    private void activateHandler(String actionId) {
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
        handlerService.activateHandler(actionId, new ActionHandler(getActionRegistry().getAction(actionId)));
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (editor.equals(getSite().getPage().getActiveEditor())) {
            updateActions(getSelectionActions());
        }
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object selected = structuredSelection.getFirstElement();
            if (!(selected instanceof EditPart)) {
                return;
            }
            this.selection = structuredSelection;
            if (structuredSelection.size() > 1) {
                return;
            }
            EditPart source = (EditPart) selected;
            GraphicalEditPart target = (GraphicalEditPart) getGraphicalViewer().getEditPartRegistry().get(source.getModel());
            if (target != null && target.getFigure().isVisible()) {
                getGraphicalViewer().select(target);
            }
        }
    }

    public void select(GraphElement element) {
        GraphicalEditPart target = (GraphicalEditPart) getGraphicalViewer().getEditPartRegistry().get(element);
        if (target == null || !target.getFigure().isVisible()) {
            editor.getOutlineViewer().select(element);
            return;
        }
        Rectangle targetElementConstraint = element.getConstraint();
        GraphElement parentElement = element.getParent();
        while (targetElementConstraint == null && parentElement != null) {
            targetElementConstraint = parentElement.getConstraint();
            parentElement = parentElement.getParent();
        }
        if (targetElementConstraint != null) {
            Viewport viewport = (Viewport) ((FreeformGraphicalRootEditPart) getGraphicalViewer().getRootEditPart()).getFigure();
            Dimension dim = viewport.getSize();
            Point startLocation = viewport.getViewLocation().getCopy();
            Point preferredEndLocation = new Point(targetElementConstraint.x - dim.width / 2 + targetElementConstraint.width / 2, targetElementConstraint.y - dim.height / 2
                    + targetElementConstraint.height / 2);
            moveViewport(viewport, startLocation, preferredEndLocation);
        }
        getGraphicalViewer().select(target);
    }

    private class MoveViewportThread extends Thread {
        private final Display display;
        private final Viewport viewport;
        private final Point start, end;
        private boolean running = true;

        public MoveViewportThread(Display display, Viewport viewport, Point start, Point end) {
            this.display = display;
            this.viewport = viewport;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            double sumDelta = 0;
            for (int i = 0; i < 20; i++) {
                if (!running) {
                    break;
                }
                double delta = (double) (83 - (i - 10) * (i - 10)) / 1000;
                sumDelta += delta;
                final Point np = viewport.getLocation().getCopy();
                np.x = (int) (start.x + sumDelta * (end.x - start.x));
                np.y = (int) (start.y + sumDelta * (end.y - start.y));
                final int debug = i;
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        moveViewportTo(viewport, np, debug);
                    }
                });
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
        }

        public void cancel() {
            running = false;
        }
    }

    private void moveViewportTo(Viewport viewport, Point p, int debug) {
        viewport.setViewLocation(p);
    }

    private void moveViewport(final Viewport viewport, Point start, Point end) {
        Dimension viewportSize = viewport.getSize();
        int len = (int) Math.sqrt((start.x - end.x) * (start.x - end.x) + (start.y - end.y) * (start.y - end.y));
        if ((viewportSize.height + viewportSize.width) / 4 > len) {
            // simple moving
            viewport.setViewLocation(end);
        } else {
            if (moveViewportThread != null && moveViewportThread.isAlive()) {
                moveViewportThread.cancel();
            }
            moveViewportThread = new MoveViewportThread(Display.getCurrent(), viewport, start, end);
            moveViewportThread.start();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        getSite().setSelectionProvider(editor.getSite().getSelectionProvider());
        getPaletteRoot().refreshActionsVisibility();
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getGraphicalViewer().setContents(editor.getDefinition());
    }

    @Override
    public DesignerPaletteRoot getPaletteRoot() {
        if (paletteRoot == null) {
            paletteRoot = new DesignerPaletteRoot(editor);
        }
        return paletteRoot;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getEditDomain().addViewer(getGraphicalViewer());
        getGraphicalViewer().setRootEditPart(new ScalableFreeformRootEditPart() {
            @Override
            public DragTracker getDragTracker(Request req) {
                MarqueeDragTracker tracker = (MarqueeDragTracker) super.getDragTracker(req);
                tracker.setMarqueeBehavior(MarqueeSelectionTool.BEHAVIOR_NODES_AND_CONNECTIONS);
                return tracker;
            }
        });
        getGraphicalViewer().setEditPartFactory(new EditPartFactory() {
            @Override
            public EditPart createEditPart(EditPart context, Object object) {
                if (!(object instanceof GraphElement)) {
                    return null;
                }
                GraphElement element = (GraphElement) object;
                EditPart editPart = element.getTypeDefinition().createGraphicalEditPart(element);
                return editPart;
            }
        });
        KeyHandler keyHandler = new GraphicalViewerKeyHandler(getGraphicalViewer());
        keyHandler.setParent(getCommonKeyHandler());
        getGraphicalViewer().setKeyHandler(keyHandler);
        getGraphicalViewer().setContextMenu(createContextMenu());
        getSite().setSelectionProvider(getGraphicalViewer());
    }

    protected KeyHandler getCommonKeyHandler() {
        if (commonKeyHandler == null) {
            commonKeyHandler = new KeyHandler();
            commonKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            commonKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return commonKeyHandler;
    }

    private MenuManager createContextMenu() {
        MenuManager menuManager = new EditorContextMenuProvider(getGraphicalViewer());
        getSite().registerContextMenu("ru.runa.gpd.graph.contextmenu", menuManager, getSite().getSelectionProvider());
        return menuManager;
    }

    private class EditorContextMenuProvider extends ContextMenuProvider {
        public EditorContextMenuProvider(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void buildContextMenu(IMenuManager menu) {
            // menu.setRemoveAllWhenShown(true);
            GEFActionConstants.addStandardActionGroups(menu);
            IAction action;
            action = getActionRegistry().getAction("copy");
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction("paste");
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.REDO.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
            }
        }
    }

    @Override
    protected FlyoutPreferences getPalettePreferences() {
        return new PaletteFlyoutPreferences();
    }

    private class PasteAction extends SelectionAction {
        private PasteAction(IWorkbenchPart part, String text) {
            super(part);
            setText(text);
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

    private class CopyAction extends SelectionAction {
        private CopyAction(IWorkbenchPart part, String text) {
            super(part);
            setText(text);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected boolean calculateEnabled() {
            List<EditPart> editParts = getGraphicalViewer().getSelectedEditParts();
            for (EditPart editPart : editParts) {
                if (editPart instanceof ActionGraphicalEditPart) {
                    continue;
                }
                if (editPart instanceof ProcessDefinitionGraphicalEditPart) {
                    continue;
                }
                if (editPart instanceof TransitionGraphicalEditPart) {
                    continue;
                }
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            if (selection != null) {
                Clipboard.getDefault().setContents(new Object[] { editor.getDefinitionFile().getParent(), editor.getDefinition(), selection });
            }
        }
    }
}
