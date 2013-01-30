package ru.runa.service.delegate;

import java.util.List;

import ru.runa.service.wf.BotService;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.User;

public class BotServiceDelegate extends EJB3Delegate implements BotService {

    public BotServiceDelegate() {
        super(BotService.class);
    }

    private BotService getBotService() {
        return (BotService) getService();
    }

    @Override
    public BotStation createBotStation(User user, BotStation bs) throws AuthorizationException, AuthenticationException,
            BotStationAlreadyExistsException {
        return getBotService().createBotStation(user, bs);
    }

    @Override
    public BotStation getBotStation(Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotStation(id);
    }

    @Override
    public BotStation getBotStation(String name) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotStation(name);
    }

    @Override
    public Bot getBot(User user, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBot(user, id);
    }

    @Override
    public List<BotStation> getBotStations() {
        return getBotService().getBotStations();
    }

    @Override
    public void removeBotStation(User user, Long id) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        getBotService().removeBotStation(user, id);
    }

    @Override
    public void removeBot(User user, Long id) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        getBotService().removeBot(user, id);
    }

    @Override
    public void updateBotStation(User user, BotStation bs) throws AuthorizationException, BotStationAlreadyExistsException {
        getBotService().updateBotStation(user, bs);
    }

    @Override
    public List<Bot> getBots(User user, Long botStationId) throws AuthorizationException, AuthenticationException {
        return getBotService().getBots(user, botStationId);
    }

    @Override
    public Bot createBot(User user, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        return getBotService().createBot(user, bot);
    }

    @Override
    public void updateBot(User user, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        getBotService().updateBot(user, bot);
    }

    @Override
    public List<BotTask> getBotTasks(User user, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotTasks(user, id);
    }

    @Override
    public BotTask createBotTask(User user, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        return getBotService().createBotTask(user, task);
    }

    @Override
    public void updateBotTask(User user, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        getBotService().updateBotTask(user, task);
    }

    @Override
    public void removeBotTask(User user, Long id) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        getBotService().removeBotTask(user, id);
    }

    @Override
    public BotTask getBotTask(User user, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotTask(user, id);
    }

    @Override
    public byte[] exportBot(User user, Bot bot) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        return getBotService().exportBot(user, bot);
    }

    @Override
    public byte[] exportBotStation(User user, BotStation station) throws AuthorizationException, BotStationDoesNotExistException {
        return getBotService().exportBotStation(user, station);
    }

    @Override
    public byte[] exportBotTask(User user, Bot bot, String botTaskName) throws BotDoesNotExistException {
        return getBotService().exportBotTask(user, bot, botTaskName);
    }

    @Override
    public void importBot(User user, BotStation station, byte[] archive, boolean replace) throws AuthorizationException,
            BotStationDoesNotExistException {
        getBotService().importBot(user, station, archive, replace);
    }

    @Override
    public void importBotStation(User user, byte[] archive, boolean replace) throws AuthorizationException {
        getBotService().importBotStation(user, archive, replace);
    }
}
