package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SwimlanedNode;

import com.google.common.base.Objects;

public class UpdateStateNodeFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode) {
            String swimlaneName = findTextValueRecursive(pe, GaProperty.SWIMLANE_NAME);
            if (!Objects.equal(swimlaneName, ((SwimlanedNode) bo).getSwimlaneName())) {
                return Reason.createTrueReason();
            }
        }
        String nodeName = findTextValueRecursive(pe, GaProperty.NAME);
        if (!Objects.equal(nodeName, bo.getName())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode) {
            setProperty(pe, GaProperty.SWIMLANE_NAME, ((SwimlanedNode) bo).getSwimlaneLabel());
        }
        setProperty(pe, GaProperty.NAME, bo.getName());
        return true;
    }
}
