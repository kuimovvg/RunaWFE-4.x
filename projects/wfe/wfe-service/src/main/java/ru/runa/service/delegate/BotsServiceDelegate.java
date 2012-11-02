package ru.runa.service.delegate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.service.wf.BotsService;
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

public class BotsServiceDelegate extends EJB3Delegate implements BotsService {

    public BotsServiceDelegate() {
        super(BotsService.class);
    }

    private BotsService getBotsService() {
        return (BotsService) getService();
    }

    @Override
    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        return getBotsService().create(subject, bs);
    }

    @Override
    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotStation(subject, bs);
    }

    @Override
    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBot(subject, b);
    }

    @Override
    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotList(subject, b);
    }

    @Override
    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotStationList(subject);
    }

    @Override
    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        getBotsService().remove(subject, bs);
    }

    @Override
    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        getBotsService().remove(subject, b);
    }

    @Override
    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        return getBotsService().update(subject, bs);
    }

    @Override
    public List<Bot> getBotList(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotList(subject, bs);
    }

    @Override
    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        return getBotsService().create(subject, bot);
    }

    @Override
    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        return getBotsService().create(subject, bot);
    }

    @Override
    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotTaskList(subject, bot);
    }

    @Override
    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        return getBotsService().create(subject, task);
    }

    @Override
    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        return getBotsService().update(subject, task);
    }

    @Override
    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        getBotsService().remove(subject, task);
    }

    @Override
    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException {
        return getBotsService().getBotTask(subject, task);
    }

    @Override
    public void saveBot(Subject subject, Bot bot, OutputStream out) throws AuthorizationException, AuthenticationException, BotDoesNotExistException,
            IOException {
        getBotsService().saveBot(subject, bot, out);
    }

    @Override
    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws AuthorizationException, AuthenticationException,
            BotStationDoesNotExistException, IOException {
        getBotsService().saveBotStation(subject, station, out);
    }

    @Override
    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws AuthorizationException,
            AuthenticationException, BotStationDoesNotExistException, IOException {
        getBotsService().deployBot(subject, station, in, replace);
    }

    @Override
    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws AuthorizationException, AuthenticationException,
            IOException {
        getBotsService().deployBotStation(subject, in, replace);
    }
}
