package ru.runa.gpd.lang.par;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

public class SwimlaneGUIContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "swimlaneGUIconfig.xml";
    private static final String PATH_ATTRIBUTE_NAME = "guiElementPath";
    private static final String SWIMLANE_ELEMENT_NAME = "swimlane";
    private static final String SWIMLANES_ELEMENT_NAME = "swimlanes";

    @Override
    public boolean isSupportedForEmbeddedSubprocess() {
        return false;
    }

    @Override
    public String getFileName() {
        return XML_FILE_NAME;
    }

    @Override
    public void read(Document document, ProcessDefinition definition) throws Exception {
        List<Element> elementsList = document.getRootElement().elements(SWIMLANE_ELEMENT_NAME);
        for (Element element : elementsList) {
            String swimlaneName = element.attributeValue(NAME_ATTRIBUTE_NAME);
            String path = element.attributeValue(PATH_ATTRIBUTE_NAME);
            definition.getSwimlaneGUIConfiguration().putSwimlanePath(swimlaneName, path);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Map<String, String> swimlanePaths = definition.getSwimlaneGUIConfiguration().getSwimlanePaths();
        if (swimlanePaths.size() > 0) {
            Document document = XmlUtil.createDocument(SWIMLANES_ELEMENT_NAME);
            Element root = document.getRootElement();
            for (String swimlaneName : swimlanePaths.keySet()) {
                Element element = root.addElement(SWIMLANE_ELEMENT_NAME);
                element.addAttribute(NAME_ATTRIBUTE_NAME, swimlaneName);
                element.addAttribute(PATH_ATTRIBUTE_NAME, swimlanePaths.get(swimlaneName));
            }
            return document;
        }
        return null;
    }
}
