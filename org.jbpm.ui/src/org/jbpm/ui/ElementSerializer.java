package org.jbpm.ui;

import java.util.List;
import java.util.Map;

import org.jbpm.ui.common.model.Action;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.common.model.Describable;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.NamedGraphElement;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Transition;
import org.jbpm.ui.jpdl3.model.ActionNode;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// TODO use dom4j & refactor
public abstract class ElementSerializer {

    private static final String TO_ATTR = "to";

    private static final String CLASS_ATTR = "class";

    private static final String ACTION_NODE = "action";

    protected static final String DESCRIPTION_NODE = "description";

    protected static final String NAME_ATTR = "name";
    protected static final String TRANSITION_NODE = "transition";

    public abstract void parseFromXml(Document document, ProcessDefinition definition, Map<Transition, String> transitionTargets);

    public abstract void writeToXml(Document document, ProcessDefinition definition);

    protected void writeTransitions(Document document, Element parent, org.jbpm.ui.common.model.Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = writeElement(document, parent, transition);
            transitionElement.setAttribute(TO_ATTR, transition.getTargetName());
            for (Action action : transition.getActions()) {
                writeDelegation(document, transitionElement, ACTION_NODE, action);
            }
        }
    }

    protected Element writeElement(Document document, Element parent, GraphElement element) {
        return writeElement(document, parent, element, element.getTypeName());
    }

    protected Element writeElement(Document document, Element parent, GraphElement element, String typeName) {
        Element result = document.createElement(typeName);
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME_ATTR, ((NamedGraphElement) element).getName());
        }
        if (element instanceof ActionNode) {
            List<Action> nodeActions = ((ActionNode) element).getNodeActions();
            for (Action nodeAction : nodeActions) {
                writeDelegation(document, result, ACTION_NODE, nodeAction);
            }
        }
        if (element instanceof Describable) {
            String description = ((Describable) element).getDescription();
            if (description != null && description.length() > 0) {
                Element desc = document.createElement(DESCRIPTION_NODE);
                setNodeValue(desc, description);
                result.appendChild(desc);
            }
        }
        if (parent != null) {
            parent.appendChild(result);
        }
        return result;
    }

    protected void parseTransition(Node node, GraphElement parent, Map<Transition, String> transitionTargets) {
        Transition transition = new Transition();
        transition.setTypeName(node.getNodeName());
        parent.addChild(transition);
        transition.setName(getAttribute(node, NAME_ATTR));
        String targetName = getAttribute(node, TO_ATTR);
        transitionTargets.put(transition, targetName);
    }

    protected void writeDelegation(Document document, Element parent, String elementName, Delegable delegable) {
        Element delegationElement = document.createElement(elementName);
        setAttribute(delegationElement, CLASS_ATTR, delegable.getDelegationClassName());
        setAttribute(delegationElement, "config-type", "configuration-property");
        setNodeValue(delegationElement, delegable.getDelegationConfiguration());
        parent.appendChild(delegationElement);
    }

    protected Element writeNode(Document document, Element parent, org.jbpm.ui.common.model.Node node, String delegationNodeName) {
        Element nodeElement = writeElement(document, parent, node);
        if (delegationNodeName != null) {
            writeDelegation(document, nodeElement, delegationNodeName, (Delegable) node);
        }
        writeTransitions(document, nodeElement, node);
        return nodeElement;
    }

    protected String getAttribute(Node node, String name) {
        Node attr = node.getAttributes().getNamedItem(name);
        if (attr != null) {
            return attr.getNodeValue();
        }
        return null;
    }

    protected String getTextContent(Node node) {
        if (node.getChildNodes().getLength() == 1) {
            return node.getFirstChild().getNodeValue();
        }
        return null;
    }

    protected void setAttribute(Element node, String attributeName, String attributeValue) {
        if (attributeValue != null) {
            node.setAttribute(attributeName, attributeValue);
        }
    }

    protected void setNodeValue(Element node, String nodeValue) {
        if (nodeValue != null) {
            CDATASection section = node.getOwnerDocument().createCDATASection(nodeValue);
            node.appendChild(section);
        }
    }

}
