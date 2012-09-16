package ru.runa.af.service.impl.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Bot;
import ru.runa.af.BotAlreadyExistsException;
import ru.runa.af.BotDoesNotExistsException;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationAlreadyExistsException;
import ru.runa.af.BotStationDoesNotExistsException;
import ru.runa.af.BotTask;
import ru.runa.af.BotTaskAlreadyExistsException;
import ru.runa.af.BotTaskDoesNotExistsException;
import ru.runa.af.logic.BotsLogic;
import ru.runa.af.service.BotsServiceLocal;
import ru.runa.af.service.BotsServiceRemote;

/**
 * Implements BotsService as bean.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class BotsServiceBean implements BotsServiceLocal, BotsServiceRemote {
    @Autowired
    private BotsLogic botsLogic;

    @Override
    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bs);
        return botsLogic.create(subject, bs);
    }

    @Override
    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bs);
        return botsLogic.getBotStation(subject, bs);
    }

    @Override
    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(b);
        return botsLogic.getBot(subject, b);
    }

    @Override
    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(b);
        return botsLogic.getBotList(subject, b);
    }

    @Override
    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return botsLogic.getBotStationList(subject);
    }

    @Override
    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bs);
        botsLogic.remove(subject, bs);
    }

    @Override
    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(b);
        botsLogic.remove(subject, b);
    }

    @Override
    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bs);
        return botsLogic.update(subject, bs);
    }

    @Override
    public List<Bot> getBotList(Subject subject, BotStation station) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(station);
        return botsLogic.getBotList(subject, station);
    }

    @Override
    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bot);
        return botsLogic.create(subject, bot);
    }

    @Override
    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bot);
        return botsLogic.update(subject, bot);
    }

    @Override
    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bot);
        return botsLogic.getBotTaskList(subject, bot);
    }

    @Override
    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(task);
        return botsLogic.create(subject, task);
    }

    @Override
    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(task);
        return botsLogic.update(subject, task);
    }

    @Override
    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(task);
        botsLogic.remove(subject, task);
    }

    @Override
    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(task);
        return botsLogic.getBotTask(subject, task);
    }

    @Override
    public void saveBot(Subject subject, Bot bot, OutputStream out) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(bot);
        ArgumentsCommons.checkNotNull(out);
        botsLogic.saveBot(subject, bot, out);
    }

    @Override
    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws AuthorizationException, AuthenticationException,
            BotStationDoesNotExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(station);
        ArgumentsCommons.checkNotNull(out);
        botsLogic.saveBotStation(subject, station, out);
    }

    @Override
    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws AuthorizationException,
            AuthenticationException, BotStationDoesNotExistsException, IOException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(station);
        ArgumentsCommons.checkNotNull(in);
        botsLogic.deployBot(subject, station, in, replace);
    }

    @Override
    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws AuthorizationException, AuthenticationException,
            IOException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(in);
        botsLogic.deployBotStation(subject, in, replace);
    }
}
