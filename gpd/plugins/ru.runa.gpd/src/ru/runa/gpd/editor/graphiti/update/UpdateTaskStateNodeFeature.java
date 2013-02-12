package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.TaskState;

import com.google.common.base.Objects;

public class UpdateTaskStateNodeFeature extends UpdateStateNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        IReason reason = super.updateNeeded(context);
        if (!reason.toBoolean()) {
            PictogramElement pe = context.getPictogramElement();
            TaskState bo = (TaskState) getBusinessObjectForPictogramElement(pe);
            String minimazed = PropertyUtil.getPropertyValue(pe, GaProperty.MINIMAZED_VIEW);
            if (!Objects.equal(minimazed, String.valueOf(bo.isMinimizedView()))) {
                return Reason.createTrueReason();
            }
        }
        return reason;
    }
    
    @Override
    public boolean update(IUpdateContext context) {
        super.update(context);
        PictogramElement pe = context.getPictogramElement();
        TaskState bo = (TaskState) getBusinessObjectForPictogramElement(pe);
        String minimazed = PropertyUtil.getPropertyValue(pe, GaProperty.MINIMAZED_VIEW);
        if (!Objects.equal(minimazed, String.valueOf(bo.isMinimizedView()))) {
            if (!PropertyUtil.setPropertyValue(pe, GaProperty.MINIMAZED_VIEW, String.valueOf(bo.isMinimizedView()))) {
                System.out.println("-ERROR in ru.runa.gpd.editor.graphiti.update.UpdateTaskStateNodeFeature.update(IUpdateContext)");
            }
            layoutPictogramElement(pe);
        }
        return true;
    }
}
