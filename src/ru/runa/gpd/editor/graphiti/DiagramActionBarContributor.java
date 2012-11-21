package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.RetargetAction;

// see org.eclipse.graphiti.ui.editor.DiagramEditorActionBarContributor
public class DiagramActionBarContributor extends ActionBarContributor {
    /**
     * Creates and initialises all Actions. See the corresponding method in the
     * super class.
     * 
     * @see org.eclipse.gef.ui.actions.ActionBarContributor
     */
    @Override
    protected void buildActions() {
        addRetargetAction(new ZoomInRetargetAction());
        addRetargetAction(new ZoomOutRetargetAction());
        //        addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY, Messages.DiagramEditorActionBarContributor_Grid, IAction.AS_CHECK_BOX));
        //        addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY, Messages.DiagramEditorActionBarContributor_SnapGeometry, IAction.AS_CHECK_BOX));
    }

    /**
     * Global action keys are already declared with
     * {@link #addRetargetAction(RetargetAction)}. See the corresponding method
     * in the super class.
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
        tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
        tbm.add(getAction(GEFActionConstants.ZOOM_IN));
        ZoomComboContributionItem zoomCombo = new ZoomComboContributionItem(getPage());
        tbm.add(zoomCombo);
        tbm.add(new Separator());
    }
}
