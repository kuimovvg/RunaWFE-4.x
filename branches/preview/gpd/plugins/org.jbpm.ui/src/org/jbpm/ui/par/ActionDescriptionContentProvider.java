package ru.runa.bpm.ui.par;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.NamedGraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ActionDescriptionContentProvider extends AuxContentProvider {
    private static final String DELIM = "/";
    private static final String ACTION_INDEX = "actionIndex";
    private static final String XML_FILE_NAME = "actionDescription.xml";
    private static final String PATH_ATTRIBUTE_NAME = "path";
    private static final String DESC_ATTRIBUTE_NAME = "description";
    private static final String ELEMENT_NAME = "action";
    private static final String CONTAINER_ELEMENT_NAME = "actions";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) {
        try {
            IFile file = folder.getFile(XML_FILE_NAME);
            if (!file.exists()) {
                return;
            }
            Document document = XmlUtil.parseDocument(file.getContents());
            NodeList elementsList = document.getDocumentElement().getElementsByTagName(ELEMENT_NAME);
            for (int j = 0; j < elementsList.getLength(); j++) {
                Element element = (Element) elementsList.item(j);
                String path = element.getAttribute(PATH_ATTRIBUTE_NAME);
                String description = element.getAttribute(DESC_ATTRIBUTE_NAME);
                Action action = findByPath(definition, path);
                if (action != null) {
                    action.setDescription(description);
                }
            }
        } catch (Exception e) {
            DesignerLogger.logError("Unable to apply " + XML_FILE_NAME, e);
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) {
        try {
            Document document = XmlUtil.createDocument(CONTAINER_ELEMENT_NAME, null);
            Element root = document.getDocumentElement();
            boolean atLeastOneDescExists = false;
            for (Action action : definition.getChildrenRecursive(Action.class)) {
                String desc = action.getDescription();
                if (desc != null && desc.trim().length() > 0) {
                    atLeastOneDescExists = true;
                    Element element = document.createElement(ELEMENT_NAME);
                    root.appendChild(element);
                    element.setAttribute(PATH_ATTRIBUTE_NAME, getPath(action));
                    element.setAttribute(DESC_ATTRIBUTE_NAME, desc);
                }
            }
            if (atLeastOneDescExists) {
                byte[] bytes = XmlUtil.writeXml(document);
                updateFile(folder.getFile(XML_FILE_NAME), bytes);
            } else {
                deleteFile(folder.getFile(XML_FILE_NAME));
            }
        } catch (Exception e) {
            DesignerLogger.logError("Unable to write " + XML_FILE_NAME, e);
        }
    }
    
    private Action findByPath(ProcessDefinition definition, String path) {
        try {
            String[] components = path.split(DELIM, -1);
            GraphElement element = definition;
            if (components.length > 2) {
                for (int i = 1; i < components.length-1; i++) {
                    String name = components[i];
                    for (NamedGraphElement e : element.getChildren(NamedGraphElement.class)) {
                        if (name.equals(e.getName())) {
                            element = e;
                            break;
                        }
                    }
                }
            }
            String actionName = components[components.length-1];
            int actionIndex = Integer.parseInt(actionName.substring(ACTION_INDEX.length()));
            return element.getActions().get(actionIndex);
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("findByPath " + path + " in " + definition, e);
            return null;
        }
    }
    
    private String getPath(GraphElement element) {
        if (element instanceof ProcessDefinition) {
            return "";
        }
        String name;
        if (element instanceof NamedGraphElement) {
            name = ((NamedGraphElement) element).getName();
        } else if (element instanceof Action) {
            Action action = (Action) element;
            name = ACTION_INDEX + action.getParent().getActions().indexOf(action);
        } else {
            throw new IllegalArgumentException("" + element);
        }
        return getPath(element.getParent()) + DELIM + name;
    }
}
