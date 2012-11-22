package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.SwimlanedNode;

import com.google.common.base.Objects;

public class UpdateNodeFeature extends AbstractUpdateFeature {
    public UpdateNodeFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canUpdate(IUpdateContext context) {
        Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
        return bo instanceof Node;
    }

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
        // retrieve name from business model
        Object bo = getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode) {
            String swimlaneName = findPropertyRecursive(ga, PropertyNames.PROPERTY_SWIMLANE);
            if (!Objects.equal(swimlaneName, ((SwimlanedNode) bo).getSwimlaneName())) {
                return Reason.createTrueReason();
            }
        }
        if (bo instanceof NamedGraphElement) {
            String nodeName = findPropertyRecursive(ga, PropertyNames.PROPERTY_NAME);
            if (!Objects.equal(nodeName, ((NamedGraphElement) bo).getName())) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    private String findPropertyRecursive(GraphicsAlgorithm ga, String propertyName) {
        ga = findGaRecursive(ga, propertyName);
        if (ga instanceof Text) {
            return ((Text) ga).getValue();
        }
        if (ga instanceof MultiText) {
            return ((MultiText) ga).getValue();
        }
        return null;
    }

    private GraphicsAlgorithm findGaRecursive(GraphicsAlgorithm ga, String propertyName) {
        for (Property property : ga.getProperties()) {
            if (Objects.equal(propertyName, property.getKey())) {
                return ga;
            }
        }
        for (GraphicsAlgorithm childGa : ga.getGraphicsAlgorithmChildren()) {
            GraphicsAlgorithm result = findGaRecursive(childGa, propertyName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void setProperty(GraphicsAlgorithm ga, String propertyName, String value) {
        ga = findGaRecursive(ga, propertyName);
        if (ga instanceof Text) {
            ((Text) ga).setValue(value);
        }
        if (ga instanceof MultiText) {
            ((MultiText) ga).setValue(value);
        }
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
        // retrieve name from business model
        Object bo = getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode) {
            setProperty(ga, PropertyNames.PROPERTY_SWIMLANE, ((SwimlanedNode) bo).getSwimlaneLabel());
        }
        if (bo instanceof NamedGraphElement) {
            setProperty(ga, PropertyNames.PROPERTY_NAME, ((NamedGraphElement) bo).getName());
        }
        return true;
    }
}
