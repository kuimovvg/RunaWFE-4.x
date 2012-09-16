/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.dao;

import java.util.List;

import ru.runa.InternalApplicationException;
import ru.runa.af.Bot;
import ru.runa.af.BotStation;
import ru.runa.af.BotTask;

/**
  * DAO level interface for managing bot stations and so on.
  * @author Konstantinov Aleksey 25.02.2012
  */
public interface BotsDAO {

    /**
     * Load {@linkplain BotStation} from database.
     * Loading performed by identity and name, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and name found. 
     * @param botStation {@linkplain BotStation}, which parameter used to load result.
     * @return Loaded {@linkplain BotStation} or null, if no bot station found.
     */
    public BotStation getBotStation(BotStation botStation);

    /**
     * Load {@linkplain Bot} from database.
     * Loading performed by identity and userName and bot station, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and userName and bot station found. 
     * @param bot {@linkplain Bot}, which parameter used to load result.
     * @return Loaded {@linkplain Bot} or null, if no bot found.
     */
    public Bot getBot(Bot bot);

    /**
     * Load {@linkplain BotTask} from database.
     * Loading performed by identity and taskName and bot, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and taskName and bot found. 
     * @param bot {@linkplain BotTask}, which parameter used to load result.
     * @return Loaded {@linkplain BotTask} or null, if no bot task found.
     */
    public BotTask getBotTask(BotTask task);

    /**
     * Load all {@linkplain BotStation}'s from database. 
     * @return {@linkplain BotStation}'s list.
     */
    public List<BotStation> getBotStationList();

    /**
     * Load all {@linkplain Bot}'s, defined for {@linkplain BotStation}.
     * @param botStation {@linkplain BotStation} to load {@linkplain Bot}'s. 
     * @return {@linkplain Bot}'s, defined for {@linkplain BotStation}.
     */
    public List<Bot> getBotList(BotStation botStation);

    /**
     * Load {@linkplain Bot}'s.
     * Loading performed by identity and userName and bot station, provided by parameter.
     * @param bot {@linkplain Bot}, which parameter used to load result.
     * @return {@linkplain Bot}'s list.
     */
    public List<Bot> getBotList(Bot bot);

    /**
     * Load all {@linkplain BotTask}, defined for {@linkplain Bot}.
     * @param bot {@linkplain Bot} to load {@linkplain BotTask}'s.
     * @return {@linkplain BotTask}, defined for {@linkplain Bot}.
     */
    public List<BotTask> getBotTaskList(Bot bot);

    /**
     * Save {@linkplain BotStation}. {@linkplain BotStation} must not be saved before.
     * @param botStation {@linkplain BotStation} to save.
     * @return Saved {@linkplain BotStation}.
     */
    public BotStation create(BotStation botStation);

    /**
     * Save {@linkplain Bot}. {@linkplain Bot} must not be saved before.
     * @param bot {@linkplain Bot} to save.
     * @return Saved {@linkplain Bot}.
     */
    public Bot create(Bot bot);

    /**
     * Save {@linkplain BotTask}. {@linkplain BotTask} must not be saved before. 
     * @param botTask {@linkplain BotTask} to save.
     * @return Saved {@linkplain BotTask}.
     */
    public BotTask create(BotTask botTask);

    /**
     * Update {@linkplain BotStation}. {@linkplain BotStation} must be saved before.
     * @param botStation {@linkplain BotStation} to update.
     * @return Updated {@linkplain BotStation}.
     */
    public BotStation update(BotStation botStation);

    /**
     * Update {@linkplain Bot}. {@linkplain Bot} must be saved before.
     * @param bot {@linkplain Bot} to update.
     * @return Updated {@linkplain Bot}.
     */
    public Bot update(Bot bot);

    /**
     * Update {@linkplain BotTask}. {@linkplain BotTask} must be saved before. 
     * @param botTask {@linkplain BotTask} to update.
     * @return Updated {@linkplain BotTask}.
     */
    public BotTask update(BotTask botTask);

    /**
     * Remove saved {@linkplain BotStation}.
     * @param botStation {@linkplain BotStation} to remove.
     */
    public void remove(BotStation botStation);

    /**
     * Remove saved {@linkplain Bot}.
     * @param bot {@linkplain Bot} to remove.
     */
    public void remove(Bot bot);

    /**
     * Remove saved {@linkplain BotTask}. 
     * @param botTask {@linkplain BotTask} to remove.
     */
    public void remove(BotTask botTask);
}
