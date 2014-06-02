package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.model.TextDecorationNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;

public class MoveElementFeature extends DefaultMoveShapeFeature {
    public MoveElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (element instanceof Timer && element.getParent() instanceof ITimed) {
            return false;
        }
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (element instanceof Swimlane) {
            return parentObject instanceof ProcessDefinition;
        }
        return parentObject instanceof Swimlane || parentObject instanceof ProcessDefinition;
    }

    @Override
    protected void postMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        Rectangle newConstraint = element.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        element.setConstraint(newConstraint);
        if (context.getSourceContainer() != context.getTargetContainer()) {
            GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
            element.setParentContainer(parent);
            if (element instanceof SwimlanedNode) {
                Swimlane swimlane = null;
                if (parent instanceof Swimlane) {
                    swimlane = (Swimlane) parent;
                }
                ((SwimlanedNode) element).setSwimlane(swimlane);
            }
        }
        // move definition with point
        if (element instanceof HasTextDecorator) {
            HasTextDecorator withDefinition = (HasTextDecorator) element;
            Rectangle defPosition = withDefinition.getTextDecoratorEmulation().getDefinition().getConstraint().getCopy();
            defPosition.setX(defPosition.x + context.getDeltaX());
            defPosition.setY(defPosition.y + context.getDeltaY());
            withDefinition.getTextDecoratorEmulation().getDefinition().setConstraint(defPosition);
            Graphiti.getGaService().setLocation(withDefinition.getTextDecoratorEmulation().getDefinition().getUiContainer().getOwner().getGraphicsAlgorithm(),
                    defPosition.x, defPosition.y);
            withDefinition.getTextDecoratorEmulation().setDefinitionLocation(defPosition.getLocation());
        }
        // if text decoration moved
        if (element instanceof TextDecorationNode) {
            TextDecorationNode graph = (TextDecorationNode) element;
            HasTextDecorator withText = (HasTextDecorator) graph.getTarget();
            withText.getTextDecoratorEmulation().setDefinitionLocation(newConstraint.getLocation());
        }
    }
}
