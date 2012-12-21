package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorContextMenuProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ObjectActionContributorManager;

import ru.runa.gpd.editor.StructuredSelectionProvider;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class DiagramContextMenuProvider extends DiagramEditorContextMenuProvider {
    private final IDiagramTypeProvider diagramTypeProvider;
    private final ProcessDefinition definition;

    /**
     * @param viewer
     *            The EditPartViewer, for which the context-menu shall be
     *            displayed.
     * @param registry
     *            The action-registry, which contains the actions corresponding
     *            to the menu-items.
     * @param configurationProvider
     *            the configuration provider
     */
    public DiagramContextMenuProvider(EditPartViewer viewer, ActionRegistry registry, IDiagramTypeProvider diagramTypeProvider, ProcessDefinition definition) {
        super(viewer, registry, diagramTypeProvider);
        this.diagramTypeProvider = diagramTypeProvider;
        this.definition = definition;
    }

    @Override
    protected void addDefaultMenuGroupUndo(IMenuManager manager) {
    }

    @Override
    protected void addDefaultMenuGroupSave(IMenuManager manager) {
    }

    @Override
    protected void addDefaultMenuGroupEdit(IMenuManager manager) {
        addActionToMenuIfAvailable(manager, ActionFactory.COPY.getId(), GEFActionConstants.GROUP_EDIT);
        addActionToMenuIfAvailable(manager, ActionFactory.PASTE.getId(), GEFActionConstants.GROUP_EDIT);
    }

    @Override
    protected void addDefaultMenuGroupPrint(IMenuManager manager) {
    }

    @Override
    protected void addDefaultMenuGroupRest(IMenuManager manager) {
        PictogramElement pes[] = getEditor().getSelectedPictogramElements();
        if (pes.length == 1) {
            Object object = getDiagramTypeProvider().getFeatureProvider().getBusinessObjectForPictogramElement(pes[0]);
            ISelectionProvider selectionProvider = new StructuredSelectionProvider(object);
            ObjectActionContributorManager.getManager().contributeObjectActions(getEditor(), manager, selectionProvider);
        }
    }

    @Override
    protected void addAlignmentSubMenu(IMenuManager manager, String group) {
    }

    private IDiagramTypeProvider getDiagramTypeProvider() {
        return this.diagramTypeProvider;
    }

    private DiagramEditor getEditor() {
        return (DiagramEditor) getDiagramTypeProvider().getDiagramEditor();
    }
}
