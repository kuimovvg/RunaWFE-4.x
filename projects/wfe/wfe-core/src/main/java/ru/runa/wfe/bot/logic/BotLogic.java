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
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;

import com.google.common.base.Objects;

public class BotLogic extends CommonLogic {
    @Autowired
    private BotDAO botDAO;

    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotStation(bs);
    }

    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBot(b);
    }

    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotList(b);
    }

    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotStationList();
    }

    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        if (botDAO.getBotStation(bs) != null) {
            throw new BotStationAlreadyExistsException(bs.getName());
        }
        return botDAO.create(bs);
    }

    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        BotStation bs2 = new BotStation(bs.getName());
        bs2 = getBotStation(subject, bs2);
        if (bs2 != null && !Objects.equal(bs2.getId(), bs.getId())) {
            throw new BotStationAlreadyExistsException(bs.getName());
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        long version = 1;
        if (bs.getVersion() != null) {
            version += bs.getVersion();
        }
        bs.setVersion(version);
        return botDAO.update(bs);
    }

    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        String name = bs.getName();
        bs = getBotStation(subject, bs);
        if (bs == null) {
            throw new BotStationDoesNotExistException(name);
        }
        List<Bot> bots = getBotList(subject, bs);
        for (Bot bot : bots) {
            remove(subject, bot);
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        botDAO.remove(bs);
    }

    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        String name = b.getWfeUser();
        b = getBot(subject, b);
        if (b == null) {
            throw new BotDoesNotExistException(name);
        }
        List<BotTask> tasks = getBotTaskList(subject, b);
        for (BotTask botTask : tasks) {
            remove(subject, botTask);
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        botDAO.remove(b);
        update(subject, b.getBotStation());
    }

    public List<Bot> getBotList(Subject subject, BotStation station) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotList(station);
    }

    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        if (botDAO.getBot(bot) != null) {
            throw new BotAlreadyExistsException(bot.getWfeUser());
        }
        Bot res = botDAO.create(bot);
        update(subject, bot.getBotStation());
        return res;
    }

    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Bot b = new Bot();
        b.setWfeUser(bot.getWfeUser());
        b.setBotStation(bot.getBotStation());
        b = getBot(subject, b);
        if (b != null && !Objects.equal(b.getId(), bot.getId())) {
            throw new BotAlreadyExistsException(bot.getWfeUser());
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        Bot res = botDAO.update(bot);
        update(subject, bot.getBotStation());
        return res;
    }

    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        BotTask t = new BotTask();
        t.setName(task.getName());
        t.setBot(task.getBot());
        t = getBotTask(subject, t);
        if (t != null && !Objects.equal(t.getId(), task.getId())) {
            throw new BotTaskAlreadyExistsException(task.getName());
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        BotTask res = botDAO.update(task);
        update(subject, task.getBot());
        return res;
    }

    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        if (getBotTask(subject, task) != null) {
            throw new BotTaskAlreadyExistsException(task.getName());
        }
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        return botDAO.create(task);
    }

    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotTaskList(bot);
    }

    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        checkPermissionsOnBotStations(subject, BotStationPermission.BOT_STATION_CONFIGURE);
        String name = task.getName();
        task = botDAO.getBotTask(task);
        if (task == null) {
            throw new BotTaskDoesNotExistException(name);
        }
        botDAO.remove(task);
        update(subject, task.getBot());
    }

    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botDAO.getBotTask(task);
    }

    private void checkPermissionsOnBotStations(Subject subject, Permission permission) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, BotStation.INSTANCE, permission);
    }
}
