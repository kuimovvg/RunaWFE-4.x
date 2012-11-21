package ru.runa.gpd.editor.graphiti;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.CompositeConnection;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.tb.IContextMenuEntry;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorContextMenuProvider;
import org.eclipse.graphiti.ui.internal.action.CustomAction;
import org.eclipse.graphiti.ui.internal.parts.CompositeConnectionEditPart;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.graphiti.util.ILocationInfo;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.ObjectActionContributorManager;

public class DiagramContextMenuProvider extends DiagramEditorContextMenuProvider {
    private final IDiagramTypeProvider diagramTypeProvider;

    /**
     * Creates a new DiagramEditorContextMenuProvider.
     * 
     * @param viewer
     *            The EditPartViewer, for which the context-menu shall be
     *            displayed.
     * @param registry
     *            The action-registry, which contains the actions corresponding
     *            to the menu-items.
     * @param configurationProvider
     *            the configuration provider
     * @since 0.9
     */
    public DiagramContextMenuProvider(EditPartViewer viewer, ActionRegistry registry, IDiagramTypeProvider diagramTypeProvider) {
        super(viewer, registry, diagramTypeProvider);
        this.diagramTypeProvider = diagramTypeProvider;
    }

    @Override
    protected void addDefaultMenuGroupUndo(IMenuManager manager) {
        //        addActionToMenu(manager, ActionFactory.UNDO.getId(), GEFActionConstants.GROUP_UNDO);
        //        addActionToMenu(manager, ActionFactory.REDO.getId(), GEFActionConstants.GROUP_UNDO);
    }

    @Override
    protected void addDefaultMenuGroupSave(IMenuManager manager) {
        //        addActionToMenu(manager, SaveImageAction.ACTION_ID, GEFActionConstants.GROUP_SAVE);
    }

    @Override
    protected void addDefaultMenuGroupEdit(IMenuManager manager) {
        //        addActionToMenuIfAvailable(manager, ActionFactory.COPY.getId(), GEFActionConstants.GROUP_EDIT);
        //        addActionToMenuIfAvailable(manager, ActionFactory.PASTE.getId(), GEFActionConstants.GROUP_EDIT);
    }

    @Override
    protected void addDefaultMenuGroupPrint(IMenuManager manager) {
        //        IFeatureProvider fp = getDiagramTypeProvider().getFeatureProvider();
        //        if (fp != null) {
        //            IPrintFeature pf = fp.getPrintFeature();
        //            if (pf != null) {
        //                addActionToMenu(manager, ActionFactory.PRINT.getId(), GEFActionConstants.GROUP_PRINT);
        //            }
        //        }
    }

    @Override
    protected void addDefaultMenuGroupRest(IMenuManager manager) {
        //        addAlignmentSubMenu(manager, GEFActionConstants.GROUP_REST);
        //        addActionToMenuIfAvailable(manager, UpdateAction.ACTION_ID, GEFActionConstants.GROUP_REST);
        //        addActionToMenuIfAvailable(manager, RemoveAction.ACTION_ID, GEFActionConstants.GROUP_REST);
        //        addActionToMenuIfAvailable(manager, DeleteAction.ACTION_ID, GEFActionConstants.GROUP_REST);
        PictogramElement pes[] = getEditor().getSelectedPictogramElements();
        if (pes.length == 1) {
            final Object object = getDiagramTypeProvider().getFeatureProvider().getBusinessObjectForPictogramElement(pes[0]);
            ObjectActionContributorManager.getManager().contributeObjectActions(getEditor(), manager, new ISelectionProvider() {
                @Override
                public void setSelection(ISelection selection) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void removeSelectionChangedListener(ISelectionChangedListener listener) {
                    // TODO Auto-generated method stub
                }

                @Override
                public ISelection getSelection() {
                    return new StructuredSelection(object);
                }

                @Override
                public void addSelectionChangedListener(ISelectionChangedListener listener) {
                    // TODO Auto-generated method stub
                }
            });
        }
        //        ICustomContext context = new CustomContext(pes);
        //        boolean diagramSelected = false;
        //        if (pes.length == 1) {
        //            extendCustomContext(pes[0], (CustomContext) context);
        //            if (pes[0] instanceof Diagram) {
        //                diagramSelected = true;
        //            }
        //        }
        //        IToolBehaviorProvider tb = getDiagramTypeProvider().getCurrentToolBehaviorProvider();
        //        IContextMenuEntry[] contextMenuEntries = tb.getContextMenu(context);
        //        if (GFPreferences.getInstance().areDebugActionsActive()) {
        //            IFeatureProvider fp = getDiagramTypeProvider().getFeatureProvider();
        //            ContextMenuEntry debugEntry = new ContextMenuEntry(null, context);
        //            debugEntry.setText("Debug"); //$NON-NLS-1$
        //            debugEntry.setSubmenu(true);
        //            debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_FIGURE_DATA), context));
        //            if (diagramSelected) {
        //                debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_FIGURE_INCL_CONNECTION_DATA), context));
        //            }
        //            debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_PICTOGRAM_DATA), context));
        //            debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_EDIT_PART_DATA), context));
        //            debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_ALL), context));
        //            debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_REFRESH), context));
        //            IContextMenuEntry[] contextMenuEntries2 = new IContextMenuEntry[contextMenuEntries.length + 1];
        //            System.arraycopy(contextMenuEntries, 0, contextMenuEntries2, 0, contextMenuEntries.length);
        //            contextMenuEntries2[contextMenuEntries2.length - 1] = debugEntry;
        //            contextMenuEntries = contextMenuEntries2;
        //        }
        //        addEntries(manager, contextMenuEntries, context, GEFActionConstants.GROUP_REST, null);
    }

