package ru.runa.service.wf;

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

/**
 * Service for operations with {@link BotStation}, {@link Bot}, {@link BotTask}.
 * 
 * @author dofs
 * @since 3.0
 */
public interface BotService {

    /**
     * Create bot station.
     * 
     * @return created bot station
     * @throws BotStationAlreadyExistsException
     *             if name collision occurs
     */
    public BotStation createBotStation(Subject subject, BotStation botStation) throws BotStationAlreadyExistsException;

    /**
     * Get all bot stations.
     * 
     * @return list, not <code>null</code>
     */
    public List<BotStation> getBotStations();

    /**
     * Finds bot station by id.
     * 
     * @return bot station, not <code>null</code>
     * @throws BotStationDoesNotExistException
     */
    public BotStation getBotStation(Long id) throws BotStationDoesNotExistException;;

    /**
     * Finds bot station by name.
     * 
     * @return bot station or <code>null</code>
     */
    public BotStation getBotStation(String name);

    /**
     * Updates bot station data.
     * 
     * @throws BotStationAlreadyExistsException
     *             if name collision occurs
     */
    public void updateBotStation(Subject subject, BotStation bs) throws BotStationAlreadyExistsException;

    /**
     * Removes bot station with all bots and bot tasks by id.
     * 
     * @throws BotStationDoesNotExistException
     */
    public void removeBotStation(Subject subject, Long id) throws BotStationDoesNotExistException;

    /**
     * Imports bot station with all bots and bot tasks from archive.
     * 
     * @param in
     *            archive stream
     * @param replace
     *            override existing entities
     */
    public void importBotStation(Subject subject, byte[] archive, boolean replace);

    /**
     * Exports bot station to archive
     * 
     * @return archive
     * @throws BotStationDoesNotExistException
     */
    public byte[] exportBotStation(Subject subject, BotStation station) throws BotStationDoesNotExistException;

    /**
     * Creates new bot.
     * 
     * @return created bot
     * @throws BotAlreadyExistsException
     *             if name collision occurs
     */
    public Bot createBot(Subject subject, Bot bot) throws BotAlreadyExistsException;

    /**
     * Loads all bots for bot station
     * 
     * @return list, not <code>null</code>
     */
    public List<Bot> getBots(Subject subject, Long botStationId);

    /**
     * Gets bot by id.
     * 
     * @return bot, not <code>null</code>
     * @throws BotDoesNotExistException
     */
    public Bot getBot(Subject subject, Long id) throws BotDoesNotExistException;

    /**
     * Updates bot data.
     * 
     * @throws BotAlreadyExistsException
     *             if name collision occurs
     */
    public void updateBot(Subject subject, Bot bot) throws BotAlreadyExistsException;

    /**
     * Removes bot with all bot tasks by id.
     * 
     * @throws BotDoesNotExistException
     */
    public void removeBot(Subject subject, Long id) throws BotDoesNotExistException;

    /**
     * Exports bot to archive.
     * 
     * @return archive
     * @throws BotDoesNotExistException
     */
    public byte[] exportBot(Subject subject, Bot bot) throws BotDoesNotExistException;

    /**
     * Imports bot from archive.
     * 
     * @param replace
     *            override existing entities
     * @throws BotStationDoesNotExistException
     */
    public void importBot(Subject subject, BotStation station, byte[] archive, boolean replace) throws BotStationDoesNotExistException;

    /**
     * Creates bot task.
     * 
     * @return created task
     * @throws BotTaskAlreadyExistsException
     *             if name collision occurs
     */
    public BotTask createBotTask(Subject subject, BotTask task) throws BotTaskAlreadyExistsException;

    /**
     * Loads all bot tasks by bot id.
     * 
     * @return list, not <code>null</code>
     */
    public List<BotTask> getBotTasks(Subject subject, Long id);

    /**
     * Loads bot task by id.
     * 
     * @return task, not <code>null</code>
     * @throws BotTaskDoesNotExistException
     */
    public BotTask getBotTask(Subject subject, Long id) throws BotTaskDoesNotExistException;

    /**
     * Updates bot task.
     * 
     * @throws BotTaskAlreadyExistsException
     *             if name collision occurs
     */
    public void updateBotTask(Subject subject, BotTask task) throws BotTaskAlreadyExistsException;

    /**
     * Removes bot task by id.
     * 
     * @throws BotTaskDoesNotExistException
     */
    public void removeBotTask(Subject subject, Long id) throws BotTaskDoesNotExistException;

}
