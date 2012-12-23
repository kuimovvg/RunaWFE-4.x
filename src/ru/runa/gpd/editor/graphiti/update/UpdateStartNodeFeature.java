package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.StartState;

import com.google.common.base.Objects;

public class UpdateStartNodeFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        StartState bo = (StartState) getBusinessObjectForPictogramElement(pe);
        String swimlaneName = PropertyUtil.findTextValueRecursive(pe, GaProperty.SWIMLANE_NAME);
        if (!Objects.equal(swimlaneName, bo.getSwimlaneLabel())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        StartState bo = (StartState) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setProperty(pe, GaProperty.SWIMLANE_NAME, bo.getSwimlaneLabel());
        return true;
    }
}
