package ru.runa.gpd.editor.graphiti.add;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.IColorConstant;

import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.editor.graphiti.TextUtil;
import ru.runa.gpd.lang.model.Bendpoint;
import ru.runa.gpd.lang.model.Transition;

public class AddTransitionFeature extends AbstractAddFeature {
    public static final String BENDPOINTS_PROPERTY = "bendpoints";
    public static final String LABEL_PROPERTY = "label";

    public AddTransitionFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canAdd(IAddContext context) {
        return (context instanceof IAddConnectionContext && context.getNewObject() instanceof Transition);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        IAddConnectionContext addConnectionContext = (IAddConnectionContext) context;
        Transition transition = (Transition) context.getNewObject();
        Anchor sourceAnchor = addConnectionContext.getSourceAnchor();
        Anchor targetAnchor = addConnectionContext.getTargetAnchor();
        if (sourceAnchor == null || targetAnchor == null) {
            return null;
        }
        IPeCreateService peCreateService = Graphiti.getPeCreateService();
        // CONNECTION WITH POLYLINE
        FreeFormConnection connection = peCreateService.createFreeFormConnection(getDiagram());
        connection.setStart(sourceAnchor);
        connection.setEnd(targetAnchor);
        sourceAnchor.getOutgoingConnections().add(connection);
        targetAnchor.getIncomingConnections().add(connection);
        GraphicsAlgorithm sourceGraphics = getPictogramElement(transition.getSource()).getGraphicsAlgorithm();
        GraphicsAlgorithm targetGraphics = getPictogramElement(transition.getTarget()).getGraphicsAlgorithm();
        List<Bendpoint> bendpoints = (List<Bendpoint>) addConnectionContext.getProperty(BENDPOINTS_PROPERTY);
        if (bendpoints != null && bendpoints.size() >= 0) {
            for (Bendpoint bendpoint : bendpoints) {
                Point point = StylesFactory.eINSTANCE.createPoint();
                point.setX(bendpoint.getX());
                point.setY(bendpoint.getY());
                connection.getBendpoints().add(point);
            }
        } else {
            Shape sourceShape = (Shape) getPictogramElement(transition.getSource());
            ILocation sourceShapeLocation = Graphiti.getLayoutService().getLocationRelativeToDiagram(sourceShape);
            int sourceX = sourceShapeLocation.getX();
            int sourceY = sourceShapeLocation.getY();
            Shape targetShape = (Shape) getPictogramElement(transition.getTarget());
            ILocation targetShapeLocation = Graphiti.getLayoutService().getLocationRelativeToDiagram(targetShape);
            int targetX = targetShapeLocation.getX();
            int targetY = targetShapeLocation.getY();
            //            if (addedSequenceFlow.getSource() instanceof Gateway && addedSequenceFlow.getTarget() instanceof Gateway == false) {
            //                if (((sourceGraphics.getY() + 10) < targetGraphics.getY() || (sourceGraphics.getY() - 10) > targetGraphics.getY())
            //                        && (sourceGraphics.getX() + (sourceGraphics.getWidth() / 2)) < targetGraphics.getX()) {
            //                    boolean subProcessWithBendPoint = false;
            //                    if (addedSequenceFlow.getTargetRef() instanceof SubProcess) {
            //                        int middleSub = targetGraphics.getY() + (targetGraphics.getHeight() / 2);
            //                        if ((sourceGraphics.getY() + 20) < middleSub || (sourceGraphics.getY() - 20) > middleSub) {
            //                            subProcessWithBendPoint = true;
            //                        }
            //                    }
            //                    if (addedSequenceFlow.getTargetRef() instanceof SubProcess == false || subProcessWithBendPoint == true) {
            //                        Point bendPoint = StylesFactory.eINSTANCE.createPoint();
            //                        bendPoint.setX(sourceX + 20);
            //                        bendPoint.setY(targetY + (targetGraphics.getHeight() / 2));
            //                        connection.getBendpoints().add(bendPoint);
            //                    }
            //                }
            //            } else if (addedSequenceFlow.getTargetRef() instanceof Gateway) {
            //                if (((sourceGraphics.getY() + 10) < targetGraphics.getY() || (sourceGraphics.getY() - 10) > targetGraphics.getY())
            //                        && (sourceGraphics.getX() + sourceGraphics.getWidth()) < targetGraphics.getX()) {
            //                    boolean subProcessWithBendPoint = false;
            //                    if (addedSequenceFlow.getSourceRef() instanceof SubProcess) {
            //                        int middleSub = sourceGraphics.getY() + (sourceGraphics.getHeight() / 2);
            //                        if ((middleSub + 20) < targetGraphics.getY() || (middleSub - 20) > targetGraphics.getY()) {
            //                            subProcessWithBendPoint = true;
            //                        }
            //                    }
            //                    if (addedSequenceFlow.getSourceRef() instanceof SubProcess == false || subProcessWithBendPoint == true) {
            //                        Point bendPoint = StylesFactory.eINSTANCE.createPoint();
            //                        bendPoint.setX(targetX + 20);
            //                        bendPoint.setY(sourceY + (sourceGraphics.getHeight() / 2));
            //                        connection.getBendpoints().add(bendPoint);
            //                    }
            //                }
            //            } else if (addedSequenceFlow.getTargetRef() instanceof EndEvent) {
            //                int middleSource = sourceGraphics.getY() + (sourceGraphics.getHeight() / 2);
            //                int middleTarget = targetGraphics.getY() + (targetGraphics.getHeight() / 2);
            //                if (((middleSource + 10) < middleTarget && (sourceGraphics.getX() + sourceGraphics.getWidth()) < targetGraphics.getX())
            //                        || ((middleSource - 10) > middleTarget && (sourceGraphics.getX() + sourceGraphics.getWidth()) < targetGraphics.getX())) {
            //                    Point bendPoint = StylesFactory.eINSTANCE.createPoint();
            //                    bendPoint.setX(targetX + (targetGraphics.getWidth() / 2));
            //                    bendPoint.setY(sourceY + (sourceGraphics.getHeight() / 2));
            //                    connection.getBendpoints().add(bendPoint);
            //                }
            //            }
        }
        IGaService gaService = Graphiti.getGaService();
        Polyline polyline = gaService.createPolyline(connection);
        polyline.setLineStyle(LineStyle.SOLID);
        polyline.setForeground(Graphiti.getGaService().manageColor(getDiagram(), IColorConstant.BLACK));
        // create link and wire it
        link(connection, transition);
        // add dynamic text decorator for the reference name
        ConnectionDecorator textDecorator = peCreateService.createConnectionDecorator(connection, true, 0.5, true);
        MultiText text = gaService.createDefaultMultiText(getDiagram(), textDecorator);
        // text.setStyle(StyleUtil.getStyleForTask((getDiagram())));
        text.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
        text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        if (addConnectionContext.getProperty(LABEL_PROPERTY) != null) {
            Rectangle labelLocation = (Rectangle) addConnectionContext.getProperty(LABEL_PROPERTY);
            gaService.setLocation(text, labelLocation.x, labelLocation.y);
        } else {
            gaService.setLocation(text, 10, 0);
        }
        TextUtil.setTextSize(transition.getName(), text);
        // set reference name in the text decorator
        text.setValue(transition.getName());
        // add static graphical decorators (composition and navigable)
        ConnectionDecorator cd = peCreateService.createConnectionDecorator(connection, false, 1.0, true);
        createArrow(cd);
        return connection;
    }

    private Polygon createArrow(GraphicsAlgorithmContainer gaContainer) {
        int xy[] = new int[] { -10, -5, 0, 0, -10, 5, -8, 0 };
        int beforeAfter[] = new int[] { 3, 3, 0, 0, 3, 3, 3, 3 };
        Polygon polyline = Graphiti.getGaCreateService().createPolygon(gaContainer, xy, beforeAfter);
        polyline.setStyle(StyleUtil.getStyleForPolygon(getDiagram()));
        return polyline;
    }

    private PictogramElement getPictogramElement(Object businessObject) {
        return getFeatureProvider().getPictogramElementForBusinessObject(businessObject);
    }
}
