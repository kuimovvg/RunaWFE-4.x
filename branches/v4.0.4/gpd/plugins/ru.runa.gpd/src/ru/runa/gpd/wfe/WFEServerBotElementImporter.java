package ru.runa.gpd.wfe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.service.BotService;

public class WFEServerBotElementImporter extends DataImporter {
    private final Map<Bot, List<BotTask>> bots = new HashMap<Bot, List<BotTask>>();
    private static WFEServerBotElementImporter instance;

    private WFEServerBotElementImporter() {
        super(WFEServerConnector.getInstance());
    }

    public static synchronized WFEServerBotElementImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerBotElementImporter();
        }
        return instance;
    }

    @Override
    public boolean hasCachedData() {
        return bots.size() > 0;
    }

    @Override
    protected void clearInMemoryCache() {
        bots.clear();
    }

    @Override
    public Object loadCachedData() throws Exception {
        return bots;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        List<BotStation> botStations = getBotService().getBotStations();
        for (BotStation botStation : botStations) {
            for (Bot bot : getBotService().getBots(WFEServerConnector.getInstance().getUser(), botStation.getId())) {
                List<BotTask> result = getBotService().getBotTasks(WFEServerConnector.getInstance().getUser(), bot.getId());
                bots.put(bot, result);
            }
        }
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public List<Bot> getBots() {
        List<Bot> result = new ArrayList<Bot>();
        if (bots.keySet() != null && bots.keySet().size() > 0) {
            result.addAll(bots.keySet());
        }
        return result;
    }

    public List<BotTask> getBotTasks() {
        List<BotTask> result = new ArrayList<BotTask>();
        for (List<BotTask> botTaskArray : bots.values()) {
            result.addAll(botTaskArray);
        }
        return result;
    }

    public List<BotTask> getBotTasks(Bot bot) {
        return bots.get(bot);
    }

    public byte[] getBotFile(Bot bot) throws Exception {
        return getBotService().exportBot(WFEServerConnector.getInstance().getUser(), bot);
    }

    public byte[] getBotTaskFile(Bot bot, String botTask) throws Exception {
        return getBotService().exportBotTask(WFEServerConnector.getInstance().getUser(), bot, botTask);
    }

    public void deployBot(String botStationName, byte[] archive) {
        WFEServerConnector.getInstance().connect();
        BotStation botStation = getBotService().getBotStationByName(botStationName);
        if (botStation == null) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Localization.getString("ExportBotWizardPage.page.title"),
                    Localization.getString("ExportBotWizardPage.page.notExistWarning"));
            return;
        }
        getBotService().importBot(WFEServerConnector.getInstance().getUser(), botStation, archive, true);
    }

    private BotService getBotService() {
        return WFEServerConnector.getInstance().getService("BotServiceBean");
    }
}
