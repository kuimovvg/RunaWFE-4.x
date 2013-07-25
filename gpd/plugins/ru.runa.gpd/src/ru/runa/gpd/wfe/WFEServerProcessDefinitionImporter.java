package ru.runa.gpd.wfe;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WFEServerProcessDefinitionImporter extends DataImporter {
    private final Map<WfDefinition, List<WfDefinition>> definitions = Maps.newHashMap();
    private static WFEServerProcessDefinitionImporter instance;

    @Override
    protected WFEServerConnector getConnector() {
        return WFEServerConnector.getInstance();
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
    public Map<WfDefinition, List<WfDefinition>> loadCachedData() {
        return definitions;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        definitions.putAll(getConnector().getProcessDefinitions(monitor));
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public byte[] loadPar(WfDefinition definition) throws Exception {
        return getConnector().getProcessDefinitionArchive(definition);
    }

    public void uploadPar(String definitionName, byte[] par) {
        WfDefinition oldVersion = null;
        if (!hasCachedData()) {
            synchronize();
            //        } else {
            //            WFEServerConnector.getInstance().connect();
        }
        for (WfDefinition definition : definitions.keySet()) {
            if (definitionName.equals(definition.getName())) {
                oldVersion = definition;
                break;
            }
        }
        WfDefinition lastDefinition;
        List<WfDefinition> lastHistory;
        if (oldVersion != null) {
            String[] types = oldVersion.getCategories();
            if (types == null) {
                types = new String[] { "GPD" };
            }
            try {
                lastDefinition = getConnector().redeployProcessDefinitionArchive(oldVersion.getId(), par, Lists.newArrayList(types));
                List<WfDefinition> oldHistory = definitions.remove(oldVersion);
                lastHistory = Lists.newArrayList(oldVersion);
                lastHistory.addAll(oldHistory);
            } catch (DefinitionDoesNotExistException e) {
                lastDefinition = getConnector().deployProcessDefinitionArchive(par);
                lastHistory = Lists.newArrayList();
            }
        } else {
            lastDefinition = getConnector().deployProcessDefinitionArchive(par);
            lastHistory = Lists.newArrayList();
        }
        definitions.put(lastDefinition, lastHistory);
    }
}
