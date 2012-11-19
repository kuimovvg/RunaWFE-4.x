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
package ru.runa.wfe.bot.logic;

import java.util.List;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.bot.dao.BotDAO;
import ru.runa.wfe.bot.dao.BotStationDAO;
import ru.runa.wfe.bot.dao.BotTaskDAO;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class BotLogic extends CommonLogic {
    @Autowired
    private BotStationDAO botStationDAO;
    @Autowired
    private BotDAO botDAO;
    @Autowired
    private BotTaskDAO botTaskDAO;

    public List<BotStation> getBotStations() throws AuthorizationException {
        return botStationDAO.getAll();
    }

    public BotStation createBotStation(Subject subject, BotStation botStation) throws AuthorizationException, BotStationAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        if (botStationDAO.get(botStation.getName()) != null) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        return botStationDAO.create(botStation);
    }

    public void updateBotStation(Subject subject, BotStation botStation) throws AuthorizationException, BotStationAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        BotStation botStationToCheck = getBotStation(botStation.getName());
        if (botStationToCheck != null && !Objects.equal(botStationToCheck.getId(), botStation.getId())) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        botStationDAO.update(botStation);
    }

    public BotStation getBotStationNotNull(Long id) throws AuthorizationException, BotStationDoesNotExistException {
        return botStationDAO.getNotNull(id);
    }

    public BotStation getBotStation(String name) throws AuthorizationException {
        return botStationDAO.get(name);
    }

    public BotStation getBotStationNotNull(String name) throws AuthorizationException, BotStationDoesNotExistException {
        return botStationDAO.getNotNull(name);
    }

    public void removeBotStation(Subject subject, Long id) throws AuthorizationException, BotStationDoesNotExistException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        List<Bot> bots = getBots(subject, id);
        for (Bot bot : bots) {
            removeBot(subject, bot.getId());
        }
        botStationDAO.delete(id);
    }

    public Bot createBot(Subject subject, Bot bot) throws AuthorizationException, BotAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(bot.getBotStation());
        if (getBot(subject, bot.getBotStation().getId(), bot.getUsername()) != null) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        return botDAO.create(bot);
    }

    public List<Bot> getBots(Subject subject, Long botStationId) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.getAll(botStation);
    }

    public Bot getBotNotNull(Subject subject, Long id) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getNotNull(id);
    }

    private Bot getBot(Subject subject, Long botStationId, String name) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.get(botStation, name);
    }

    public Bot getBotNotNull(Subject subject, Long botStationId, String name) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDAO.getNotNull(botStation, name);
    }

    public void updateBot(Subject subject, Bot bot) throws AuthorizationException, BotAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(bot.getBotStation());
        Bot botToCheck = getBot(subject, bot.getBotStation().getId(), bot.getUsername());
        if (botToCheck != null && !Objects.equal(botToCheck.getId(), bot.getId())) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        botDAO.update(bot);
    }

    public void removeBot(Subject subject, Long id) throws AuthorizationException, BotDoesNotExistException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        List<BotTask> tasks = getBotTasks(subject, id);
        for (BotTask botTask : tasks) {
            removeBotTask(subject, botTask.getId());
        }
        botDAO.delete(id);
    }

    public BotTask createBotTask(Subject subject, BotTask botTask) throws AuthorizationException, BotTaskAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(botTask.getBot());
        if (getBotTask(subject, botTask.getBot().getId(), botTask.getName()) != null) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        return botTaskDAO.create(botTask);
    }

    public List<BotTask> getBotTasks(Subject subject, Long id) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        Bot bot = getBotNotNull(subject, id);
        return botTaskDAO.getAll(bot);
    }

    public BotTask getBotTaskNotNull(Subject subject, Long id) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botTaskDAO.getNotNull(id);
    }

    private BotTask getBotTask(Subject subject, Long botId, String name) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        Bot bot = getBotNotNull(subject, botId);
        return botTaskDAO.get(bot, name);
    }

    public BotTask getBotTaskNotNull(Subject subject, Long botId, String name) throws AuthorizationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        Bot bot = getBotNotNull(subject, botId);
        return botTaskDAO.getNotNull(bot, name);
    }

    public void updateBotTask(Subject subject, BotTask botTask) throws AuthorizationException, BotTaskAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        Preconditions.checkNotNull(botTask.getBot());
        BotTask botTaskToCheck = getBotTask(subject, botTask.getBot().getId(), botTask.getName());
        if (botTaskToCheck != null && !Objects.equal(botTaskToCheck.getId(), botTask.getId())) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        if (botTask.getConfiguration() != null && botTask.getConfiguration().length == 0) {
            BotTask botTaskFromDB = getBotTaskNotNull(subject, botTask.getId());
            botTask.setConfiguration(botTaskFromDB.getConfiguration());
        }
        botTaskDAO.update(botTask);
    }

    public void removeBotTask(Subject subject, Long id) throws AuthorizationException, BotTaskDoesNotExistException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        botTaskDAO.delete(id);
    }

    private void checkPermissionsOnBotStations(Subject subject, Permission permission) throws AuthorizationException {
        checkPermissionAllowed(subject, BotStation.INSTANCE, permission);
    }
}
