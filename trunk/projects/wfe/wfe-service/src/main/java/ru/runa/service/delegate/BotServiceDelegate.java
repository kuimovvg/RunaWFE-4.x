package ru.runa.service.delegate;

import java.util.List;

import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.user.User;

public class BotServiceDelegate extends EJB3Delegate implements BotService {

    public BotServiceDelegate() {
        super(BotService.class);
    }

    private BotService getBotService() {
        return (BotService) getService();
    }

    @Override
    public BotStation createBotStation(User user, BotStation bs) {
        return getBotService().createBotStation(user, bs);
    }

    @Override
    public BotStation getBotStation(Long id) {
        return getBotService().getBotStation(id);
    }

    @Override
    public BotStation getBotStationByName(String name) {
        return getBotService().getBotStationByName(name);
    }

    @Override
    public Bot getBot(User user, Long id) {
        return getBotService().getBot(user, id);
    }

    @Override
    public List<BotStation> getBotStations() {
        return getBotService().getBotStations();
    }

    @Override
    public void removeBotStation(User user, Long id) {
        getBotService().removeBotStation(user, id);
    }

    @Override
    public void removeBot(User user, Long id) {
        getBotService().removeBot(user, id);
    }

    @Override
    public void updateBotStation(User user, BotStation bs) {
        getBotService().updateBotStation(user, bs);
    }

    @Override
    public List<Bot> getBots(User user, Long botStationId) {
        return getBotService().getBots(user, botStationId);
    }

    @Override
    public Bot createBot(User user, Bot bot) {
        return getBotService().createBot(user, bot);
    }

    @Override
    public void updateBot(User user, Bot bot) {
        getBotService().updateBot(user, bot);
    }

    @Override
    public List<BotTask> getBotTasks(User user, Long id) {
        return getBotService().getBotTasks(user, id);
    }

    @Override
    public BotTask createBotTask(User user, BotTask task) {
        return getBotService().createBotTask(user, task);
    }

    @Override
    public void updateBotTask(User user, BotTask task) {
        getBotService().updateBotTask(user, task);
    }

    @Override
    public void removeBotTask(User user, Long id) {
        getBotService().removeBotTask(user, id);
    }

    @Override
    public BotTask getBotTask(User user, Long id) {
        return getBotService().getBotTask(user, id);
    }

    @Override
    public byte[] exportBot(User user, Bot bot) {
        return getBotService().exportBot(user, bot);
    }

    @Override
    public byte[] exportBotStation(User user, BotStation station) {
        return getBotService().exportBotStation(user, station);
    }

    @Override
    public byte[] exportBotTask(User user, Bot bot, String botTaskName) {
        return getBotService().exportBotTask(user, bot, botTaskName);
    }

    @Override
    public void importBot(User user, BotStation station, byte[] archive, boolean replace) {
        getBotService().importBot(user, station, archive, replace);
    }

    @Override
    public void importBotStation(User user, byte[] archive, boolean replace) {
        getBotService().importBotStation(user, archive, replace);
    }
}
