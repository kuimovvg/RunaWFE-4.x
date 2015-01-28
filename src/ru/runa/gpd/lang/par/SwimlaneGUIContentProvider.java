package ru.runa.gpd.lang.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

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
            Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
            if (swimlane != null) {
                swimlane.setEditorPath(element.attributeValue(PATH));
            }
        }
    }

    @Override
    public Document save(ProcessDefinition definition) throws Exception {
        return null;
    }
}
