package ru.runa.bpm.ui.sync;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import ru.runa.bpm.ui.DesignerLogger;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.impl.interfaces.DefinitionService;
import ru.runa.wf.service.impl.interfaces.DefinitionServiceHome;

public class WFEServerProcessDefinitionImporter extends DataImporter {

    private final Map<ProcessDefinitionStub, ProcessDefinitionStub[]> definitions = new HashMap<ProcessDefinitionStub, ProcessDefinitionStub[]>();

    private static WFEServerProcessDefinitionImporter instance;

    private WFEServerProcessDefinitionImporter() {
        super(WFEServerConnector.getInstance());
    }

    public static synchronized WFEServerProcessDefinitionImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerProcessDefinitionImporter();
        }
        return instance;
    }

    @Override
    public boolean hasCachedData() {
        return definitions.size() > 0;
    }

    @Override
    protected void clearInMemoryCache() {
        definitions.clear();
    }

    @Override
    public Map<ProcessDefinitionStub, ProcessDefinitionStub[]> loadCachedData() {
        return definitions;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        DefinitionService definitionService = getDefinitionService();
        BatchPresentation batch = WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation();
        batch.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        ProcessDefinitionStub[] latests = definitionService.getLatestProcessDefinitionStubs(WFEServerConnector.getInstance().getSubject(), batch);
        monitor.worked(30);
        double perDefinition = (double) 70 / latests.length;
        for (ProcessDefinitionStub latest : latests) {
            ProcessDefinitionStub[] historyDefinitionStubs;
            try {
                historyDefinitionStubs = definitionService.getProcessDefinitionHistory(WFEServerConnector.getInstance().getSubject(), latest
                        .getName());
            } catch (Exception e) {
                DesignerLogger.logErrorWithoutDialog("definitions sync", e);
                historyDefinitionStubs = new ProcessDefinitionStub[0];
            }
            definitions.put(latest, historyDefinitionStubs);
            monitor.internalWorked(perDefinition);
        }
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public byte[] loadPar(ProcessDefinitionStub definition) throws Exception {
        WFEServerConnector.getInstance().connect();
        return getDefinitionService().getFile(WFEServerConnector.getInstance().getSubject(), definition.getNativeId(), "par");
    }

    public void uploadPar(String definitionName, byte[] par) throws Exception {
        ProcessDefinitionStub oldVersion = null;
        if (!hasCachedData()) {
            synchronize();
        } else {
            WFEServerConnector.getInstance().connect();
        }
        for (ProcessDefinitionStub stub : definitions.keySet()) {
            if (definitionName.equals(stub.getName())) {
                oldVersion = stub;
                break;
            }
        }
        ProcessDefinitionStub lastDefinition;
        ProcessDefinitionStub[] lastHistory;
        if (oldVersion != null) {
            String[] types = oldVersion.getType();
            if (types == null) {
                types = new String[] { "GPD" };
            }
            try {
                lastDefinition = getDefinitionService().redeployProcessDefinition(WFEServerConnector.getInstance().getSubject(),
                        oldVersion.getNativeId(), par, types);
                ProcessDefinitionStub[] oldHistory = definitions.remove(oldVersion);
                lastHistory = new ProcessDefinitionStub[oldHistory.length + 1];
                System.arraycopy(oldHistory, 0, lastHistory, 1, oldHistory.length);
                lastHistory[0] = oldVersion;
            } catch (ProcessDefinitionDoesNotExistException e) {
                lastDefinition = getDefinitionService().deployProcessDefinition(WFEServerConnector.getInstance().getSubject(), par,
                        new String[] { "GPD" });
                lastHistory = new ProcessDefinitionStub[0];
            }
        } else {
            lastDefinition = getDefinitionService().deployProcessDefinition(WFEServerConnector.getInstance().getSubject(), par,
                    new String[] { "GPD" });
            lastHistory = new ProcessDefinitionStub[0];
        }
        definitions.put(lastDefinition, lastHistory);
    }

    private DefinitionService getDefinitionService() throws Exception {
        return WFEServerConnector.getInstance().getService(DefinitionServiceHome.JNDI_NAME, DefinitionServiceHome.class);
    }

}
