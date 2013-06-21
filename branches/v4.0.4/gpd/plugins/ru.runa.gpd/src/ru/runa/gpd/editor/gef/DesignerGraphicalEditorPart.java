package ru.runa.gpd.editor.gef;

import java.util.List;

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
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ObjectActionContributorManager;

import ru.runa.gpd.editor.GraphicalEditorContributor;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.StructuredSelectionProvider;
import ru.runa.gpd.lang.model.GraphElement;

@SuppressWarnings({ "unchecked", "restriction" })
public class DesignerGraphicalEditorPart extends GraphicalEditorWithFlyoutPalette {
    private final ProcessEditorBase editor;
    private MoveViewportThread moveViewportThread;
    private final DesignerPaletteRoot paletteRoot;

    public DesignerGraphicalEditorPart(ProcessEditorBase editor) {
        this.editor = editor;
        this.paletteRoot = new DesignerPaletteRoot(editor);
        setEditDomain(new DefaultEditDomain(this));
    }

    public ProcessEditorBase getEditor() {
        return editor;
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
                tracker.setMarqueeBehavior(MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS);
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
                GefEntry gefEntry = element.getTypeDefinition().getGefEntry();
                if (gefEntry != null) {
                    return gefEntry.createGraphicalEditPart(element);
                }
                throw new RuntimeException("No graph part defined for " + element);
            }
        });
        KeyHandler keyHandler = new GraphicalViewerKeyHandler(getGraphicalViewer());
        keyHandler.setParent(GraphicalEditorContributor.createKeyHandler(getActionRegistry()));
        getGraphicalViewer().setKeyHandler(keyHandler);
        getGraphicalViewer().setContextMenu(createContextMenu());
        getSite().setSelectionProvider(getGraphicalViewer());
    }

    private MenuManager createContextMenu() {
        MenuManager menuManager = new EditorContextMenuProvider(getGraphicalViewer());
        getSite().registerContextMenu("ru.runa.gpd.graph.contextmenu", menuManager, getSite().getSelectionProvider());
        return menuManager;
    }

    @Override
    protected void createActions() {
        super.createActions();
        GraphicalEditorContributor.createCopyPasteActions(getActionRegistry(), editor);
    }

//    @Override
//    protected void createActions() {
//        IAction copyAction = new CopyAction(this, editor);
//        copyAction.setId(ActionFactory.COPY.getId());
//        new ActionHandler(copyAction);
//        getActionRegistry().registerAction(copyAction);
//        getSelectionActions().add(copyAction.getId());
//        IAction pasteAction = new PasteAction(this, editor);
//        pasteAction.setId(ActionFactory.PASTE.getId());
//        getActionRegistry().registerAction(pasteAction);
//        getSelectionActions().add(pasteAction.getId());
//        super.createActions();
//    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (editor.equals(getSite().getPage().getActiveEditor())) {
            updateActions(getSelectionActions());
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

    private class EditorContextMenuProvider extends ContextMenuProvider {
        public EditorContextMenuProvider(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void buildContextMenu(IMenuManager menu) {
            // menu.setRemoveAllWhenShown(true);
            GEFActionConstants.addStandardActionGroups(menu);
            IAction action;
            action = getActionRegistry().getAction(ActionFactory.COPY.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.PASTE.getId());
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
            List<EditPart> editParts = getGraphicalViewer().getSelectedEditParts();
            GraphElement graphElement = null;
            if (editParts.size() == 1) {
                graphElement = (GraphElement) editParts.get(0).getModel();
            } else if (editParts.size() == 0) {
                graphElement = editor.getDefinition();
            }
            ISelectionProvider selectionProvider = new StructuredSelectionProvider(graphElement);
            ObjectActionContributorManager.getManager().contributeObjectActions(editor, menu, selectionProvider);
        }
    }

    @Override
    protected FlyoutPreferences getPalettePreferences() {
        return new PaletteFlyoutPreferences();
    }

}
