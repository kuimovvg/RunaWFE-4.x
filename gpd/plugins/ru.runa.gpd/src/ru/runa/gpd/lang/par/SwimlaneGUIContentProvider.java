package ru.runa.gpd.lang.par;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.XmlUtil;

public class SwimlaneGUIContentProvider extends AuxContentProvider {
    public static final String XML_FILE_NAME = "swimlaneGUIconfig.xml";
    private static final String PATH = "guiElementPath";
    private static final String SWIMLANE = "swimlane";
    private static final String SWIMLANES = "swimlanes";

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
        List<Element> elementsList = document.getRootElement().elements(SWIMLANE);
        for (Element element : elementsList) {
            String swimlaneName = element.attributeValue(NAME);
            String path = element.attributeValue(PATH);
            definition.getSwimlaneGUIConfiguration().putSwimlanePath(swimlaneName, path);
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        Map<String, String> swimlanePaths = definition.getSwimlaneGUIConfiguration().getSwimlanePaths();
        if (swimlanePaths.size() > 0) {
            Document document = XmlUtil.createDocument(SWIMLANES);
            Element root = document.getRootElement();
            for (String swimlaneName : swimlanePaths.keySet()) {
                Element element = root.addElement(SWIMLANE);
                element.addAttribute(NAME, swimlaneName);
                element.addAttribute(PATH, swimlanePaths.get(swimlaneName));
            }
            return document;
        }
        return null;
    }
}
