package ru.runa.service.delegate;

import java.util.List;

import javax.security.auth.Subject;

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

public class BotServiceDelegate extends EJB3Delegate implements BotService {

    public BotServiceDelegate() {
        super(BotService.class);
    }

    private BotService getBotsService() {
        return (BotService) getService();
    }

    @Override
    public BotStation createBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException,
            BotStationAlreadyExistsException {
        return getBotsService().createBotStation(subject, bs);
    }

    @Override
    public BotStation getBotStation(Long id) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotStation(id);
    }

    @Override
    public BotStation getBotStation(String name) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotStation(name);
    }

    @Override
    public Bot getBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBot(subject, id);
    }

    @Override
    public List<BotStation> getBotStations() {
        return getBotsService().getBotStations();
    }

    @Override
    public void removeBotStation(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        getBotsService().removeBotStation(subject, id);
    }

    @Override
    public void removeBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        getBotsService().removeBot(subject, id);
    }

    @Override
    public void updateBotStation(Subject subject, BotStation bs) throws AuthorizationException, BotStationAlreadyExistsException {
        getBotsService().updateBotStation(subject, bs);
    }

    @Override
    public List<Bot> getBots(Subject subject, Long botStationId) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBots(subject, botStationId);
    }

    @Override
    public Bot createBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        return getBotsService().createBot(subject, bot);
    }

    @Override
    public void updateBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        getBotsService().updateBot(subject, bot);
    }

    @Override
    public List<BotTask> getBotTasks(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotTasks(subject, id);
    }

    @Override
    public BotTask createBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        return getBotsService().createBotTask(subject, task);
    }

    @Override
    public void updateBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        getBotsService().updateBotTask(subject, task);
    }

    @Override
    public void removeBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        getBotsService().removeBotTask(subject, id);
    }

    @Override
    public BotTask getBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotTask(subject, id);
    }

    @Override
    public byte[] exportBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        return getBotsService().exportBot(subject, bot);
    }

    @Override
    public byte[] exportBotStation(Subject subject, BotStation station) throws AuthorizationException, BotStationDoesNotExistException {
        return getBotsService().exportBotStation(subject, station);
    }

    @Override
    public void importBot(Subject subject, BotStation station, byte[] archive, boolean replace) throws AuthorizationException,
            BotStationDoesNotExistException {
        getBotsService().importBot(subject, station, archive, replace);
    }

    @Override
    public void importBotStation(Subject subject, byte[] archive, boolean replace) throws AuthorizationException {
        getBotsService().importBotStation(subject, archive, replace);
    }
}
