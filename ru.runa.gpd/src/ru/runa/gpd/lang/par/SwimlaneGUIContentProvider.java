package ru.runa.gpd.lang.par;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.swimlane.SwimlaneGUIConfiguration;
import ru.runa.gpd.util.XmlUtil;

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
            Document document = XmlUtil.parseWithoutValidation(file.getContents());
            List<Element> elementsList = document.getRootElement().elements(SWIMLANE_ELEMENT_NAME);
            for (Element element : elementsList) {
                String swimlaneName = element.attributeValue(NAME_ATTRIBUTE_NAME);
                String path = element.attributeValue(PATH_ATTRIBUTE_NAME);
                configuration.putSwimlanePath(swimlaneName, path);
            }
        }
        definition.setSwimlaneGUIConfiguration(configuration);
    }

    @Override
    public void saveToFile(IFolder folder, ProcessDefinition definition) throws Exception {
        Document document = XmlUtil.createDocument(SWIMLANES_ELEMENT_NAME);
        Element root = document.getRootElement();
        Map<String, String> swimlanePaths = definition.getSwimlaneGUIConfiguration().getSwimlanePaths();
        byte[] bytes = null;
        if (swimlanePaths.size() > 0) {
            for (String swimlaneName : swimlanePaths.keySet()) {
                Element element = root.addElement(SWIMLANE_ELEMENT_NAME);
                element.addAttribute(NAME_ATTRIBUTE_NAME, swimlaneName);
                element.addAttribute(PATH_ATTRIBUTE_NAME, swimlanePaths.get(swimlaneName));
            }
            bytes = XmlUtil.writeXml(document);
        }
        updateFile(folder.getFile(XML_FILE_NAME), bytes);
    }
}
