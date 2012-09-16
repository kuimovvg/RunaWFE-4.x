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
package ru.runa.af.logic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.security.auth.Subject;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Bot;
import ru.runa.af.BotAlreadyExistsException;
import ru.runa.af.BotDoesNotExistsException;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationAlreadyExistsException;
import ru.runa.af.BotStationConfigurePermission;
import ru.runa.af.BotStationDoesNotExistsException;
import ru.runa.af.BotTask;
import ru.runa.af.BotTaskAlreadyExistsException;
import ru.runa.af.BotTaskDoesNotExistsException;
import ru.runa.af.Permission;
import ru.runa.af.dao.BotsDAO;

import com.google.common.io.ByteStreams;

public class BotsLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(BotsLogic.class);
    @Autowired
    private BotsDAO botsDAO;

    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        BotStation res = null;
        checkPermissionsOnBotStations(subject, Permission.READ);
        res = botsDAO.getBotStation(bs);
        return res;
    }

    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBot(b);
    }

    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBotList(b);
    }

    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBotStationList();
    }

    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        if (botsDAO.getBotStation(bs) != null) {
            throw new BotStationAlreadyExistsException(bs.getName());
        }
        return botsDAO.create(bs);
    }

    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        BotStation bs2 = new BotStation(bs.getName());
        bs2 = getBotStation(subject, bs2);
        if (bs2 != null && bs2.getId() != bs.getId()) {
            throw new BotStationAlreadyExistsException(bs.getName());
        }
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        bs.setVersion(bs.getVersion() + 1);
        return botsDAO.update(bs);
    }

    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistsException {
        String name = bs.getName();
        bs = getBotStation(subject, bs);
        if (bs == null) {
            throw new BotStationDoesNotExistsException(name);
        }
        List<Bot> bots = getBotList(subject, bs);
        Iterator<Bot> i = bots.iterator();
        while (i.hasNext()) {
            try {
                remove(subject, i.next());
            } catch (BotDoesNotExistsException e1) {
                throw new InternalApplicationException();
            }
        }
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        botsDAO.remove(bs);
    }

    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException {
        String name = b.getWfeUser();
        b = getBot(subject, b);
        if (b == null) {
            throw new BotDoesNotExistsException(name);
        }
        List<BotTask> tasks = getBotTaskList(subject, b);
        Iterator<BotTask> i = tasks.iterator();
        while (i.hasNext()) {
            try {
                remove(subject, i.next());
            } catch (BotTaskDoesNotExistsException e1) {
                throw new InternalApplicationException();
            }
        }
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        botsDAO.remove(b);
        try {
            update(subject, b.getBotStation());
        } catch (BotStationAlreadyExistsException e) {
            throw new InternalApplicationException(e);
        }
    }

    public List<Bot> getBotList(Subject subject, BotStation station) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBotList(station);
    }

    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        if (botsDAO.getBot(bot) != null) {
            throw new BotAlreadyExistsException(bot.getWfeUser());
        }
        Bot res = botsDAO.create(bot);
        update(subject, bot.getBotStation());
        return res;
    }

    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Bot b = new Bot();
        b.setWfeUser(bot.getWfeUser());
        b.setBotStation(bot.getBotStation());
        b = getBot(subject, b);
        if (b != null && b.getId() != bot.getId()) {
            throw new BotAlreadyExistsException(bot.getWfeUser());
        }
        Bot res = null;
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        res = botsDAO.update(bot);
        update(subject, bot.getBotStation());
        return res;
    }

    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        BotTask t = new BotTask();
        t.setName(task.getName());
        t.setBot(task.getBot());
        t = getBotTask(subject, t);
        if (t != null && t.getId() != task.getId()) {
            throw new BotTaskAlreadyExistsException(task.getName());
        }
        BotTask res = null;
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        res = botsDAO.update(task);
        update(subject, task.getBot());
        return res;
    }

    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        BotTask t = new BotTask();
        t.setName(task.getName());
        t.setBot(task.getBot());
        t = getBotTask(subject, t);
        if (t != null) {
            throw new BotTaskAlreadyExistsException(task.getName());
        }
        BotTask res = null;
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        res = botsDAO.create(task);
        update(subject, task.getBot());
        return res;
    }

    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBotTaskList(bot);
    }

    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistsException {
        checkPermissionsOnBotStations(subject, BotStationConfigurePermission.BOT_STATION_CONFIGURE);
        String name = task.getName();
        task = botsDAO.getBotTask(task);
        if (task == null) {
            throw new BotTaskDoesNotExistsException(name);
        }
        botsDAO.remove(task);
        update(subject, task.getBot());
    }

    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException {
        checkPermissionsOnBotStations(subject, Permission.READ);
        return botsDAO.getBotTask(task);
    }

    public void saveBot(Subject subject, Bot bot, OutputStream out) throws AuthorizationException, AuthenticationException, BotDoesNotExistsException {
        try {
            ZipOutputStream zipStream = new ZipOutputStream(out);
            zipStream.putNextEntry(new ZipEntry("script.xml"));
            List<BotTask> tasks = new BotsLogic().getBotTaskList(subject, bot);
            Class<?> clazz = Class.forName("ru.runa.wf.delegate.impl.WfeScriptForBotStations");
            Method method = clazz.getMethod("createScriptForBotLoading", Bot.class, List.class);
            Document script = (Document) method.invoke(null, bot, tasks);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(script), new StreamResult(zipStream));
            for (BotTask task : tasks) {
                byte[] conf = task.getConfiguration();
                if (conf == null || conf.length == 0) {
                    continue;
                }
                zipStream.putNextEntry(new ZipEntry(task.getName() + ".conf"));
                zipStream.write(conf);
            }
            zipStream.close();
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InternalApplicationException(e);
        }
    }

    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws AuthorizationException, AuthenticationException,
    BotStationDoesNotExistsException {
        try {
            ZipOutputStream zipStream = new ZipOutputStream(out);
            zipStream.putNextEntry(new ZipEntry("botstation"));
            zipStream.write(station.getName().getBytes());
            zipStream.write('\n');
            zipStream.write(station.getAddress().getBytes());
            for (Bot bot : getBotList(subject, station)) {
                zipStream.putNextEntry(new ZipEntry(bot.getWfeUser() + ".bot"));
                ByteArrayOutputStream botStream = new ByteArrayOutputStream();
                saveBot(subject, bot, botStream);
                botStream.close();
                zipStream.write(botStream.toByteArray());
            }
            zipStream.close();
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InternalApplicationException();
        } catch (BotDoesNotExistsException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws AuthorizationException,
            AuthenticationException, IOException {
        if (in == null) {
            throw new IOException("Incorrect bot archive");
        }
        ZipInputStream zin = new ZipInputStream(in);
        Map<String, byte[]> files = new HashMap<String, byte[]>();
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            byte[] bytes = ByteStreams.toByteArray(zin);
            files.put(entry.getName(), bytes);
        }
        if (files.get("script.xml") == null) {
            throw new IOException("Incorrect bot archive");
        }
        InputStream script = new ByteArrayInputStream(files.get("script.xml"));
        try {
            Class<?> clazz = Class.forName("ru.runa.wf.delegate.impl.WfeScriptForBotStations");
            Object scriptRunner = clazz.getConstructor(Subject.class, boolean.class).newInstance(subject, replace);
            clazz.getMethod("setBotStation", BotStation.class).invoke(scriptRunner, station);
            clazz.getMethod("setConfigs", Map.class).invoke(scriptRunner, files);
            clazz.getMethod("runScript", InputStream.class).invoke(scriptRunner, script);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IOException("Incorrect bot archive");
        }
    }

    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws AuthorizationException, AuthenticationException,
            IOException {
        if (in == null) {
            throw new IOException("Incorrect bot archive");
        }
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry;
        BotStation station = null;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals("botstation")) {
                BufferedReader r = new BufferedReader(new InputStreamReader(zin));
                String name = r.readLine();
                String addr = r.readLine();
                BotStation bs = new BotStation(name, addr);
                station = getBotStation(subject, bs);
                if (station == null) {
                    station = create(subject, bs);
                }
                continue;
            }
            if (station == null) {
                throw new IOException("Incorrect bot archive");
            }
            deployBot(subject, station, zin, replace);
        }
    }

    private void checkPermissionsOnBotStations(Subject subject, Permission permission) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, BotStation.SECURED_INSTANCE, permission);
    }
}