    private void addEntries(IMenuManager manager, IContextMenuEntry[] contextMenuEntries, ICustomContext context, String groupID, String textParentEntry) {
        for (int i = 0; i < contextMenuEntries.length; i++) {
            IContextMenuEntry cmEntry = contextMenuEntries[i];
            String text = cmEntry.getText();
            if (cmEntry.getChildren().length == 0) {
                IFeature feature = cmEntry.getFeature();
                if (feature instanceof ICustomFeature && feature.isAvailable(context)) {
                    IAction action = new CustomAction((ICustomFeature) feature, context, getEditor());
                    if (textParentEntry != null) {
                        text = textParentEntry + " " + text; //$NON-NLS-1$
                    }
                    action.setText(text);
                    action.setDescription(cmEntry.getDescription());
                    ImageDescriptor image = GraphitiUi.getImageService().getImageDescriptorForId(cmEntry.getIconId());
                    action.setImageDescriptor(image);
                    appendContributionItem(manager, groupID, new ActionContributionItem(action));
                }
            } else {
                if (cmEntry.isSubmenu()) {
                    MenuManager subMenu = new MenuManager(text);
                    addEntries(subMenu, cmEntry.getChildren(), context, null, null);
                    if (!subMenu.isEmpty()) {
                        appendContributionItem(manager, groupID, subMenu);
                    }
                } else {
                    appendContributionItem(manager, groupID, new Separator());
                    addEntries(manager, cmEntry.getChildren(), context, groupID, text);
                    appendContributionItem(manager, groupID, new Separator());
                }
            }
        }
    }

    private void appendContributionItem(IMenuManager manager, String groupID, IContributionItem contributionItem) {
        if (groupID != null) {
            manager.appendToGroup(groupID, contributionItem);
        } else {
            manager.add(contributionItem);
        }
    }

    // ====================== add single menu-entries =========================
    private void extendCustomContext(PictogramElement pe, CustomContext context) {
        Point location = getEditor().getMouseLocation();
        int mX = location.x;
        int mY = location.y;
        context.setX(mX);
        context.setY(mY);
        if (pe instanceof Shape && !(pe instanceof Diagram)) {
            GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
            if (ga != null) {
                ILocation relLocation = Graphiti.getPeService().getLocationRelativeToDiagram((Shape) pe);
                int x = relLocation.getX();
                int y = relLocation.getY();
                int width = ga.getWidth();
                int height = ga.getHeight();
                if (mX > x && mX < x + width && mY > y && mY < y + height) {
                    int relativeX = mX - x;
                    int relativeY = mY - y;
                    ILocationInfo locationInfo = Graphiti.getLayoutService().getLocationInfo((Shape) pe, relativeX, relativeY);
                    context.setInnerPictogramElement(locationInfo.getShape());
                    context.setInnerGraphicsAlgorithm(locationInfo.getGraphicsAlgorithm());
                }
            }
        } else if (pe instanceof CompositeConnection) {
            List<?> selectedEditParts = getViewer().getSelectedEditParts();
            for (Object object : selectedEditParts) {
                if (object instanceof CompositeConnectionEditPart) {
                    // Retrieve child selection info from the edit part
                    CompositeConnectionEditPart compEditPart = (CompositeConnectionEditPart) object;
                    org.eclipse.graphiti.ui.internal.parts.ConnectionEditPart originallySelectedChildConnection = compEditPart.getOriginallySelectedChild();
                    if (originallySelectedChildConnection != null) {
                        // and provide the originally selection child connection
                        // as inner PE
                        Connection connectionPicto = (Connection) originallySelectedChildConnection.getModel();
                        context.setInnerGraphicsAlgorithm(connectionPicto.getGraphicsAlgorithm());
                        context.setInnerPictogramElement(connectionPicto);
                    }
                }
            }
        }
    }

    /**
     * Adds the alignment sub menu.
     * 
     * @param manager
     *            the manager
     * @param group
     *            the group
     */
    @Override
    protected void addAlignmentSubMenu(IMenuManager manager, String group) {
        //        IAction action;
        //        MenuManager alignmentSubMenu = new MenuManager(Messages.GraphicsContextMenuProvider_0_xmen);
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_LEFT);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_CENTER);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_RIGHT);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_TOP);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_MIDDLE);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_BOTTOM);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.MATCH_WIDTH);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        action = this.actionRegistry.getAction(GEFActionConstants.MATCH_HEIGHT);
        //        if (action != null && action.isEnabled()) {
        //            alignmentSubMenu.add(action);
        //        }
        //        if (!alignmentSubMenu.isEmpty()) {
        //            manager.appendToGroup(group, alignmentSubMenu);
        //        }
    }

    private IDiagramTypeProvider getDiagramTypeProvider() {
        return this.diagramTypeProvider;
    }

    private DiagramEditor getEditor() {
        return (DiagramEditor) getDiagramTypeProvider().getDiagramEditor();
    }
}
