package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Lists;

/**
 * Information saved in absolute coordinates for all elements.
 * 
 * @author Dofs
 * @since 4.0
 */
public class GpdXmlContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "gpd.xml";
    private static final String Y_ATTRIBUTE_NAME = "y";
    private static final String X_ATTRIBUTE_NAME = "x";
    private static final String NOTATION_ATTRIBUTE_NAME = "notation";
    private static final String RENDERED_ATTRIBUTE_NAME = "rendered";
    private static final String HEIGHT_ATTRIBUTE_NAME = "height";
    private static final String WIDTH_ATTRIBUTE_NAME = "width";
    private static final String MIN_VIEW_ATTRIBUTE_NAME = "minimizedView";
    private static final String SHOW_ACTIONS_NAME = "showActions";
    private static final String SHOW_GRID_NAME = "showGrid";
    private static final String PROCESS_DIAGRAM_ELEMENT_NAME = "process-diagram";
    private static final String NODE_ELEMENT_NAME = "node";
    private static final String TRANSITION_ELEMENT_NAME = "transition";
    private static final String BENDPOINT_ELEMENT_NAME = "bendpoint";
    private static final String LABEL_ELEMENT_NAME = "label";

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }
    
    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        Element processDiagramInfo = document.getRootElement();
        addProcessDiagramInfo(definition, processDiagramInfo);
        List<Element> children = processDiagramInfo.elements(NODE_ELEMENT_NAME);
        for (Element element : children) {
            String nodeId = element.attributeValue(NAME_ATTRIBUTE_NAME);
            GraphElement graphElement = definition.getGraphElementByIdNotNull(nodeId);
            Rectangle constraint = new Rectangle();
            constraint.x = getIntAttribute(element, X_ATTRIBUTE_NAME, 0);
            constraint.y = getIntAttribute(element, Y_ATTRIBUTE_NAME, 0);
            constraint.width = getIntAttribute(element, WIDTH_ATTRIBUTE_NAME, 0);
            constraint.height = getIntAttribute(element, HEIGHT_ATTRIBUTE_NAME, 0);
            boolean minimizedView = getBooleanAttribute(element, MIN_VIEW_ATTRIBUTE_NAME, false);
            graphElement.setConstraint(constraint);
            if (graphElement instanceof Node) {
                ((Node) graphElement).setMinimizedView(minimizedView);
                List<Transition> leavingTransitions = ((Node) graphElement).getLeavingTransitions();
                List<Element> transitionInfoList = element.elements(TRANSITION_ELEMENT_NAME);
                for (int i = 0; i < leavingTransitions.size(); i++) {
                    Element transitionElement = transitionInfoList.get(i);
                    String transitionName = transitionElement.attributeValue(NAME_ATTRIBUTE_NAME);
                    for (Transition transition : leavingTransitions) {
                        if (transition.getName().equals(transitionName)) {
                            List<Point> bendpoints = Lists.newArrayList();
                            Element labelElement = transitionElement.element(LABEL_ELEMENT_NAME);
                            if (labelElement != null) {
                                int x = getIntAttribute(labelElement, X_ATTRIBUTE_NAME, 0);
                                int y = getIntAttribute(labelElement, Y_ATTRIBUTE_NAME, 0);
                                transition.setLabelLocation(new Point(x, y));
                            }
                            List<Element> bendpointInfoList = transitionElement.elements(BENDPOINT_ELEMENT_NAME);
                            for (Element bendpointElement : bendpointInfoList) {
                                int x = getIntAttribute(bendpointElement, X_ATTRIBUTE_NAME, 0);
                                int y = getIntAttribute(bendpointElement, Y_ATTRIBUTE_NAME, 0);
                                bendpoints.add(new Point(x, y));
                            }
                            transition.setBendpoints(bendpoints);
                            break;
                        }
                    }
                }
            }
        }
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            GraphElement parentGraphElement = graphElement.getParentContainer();
            if (parentGraphElement != null && !parentGraphElement.equals(definition)) {
                Rectangle parentConstraint = parentGraphElement.getConstraint();
                Rectangle constraint = graphElement.getConstraint();
                constraint.x -= parentConstraint.x;
                constraint.y -= parentConstraint.y;
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(PROCESS_DIAGRAM_ELEMENT_NAME);
        Element root = document.getRootElement();
        addAttribute(root, NAME_ATTRIBUTE_NAME, definition.getName());
        addAttribute(root, NOTATION_ATTRIBUTE_NAME, definition.getLanguage().getNotation());
        if (definition.getLanguage() == Language.BPMN) {
            addAttribute(root, RENDERED_ATTRIBUTE_NAME, "graphiti");
        }
        Dimension dimension = definition.getDimension();
        addAttribute(root, WIDTH_ATTRIBUTE_NAME, String.valueOf(dimension.width));
        addAttribute(root, HEIGHT_ATTRIBUTE_NAME, String.valueOf(dimension.height));
        addAttribute(root, SHOW_ACTIONS_NAME, String.valueOf(definition.isShowActions()));
        addAttribute(root, SHOW_GRID_NAME, String.valueOf(definition.isShowGrid()));
        int xOffset = 0;
        int yOffset = 0;
        int canvasShift = 0;
        if (definition.getLanguage() == Language.BPMN) {
            canvasShift = 5;
        }
        // calculating negative offsets;
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            Rectangle constraint = graphElement.getConstraint();
            if (constraint.x - canvasShift < xOffset) {
                xOffset = constraint.x - canvasShift;
            }
            if (constraint.y - canvasShift < yOffset) {
                yOffset = constraint.y - canvasShift;
            }
            if (graphElement instanceof Node) {
                Node node = (Node) graphElement;
                for (Transition transition : node.getLeavingTransitions()) {
                    for (Point bendpoint : transition.getBendpoints()) {
                        if (bendpoint.x - canvasShift < xOffset) {
                            xOffset = bendpoint.x - canvasShift;
                        }
                        if (bendpoint.y - canvasShift < yOffset) {
                            yOffset = bendpoint.y - canvasShift;
                        }
                    }
                }
            }
        }
        for (GraphElement graphElement : definition.getElementsRecursive()) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            Element element = root.addElement(NODE_ELEMENT_NAME);
            addAttribute(element, NAME_ATTRIBUTE_NAME, graphElement.getId());
            Rectangle constraint = graphElement.getConstraint().getCopy();
            GraphElement parentGraphElement = graphElement.getParentContainer();
            Rectangle parentConstraint = null;
            if (parentGraphElement != null && !parentGraphElement.equals(definition)) {
                parentConstraint = parentGraphElement.getConstraint();
                constraint.x += parentConstraint.x;
                constraint.y += parentConstraint.y;
            }
            if (constraint.isEmpty()) {
                throw new Exception("Invalid figure size: " + constraint.getSize());
            }
            addAttribute(element, X_ATTRIBUTE_NAME, String.valueOf(constraint.x - xOffset));
            addAttribute(element, Y_ATTRIBUTE_NAME, String.valueOf(constraint.y - yOffset));
            addAttribute(element, WIDTH_ATTRIBUTE_NAME, String.valueOf(constraint.width));
            addAttribute(element, HEIGHT_ATTRIBUTE_NAME, String.valueOf(constraint.height));
            if (graphElement instanceof Node) {
                Node node = (Node) graphElement;
                if (node.isMinimizedView()) {
                    addAttribute(element, MIN_VIEW_ATTRIBUTE_NAME, "true");
                }
                for (Transition transition : node.getLeavingTransitions()) {
                    Element transitionElement = element.addElement(TRANSITION_ELEMENT_NAME);
                    String name = transition.getName();
                    if (name != null) {
                        addAttribute(transitionElement, NAME_ATTRIBUTE_NAME, name);
                    }
                    if (transition.getLabelLocation() != null) {
                        Element labelElement = transitionElement.addElement(LABEL_ELEMENT_NAME);
                        addAttribute(labelElement, X_ATTRIBUTE_NAME, String.valueOf(transition.getLabelLocation().x));
                        addAttribute(labelElement, Y_ATTRIBUTE_NAME, String.valueOf(transition.getLabelLocation().y));
                    }
                    for (Point bendpoint : transition.getBendpoints()) {
                        Element bendpointElement = transitionElement.addElement(BENDPOINT_ELEMENT_NAME);
                        int x = bendpoint.x - xOffset;
                        int y = bendpoint.y - yOffset;
                        addAttribute(bendpointElement, X_ATTRIBUTE_NAME, String.valueOf(x));
                        addAttribute(bendpointElement, Y_ATTRIBUTE_NAME, String.valueOf(y));
                    }
                }
            }
        }
        return document;
    }

    private void addProcessDiagramInfo(ProcessDefinition definition, Element processDiagramInfo) {
        int width = getIntAttribute(processDiagramInfo, WIDTH_ATTRIBUTE_NAME, 0);
        int height = getIntAttribute(processDiagramInfo, HEIGHT_ATTRIBUTE_NAME, 0);
        definition.setDimension(new Dimension(width, height));
        if (!(definition instanceof SubprocessDefinition)) {
            definition.setShowActions(getBooleanAttribute(processDiagramInfo, SHOW_ACTIONS_NAME, false));
            definition.setShowGrid(getBooleanAttribute(processDiagramInfo, SHOW_GRID_NAME, false));
        }
    }
}
