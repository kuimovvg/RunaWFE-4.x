package org.jbpm.ui.par;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.Bendpoint;
import org.jbpm.ui.common.model.Node;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.State;
import org.jbpm.ui.common.model.Transition;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GpdXmlContentProvider extends AuxContentProvider {
    public static final String GPD_FILE_NAME = "gpd.xml";

    private static final String Y_ATTRIBUTE_NAME = "y";

    private static final String X_ATTRIBUTE_NAME = "x";

    private static final String NOTATION_ATTRIBUTE_NAME = "notation";

    private static final String HEIGHT_ATTRIBUTE_NAME = "height";

    private static final String WIDTH_ATTRIBUTE_NAME = "width";

    private static final String MIN_VIEW_ATTRIBUTE_NAME = "minimizedView";

    private static final String SHOW_ACTIONS_NAME = "showActions";

    private static final String SHOW_GRID_NAME = "showGrid";

    private static final String PROCESS_DIAGRAM_ELEMENT_NAME = "process-diagram";

    private static final String NODE_ELEMENT_NAME = "node";

    private static final String TRANSITION_ELEMENT_NAME = "transition";

    private static final String BENDPOINT_ELEMENT_NAME = "bendpoint";

    private void addProcessDiagramInfo(ProcessDefinition definition, Element processDiagramInfo) {
        int width = getIntAttribute(processDiagramInfo, WIDTH_ATTRIBUTE_NAME, 0);
        int height = getIntAttribute(processDiagramInfo, HEIGHT_ATTRIBUTE_NAME, 0);
        definition.setDimension(new Dimension(width, height));
        definition.setNotation(getAttribute(processDiagramInfo, NOTATION_ATTRIBUTE_NAME, "uml"));
        definition.setShowActions(getBooleanAttribute(processDiagramInfo, SHOW_ACTIONS_NAME, false));
        definition.setShowGrid(getBooleanAttribute(processDiagramInfo, SHOW_GRID_NAME, false));
    }

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.parseDocument(folder.getFile(GPD_FILE_NAME).getContents());
        Element processDiagramInfo = document.getDocumentElement();
        addProcessDiagramInfo(definition, processDiagramInfo);
        List<Element> children = getNamedChildren(processDiagramInfo, NODE_ELEMENT_NAME);
        for (Element element : children) {
            Node node = definition.getNodeByNameNotNull(element.getAttribute(NAME_ATTRIBUTE_NAME));
            Rectangle constraint = new Rectangle();
            constraint.x = Integer.valueOf(element.getAttribute(X_ATTRIBUTE_NAME)).intValue();
            constraint.y = Integer.valueOf(element.getAttribute(Y_ATTRIBUTE_NAME)).intValue();
            constraint.width = Integer.valueOf(element.getAttribute(WIDTH_ATTRIBUTE_NAME)).intValue();
            constraint.height = Integer.valueOf(element.getAttribute(HEIGHT_ATTRIBUTE_NAME)).intValue();
            String minimizedViewStr = element.getAttribute(MIN_VIEW_ATTRIBUTE_NAME);
            if (!isEmptyOrNull(minimizedViewStr) && node instanceof State) {
                boolean minimizedView = Boolean.parseBoolean(minimizedViewStr);
                ((State) node).setMinimizedView(minimizedView);
            }
            node.setConstraint(constraint);
            List<Transition> leavingTransitions = node.getLeavingTransitions();
            List<Element> transitionInfoList = getNamedChildren(element, TRANSITION_ELEMENT_NAME);
            for (int i = 0; i < leavingTransitions.size(); i++) {
                Element transitionElement = transitionInfoList.get(i);
                String transitionName = transitionElement.getAttribute(NAME_ATTRIBUTE_NAME);
                for (Transition transition : leavingTransitions) {
                    if (transition.getName().equals(transitionName)) {
                        List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();
                        List<Element> bendpointInfoList = getNamedChildren(transitionElement, BENDPOINT_ELEMENT_NAME);
                        for (Element bendpointElement : bendpointInfoList) {
                            try {
                                int x = Integer.valueOf(bendpointElement.getAttribute(X_ATTRIBUTE_NAME)).intValue();
                                int y = Integer.valueOf(bendpointElement.getAttribute(Y_ATTRIBUTE_NAME)).intValue();
                                bendpoints.add(new Bendpoint(x, y));
                            } catch (NumberFormatException e) {
                                DesignerLogger.logErrorWithoutDialog("Unable to parce bendpoint info for element " + bendpointElement, e);
                            }
                        }
                        transition.setBendpoints(bendpoints);
                        break;
                    }

                }
            }
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(PROCESS_DIAGRAM_ELEMENT_NAME, null);
        Element root = document.getDocumentElement();

        addAttribute(root, NAME_ATTRIBUTE_NAME, definition.getName());
        addAttribute(root, NOTATION_ATTRIBUTE_NAME, definition.getNotation());
        Dimension dimension = definition.getDimension();
        addAttribute(root, WIDTH_ATTRIBUTE_NAME, String.valueOf(dimension.width));
        addAttribute(root, HEIGHT_ATTRIBUTE_NAME, String.valueOf(dimension.height));
        addAttribute(root, SHOW_ACTIONS_NAME, String.valueOf(definition.isShowActions()));
        addAttribute(root, SHOW_GRID_NAME, String.valueOf(definition.isShowGrid()));

        int xOffset = 0;
        int yOffset = 0;
        // calculating negative offsets;
        for (Node node : definition.getNodes()) {
            Rectangle constraints = node.getConstraint();
            if (constraints.x < xOffset) {
                xOffset = constraints.x;
            }
            if (constraints.y < yOffset) {
                yOffset = constraints.y;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                for (Bendpoint bendpoint : transition.getBendpoints()) {
                    if (bendpoint.getX() < xOffset) {
                        xOffset = bendpoint.getX();
                    }
                    if (bendpoint.getY() < yOffset) {
                        yOffset = bendpoint.getY();
                    }
                }
            }
        }

        for (Node node : definition.getNodes()) {
            Element element = addElement(root, NODE_ELEMENT_NAME);
            addAttribute(element, NAME_ATTRIBUTE_NAME, node.getName());
            Rectangle constraint = node.getConstraint();
            if (constraint.width == 0 || constraint.height == 0) {
                throw new Exception("Invalid figure size: " + constraint.getSize());
            }
            addAttribute(element, X_ATTRIBUTE_NAME, String.valueOf(constraint.x - xOffset));
            addAttribute(element, Y_ATTRIBUTE_NAME, String.valueOf(constraint.y - yOffset));
            addAttribute(element, WIDTH_ATTRIBUTE_NAME, String.valueOf(constraint.width));
            addAttribute(element, HEIGHT_ATTRIBUTE_NAME, String.valueOf(constraint.height));
            if (node instanceof State) {
                boolean minimizedView = ((State) node).isMinimizedView();
                addAttribute(element, MIN_VIEW_ATTRIBUTE_NAME, String.valueOf(minimizedView));
            }
            for (Transition transition : node.getLeavingTransitions()) {
                Element transitionElement = addElement(element, TRANSITION_ELEMENT_NAME);
                String name = transition.getName();
                if (name != null) {
                    addAttribute(transitionElement, NAME_ATTRIBUTE_NAME, name);
                }
                for (Bendpoint bendpoint : transition.getBendpoints()) {
                    Element bendpointElement = addElement(transitionElement, BENDPOINT_ELEMENT_NAME);
                    addAttribute(bendpointElement, X_ATTRIBUTE_NAME, String.valueOf(bendpoint.getX() - xOffset));
                    addAttribute(bendpointElement, Y_ATTRIBUTE_NAME, String.valueOf(bendpoint.getY() - yOffset));
                }
            }
        }

        byte[] bytes = XmlUtil.writeXml(document);
        updateFile(folder.getFile(GPD_FILE_NAME), bytes);
    }

}
