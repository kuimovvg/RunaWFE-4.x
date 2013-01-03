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

    private BotService getBotService() {
        return (BotService) getService();
    }

    @Override
    public BotStation createBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException,
            BotStationAlreadyExistsException {
        return getBotService().createBotStation(subject, bs);
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
    public Bot getBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBot(subject, id);
    }

    @Override
    public List<BotStation> getBotStations() {
        return getBotService().getBotStations();
    }

    @Override
    public void removeBotStation(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        getBotService().removeBotStation(subject, id);
    }

    @Override
    public void removeBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        getBotService().removeBot(subject, id);
    }

    @Override
    public void updateBotStation(Subject subject, BotStation bs) throws AuthorizationException, BotStationAlreadyExistsException {
        getBotService().updateBotStation(subject, bs);
    }

    @Override
    public List<Bot> getBots(Subject subject, Long botStationId) throws AuthorizationException, AuthenticationException {
        return getBotService().getBots(subject, botStationId);
    }

    @Override
    public Bot createBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        return getBotService().createBot(subject, bot);
    }

    @Override
    public void updateBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        getBotService().updateBot(subject, bot);
    }

    @Override
    public List<BotTask> getBotTasks(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotTasks(subject, id);
    }

    @Override
    public BotTask createBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        return getBotService().createBotTask(subject, task);
    }

    @Override
    public void updateBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        getBotService().updateBotTask(subject, task);
    }

    @Override
    public void removeBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        getBotService().removeBotTask(subject, id);
    }

    @Override
    public BotTask getBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        return getBotService().getBotTask(subject, id);
    }

    @Override
    public byte[] exportBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        return getBotService().exportBot(subject, bot);
    }

    @Override
    public byte[] exportBotStation(Subject subject, BotStation station) throws AuthorizationException, BotStationDoesNotExistException {
        return getBotService().exportBotStation(subject, station);
    }

    @Override
    public byte[] exportBotTask(Subject subject, Bot bot, String botTaskName) throws BotDoesNotExistException {
        return getBotService().exportBotTask(subject, bot, botTaskName);
    }

    @Override
    public void importBot(Subject subject, BotStation station, byte[] archive, boolean replace) throws AuthorizationException,
            BotStationDoesNotExistException {
        getBotService().importBot(subject, station, archive, replace);
    }

    @Override
    public void importBotStation(Subject subject, byte[] archive, boolean replace) throws AuthorizationException {
        getBotService().importBotStation(subject, archive, replace);
    }
}
