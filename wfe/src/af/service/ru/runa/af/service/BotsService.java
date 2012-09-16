package ru.runa.af.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.security.auth.Subject;

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

public interface BotsService {

    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException;

    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException;

    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException;

    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException;

    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistsException;

    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException;

    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException;

    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException;

    public List<Bot> getBotList(Subject subject, BotStation station) throws AuthorizationException, AuthenticationException;

    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException;

    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException;

    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistsException;

    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException;

    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException;

    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException;

    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException;

    public void saveBot(Subject subject, Bot bot, OutputStream out) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException;

    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws AuthorizationException, AuthenticationException,
            BotStationDoesNotExistsException;

    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws AuthorizationException,
            AuthenticationException, BotStationDoesNotExistsException, IOException;

    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws AuthorizationException, AuthenticationException,
            IOException;

}
