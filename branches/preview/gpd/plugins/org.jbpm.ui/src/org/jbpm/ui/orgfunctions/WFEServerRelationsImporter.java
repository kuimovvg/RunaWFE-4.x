package ru.runa.bpm.ui.orgfunctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import ru.runa.bpm.ui.sync.DataImporter;
import ru.runa.bpm.ui.sync.WFEServerConnector;
import ru.runa.bpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.af.Relation;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.impl.interfaces.RelationService;
import ru.runa.af.service.impl.interfaces.RelationServiceHome;

public class WFEServerRelationsImporter extends DataImporter {

    private final List<String> relations = new ArrayList<String>();

    private static WFEServerRelationsImporter instance;

    private WFEServerRelationsImporter() {
        super(WFEServerConnector.getInstance());
    }

    public static synchronized WFEServerRelationsImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerRelationsImporter();
        }
        return instance;
    }

    @Override
    protected void clearInMemoryCache() {
        relations.clear();
    }

    @Override
    protected void saveCachedData() throws Exception {
        Document document = XmlUtil.createDocument("data", null);
        for (String name : relations) {
            Element element = document.createElement("relation");
            element.setAttribute("name", name);
            document.getDocumentElement().appendChild(element);
        }
        XmlUtil.writeXml(document, new FileOutputStream(getCacheFile()));
    }

    @Override
    public List<String> loadCachedData() throws Exception {
        List<String> result = new ArrayList<String>();
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            Document document = XmlUtil.parseDocument(new FileInputStream(cacheFile));
            NodeList nodeList = document.getElementsByTagName("relation");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String name = element.getAttribute("name");
                result.add(name);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        RelationService executorService = WFEServerConnector.getInstance().getService(RelationServiceHome.JNDI_NAME, RelationServiceHome.class);
        BatchPresentation batchPresentation = AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        List<Relation> loaded = executorService.getRelations(WFEServerConnector.getInstance().getSubject(),
                batchPresentation);
        for (Relation relation : loaded) {
            relations.add(relation.getName());
        }
        monitor.worked(100);
    }

}
