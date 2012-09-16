package ru.runa.bpm.ui.par;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.orgfunctions.SwimlaneGUIConfiguration;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SwimlaneGUIContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "swimlaneGUIconfig.xml";

    private static final String PATH_ATTRIBUTE_NAME = "guiElementPath";
    private static final String SWIMLANE_ELEMENT_NAME = "swimlane";
    private static final String SWIMLANES_ELEMENT_NAME = "swimlanes";

    @Override
    public void readFromFile(IFolder folder, ProcessDefinition definition) throws Exception {
        IFile file = folder.getFile(XML_FILE_NAME);
        SwimlaneGUIConfiguration configuration = new SwimlaneGUIConfiguration();
        if (file.exists()) {
            Document document = XmlUtil.parseDocument(file.getContents());
            NodeList elementsList = document.getDocumentElement().getElementsByTagName(SWIMLANE_ELEMENT_NAME);
            for (int j = 0; j < elementsList.getLength(); j++) {
                Element element = (Element) elementsList.item(j);
                String swimlaneName = element.getAttribute(NAME_ATTRIBUTE_NAME);
                String path = element.getAttribute(PATH_ATTRIBUTE_NAME);
                configuration.putSwimlanePath(swimlaneName, path);
            }
        }
        definition.setSwimlaneGUIConfiguration(configuration);
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(SWIMLANES_ELEMENT_NAME, null);
        Element root = document.getDocumentElement();
        Map<String, String> swimlanePaths = definition.getSwimlaneGUIConfiguration().getSwimlanePaths();
        for (String swimlaneName : swimlanePaths.keySet()) {
            Element element = document.createElement(SWIMLANE_ELEMENT_NAME);
            root.appendChild(element);
            element.setAttribute(NAME_ATTRIBUTE_NAME, swimlaneName);
            element.setAttribute(PATH_ATTRIBUTE_NAME, swimlanePaths.get(swimlaneName));
        }
        byte[] bytes = XmlUtil.writeXml(document);
        updateFile(folder.getFile(XML_FILE_NAME), bytes);
    }

}
