package ru.runa.service.wf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;

public interface BotsService {

    public BotStation getBotStation(Subject subject, BotStation bs);

    public List<BotStation> getBotStationList(Subject subject);

    public BotStation create(Subject subject, BotStation bs) throws BotStationAlreadyExistsException;

    public BotStation update(Subject subject, BotStation bs) throws BotStationAlreadyExistsException;

    public void remove(Subject subject, BotStation bs) throws BotStationDoesNotExistException;

    public void remove(Subject subject, Bot b) throws BotDoesNotExistException;

    public Bot getBot(Subject subject, Bot b);

    public List<Bot> getBotList(Subject subject, Bot b);

    public List<Bot> getBotList(Subject subject, BotStation station);

    public Bot create(Subject subject, Bot bot) throws BotAlreadyExistsException;

    public Bot update(Subject subject, Bot bot) throws BotAlreadyExistsException;

    public void remove(Subject subject, BotTask task) throws BotTaskDoesNotExistException;

    public BotTask getBotTask(Subject subject, BotTask task);

    public List<BotTask> getBotTaskList(Subject subject, Bot bot);

    public BotTask create(Subject subject, BotTask task) throws BotTaskAlreadyExistsException;

    public BotTask update(Subject subject, BotTask task) throws BotTaskAlreadyExistsException;

    public void saveBot(Subject subject, Bot bot, OutputStream out) throws BotDoesNotExistException, IOException;

    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws BotStationDoesNotExistException, IOException;

    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws BotStationDoesNotExistException, IOException;

    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws IOException;

}
