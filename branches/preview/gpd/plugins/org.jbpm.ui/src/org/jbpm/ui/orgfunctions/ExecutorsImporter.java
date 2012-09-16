package ru.runa.bpm.ui.orgfunctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.TreeMap;

import ru.runa.bpm.ui.sync.DataImporter;
import ru.runa.bpm.ui.sync.IConnector;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class ExecutorsImporter extends DataImporter {

    protected Map<String, Boolean> executors = new TreeMap<String, Boolean>();

    public ExecutorsImporter(IConnector connector) {
        super(connector);
    }

    @Override
    protected void clearInMemoryCache() {
        executors.clear();
    }

    @Override
    protected void saveCachedData() throws Exception {
        Document document = XmlUtil.createDocument("executors", null);
        for (String name : executors.keySet()) {
            Element element = document.createElement("executor");
            element.setAttribute("name", name);
            element.setAttribute("group", String.valueOf(executors.get(name)));
            document.getDocumentElement().appendChild(element);
        }
        XmlUtil.writeXml(document, new FileOutputStream(getCacheFile()));
    }

    @Override
    public Map<String, Boolean> loadCachedData() throws Exception {
        Map<String, Boolean> result = new TreeMap<String, Boolean>();
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            Document document = XmlUtil.parseDocument(new FileInputStream(cacheFile));
            NodeList nodeList = document.getElementsByTagName("executor");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String name = element.getAttribute("name");
                Boolean isGroup = Boolean.parseBoolean(element.getAttribute("group"));
                result.put(name, isGroup);
            }
        }
        return result;
    }

}
