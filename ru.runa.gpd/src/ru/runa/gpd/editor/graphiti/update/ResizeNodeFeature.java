package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.add.AddStateNodeFeature;
import ru.runa.gpd.lang.model.Node;

import com.google.common.base.Objects;

public class ResizeNodeFeature extends DefaultResizeShapeFeature implements GEFConstants {
    public ResizeNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        Node node = (Node) getBusinessObjectForPictogramElement(shape);
        if (node != null) {
            IAddFeature addFeature = ((DiagramFeatureProvider) getFeatureProvider()).getAddFeature(node.getClass());
            if (addFeature instanceof AddStateNodeFeature) {
                //return !((AddNodeFeature) addFeature).isFixedSize();
                return true;
            }
        }
        return false;
    }

    protected GraphicsAlgorithm findGaRecursiveByName(PictogramElement pe, String name) {
        GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
        for (Property property : pe.getProperties()) {
            if (Objects.equal(GaProperty.ID, property.getKey()) && Objects.equal(name, property.getValue())) {
                return ga;
            }
        }
        GraphicsAlgorithm result = findGaRecursiveByName(ga, name);
        if (result != null) {
            return result;
        }
        if (pe instanceof Connection) {
            Connection connection = (Connection) pe;
            for (ConnectionDecorator connectionDecorator : connection.getConnectionDecorators()) {
                result = findGaRecursiveByName(connectionDecorator, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected GraphicsAlgorithm findGaRecursiveByName(GraphicsAlgorithm ga, String name) {
        for (Property property : ga.getProperties()) {
            if (Objects.equal(GaProperty.ID, property.getKey()) && Objects.equal(name, property.getValue())) {
                return ga;
            }
        }
        for (GraphicsAlgorithm childGa : ga.getGraphicsAlgorithmChildren()) {
            GraphicsAlgorithm result = findGaRecursiveByName(childGa, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void resizeShape(IResizeShapeContext context) {
        super.resizeShape(context);
        Node node = (Node) getBusinessObjectForPictogramElement(context.getShape());
        Rectangle bounds = node.getConstraint().getCopy();
        bounds.x = context.getX();
        bounds.y = context.getY();
        bounds.width = context.getWidth();
        bounds.height = context.getHeight();
        node.setConstraint(bounds);
        // see ga structure in AddStateNodeFeature and hierarchy
        GraphicsAlgorithm ga = context.getShape().getGraphicsAlgorithm();
        RoundedRectangle borderRect = (RoundedRectangle) ga.getGraphicsAlgorithmChildren().get(0);
        borderRect.setWidth(bounds.width - GRID_SIZE);
        borderRect.setHeight(bounds.height - GRID_SIZE);
        Text swimlaneText = (Text) findGaRecursiveByName(ga, GaProperty.SWIMLANE_NAME);
        if (swimlaneText != null) {
            swimlaneText.setWidth(bounds.width - GRID_SIZE);
            swimlaneText.setHeight(2 * GRID_SIZE);
        }
        MultiText nameMultiText = (MultiText) findGaRecursiveByName(ga, GaProperty.NAME);
        if (nameMultiText != null) {
            nameMultiText.setWidth(bounds.width - GRID_SIZE);
            nameMultiText.setHeight(bounds.height - 4 * GRID_SIZE);
        }
        Image subProcessImage = (Image) findGaRecursiveByName(ga, GaProperty.SUBPROCESS);
        if (subProcessImage != null) {
            Graphiti.getGaService().setLocationAndSize(subProcessImage, bounds.width / 2 - 7, bounds.height - 2 * GRID_SIZE, 14, 14);
        }
        Image multiProcessImage = (Image) findGaRecursiveByName(ga, GaProperty.MULTIPROCESS);
        if (multiProcessImage != null) {
            Graphiti.getGaService().setLocationAndSize(multiProcessImage, bounds.width / 2 - 8, bounds.height - 2 * GRID_SIZE, 16, 12);
        }
    }
}
