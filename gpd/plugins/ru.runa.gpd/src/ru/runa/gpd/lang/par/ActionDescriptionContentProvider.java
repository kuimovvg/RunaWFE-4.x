package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

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
            Document document = XmlUtil.parseWithoutValidation(file.getContents());
            List<Element> elementsList = document.getRootElement().elements(ELEMENT_NAME);
            for (Element element : elementsList) {
                String path = element.attributeValue(PATH_ATTRIBUTE_NAME);
                String description = element.attributeValue(DESC_ATTRIBUTE_NAME);
                Action action = findByPath(definition, path);
                if (action != null) {
                    action.setDescription(description);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("Unable to apply " + XML_FILE_NAME, e);
        }
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) {
        try {
            Document document = XmlUtil.createDocument(CONTAINER_ELEMENT_NAME);
            Element root = document.getRootElement();
            boolean atLeastOneDescExists = false;
            for (Action action : definition.getChildrenRecursive(Action.class)) {
                String desc = action.getDescription();
                if (desc != null && desc.trim().length() > 0) {
                    atLeastOneDescExists = true;
                    Element element = root.addElement(ELEMENT_NAME);
                    element.addAttribute(PATH_ATTRIBUTE_NAME, getPath(action));
                    element.addAttribute(DESC_ATTRIBUTE_NAME, desc);
                }
            }
            if (atLeastOneDescExists) {
                byte[] bytes = XmlUtil.writeXml(document);
                updateFile(folder.getFile(XML_FILE_NAME), bytes);
            } else {
                deleteFile(folder.getFile(XML_FILE_NAME));
            }
        } catch (Exception e) {
            PluginLogger.logError("Unable to write " + XML_FILE_NAME, e);
        }
    }

    private Action findByPath(ProcessDefinition definition, String path) {
        try {
            String[] components = path.split(DELIM, -1);
            GraphElement element = definition;
            if (components.length > 2) {
                for (int i = 1; i < components.length - 1; i++) {
                    String name = components[i];
                    for (NamedGraphElement e : element.getChildren(NamedGraphElement.class)) {
                        if (name.equals(e.getName())) {
                            element = e;
                            break;
                        }
                    }
                }
            }
            String actionName = components[components.length - 1];
            int actionIndex = Integer.parseInt(actionName.substring(ACTION_INDEX.length()));
            if (element.getActions().size() > actionIndex) {
                return element.getActions().get(actionIndex);
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("findByPath " + path + " in " + definition, e);
        }
        return null;
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
