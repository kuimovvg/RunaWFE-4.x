package ru.runa.gpd.wfe;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.BotStation;

public class WFEServerBotStationElementImporter extends DataImporter {
    private final List<BotStation> botStations = new ArrayList<BotStation>();
    private static WFEServerBotStationElementImporter instance;

    private WFEServerBotStationElementImporter() {
        super(WFEServerConnector.getInstance());
    }

    public static synchronized WFEServerBotStationElementImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerBotStationElementImporter();
        }
        return instance;
    }

    @Override
    public boolean hasCachedData() {
        return botStations.size() > 0;
    }

    @Override
    protected void clearInMemoryCache() {
        botStations.clear();
    }

    @Override
    public Object loadCachedData() throws Exception {
        return botStations;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        botStations.addAll(getBotService().getBotStations());
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public List<BotStation> getBotStations() {
        return botStations;
    }

    public byte[] getBotStationFile(BotStation botStation) throws Exception {
        return getBotService().exportBotStation(WFEServerConnector.getInstance().getSubject(), botStation);
    }

    public void deployBotStation(byte[] archive) throws Exception {
        WFEServerConnector.getInstance().connect();
        getBotService().importBotStation(WFEServerConnector.getInstance().getSubject(), archive, true);
    }

    private BotService getBotService() throws Exception {
        return WFEServerConnector.getInstance().getService("BotServiceBean");
    }
}
