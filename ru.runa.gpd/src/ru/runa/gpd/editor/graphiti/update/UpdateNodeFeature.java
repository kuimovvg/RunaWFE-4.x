package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

import ru.runa.gpd.lang.model.Node;

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
        // retrieve name from pictogram model
        String pictogramName = null;
        PictogramElement pictogramElement = context.getPictogramElement();
        if (pictogramElement instanceof ContainerShape) {
            ContainerShape cs = (ContainerShape) pictogramElement;
            for (Shape shape : cs.getChildren()) {
                if (shape.getGraphicsAlgorithm() instanceof Text) {
                    Text text = (Text) shape.getGraphicsAlgorithm();
                    pictogramName = text.getValue();
                }
            }
        }
        // retrieve name from business model
        Node node = (Node) getBusinessObjectForPictogramElement(pictogramElement);
        if (!Objects.equal(pictogramName, node.getName())) {
            return Reason.createTrueReason();
        } else {
            return Reason.createFalseReason();
        }
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from business model
        PictogramElement pictogramElement = context.getPictogramElement();
        Node node = (Node) getBusinessObjectForPictogramElement(pictogramElement);
        // Set name in pictogram model
        if (pictogramElement instanceof ContainerShape) {
            ContainerShape cs = (ContainerShape) pictogramElement;
            for (Shape shape : cs.getChildren()) {
                if (shape.getGraphicsAlgorithm() instanceof Text) {
                    Text text = (Text) shape.getGraphicsAlgorithm();
                    text.setValue(node.getName());
                    return true;
                }
                if (shape.getGraphicsAlgorithm() instanceof MultiText) {
                    MultiText text = (MultiText) shape.getGraphicsAlgorithm();
                    text.setValue(node.getName());
                    return true;
                }
            }
        }
        return false;
    }
}
