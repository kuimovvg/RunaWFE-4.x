package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Transition;

import com.google.common.base.Objects;

public class UpdateTransitionFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Transition bo = (Transition) getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm defaultFlowGa = findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null && defaultFlowGa.getPictogramElement().isVisible() != bo.isDefaultFlow()) {
            return Reason.createTrueReason();
        }
        GraphicsAlgorithm exclusiveFlowGa = findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null && exclusiveFlowGa.getPictogramElement().isVisible() != bo.isExclusiveFlow()) {
            return Reason.createTrueReason();
        }
        String transitionName = findTextValueRecursive(pe, GaProperty.NAME);
        if (transitionName != null && !Objects.equal(transitionName, bo.getName())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Transition bo = (Transition) getBusinessObjectForPictogramElement(pe);
        setProperty(pe, GaProperty.NAME, ((NamedGraphElement) bo).getName());
        GraphicsAlgorithm defaultFlowGa = findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null) {
            defaultFlowGa.getPictogramElement().setVisible(bo.isDefaultFlow());
        }
        GraphicsAlgorithm exclusiveFlowGa = findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null) {
            exclusiveFlowGa.getPictogramElement().setVisible(bo.isExclusiveFlow());
        }
        return true;
    }
}
