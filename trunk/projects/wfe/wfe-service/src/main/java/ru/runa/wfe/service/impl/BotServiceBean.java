package ru.runa.wfe.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.service.decl.BotServiceLocal;
import ru.runa.wfe.service.decl.BotServiceRemote;
import ru.runa.wfe.service.delegate.WfeScriptForBotStations;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * Implements BotsService as bean.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService
@SOAPBinding
public class BotServiceBean implements BotServiceLocal, BotServiceRemote {
    @Autowired
    private BotLogic botLogic;

    @Override
    public BotStation createBotStation(User user, BotStation bs) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bs);
        return botLogic.createBotStation(user, bs);
    }

    @Override
    public BotStation getBotStation(Long id) {
        Preconditions.checkNotNull(id);
        return botLogic.getBotStationNotNull(id);
    }

    // @javax.xml.ws.RequestWrapper(className="ru.runa.wfe.service.wf.impl.UniqueClassName")
    // @javax.xml.ws.ResponseWrapper(className="ru.runa.wfe.service.wf.impl.UniqueClassNameResponse")
    @Override
    public BotStation getBotStationByName(String name) {
        Preconditions.checkNotNull(name);
        return botLogic.getBotStation(name);
    }

    @Override
    public List<BotStation> getBotStations() {
        return botLogic.getBotStations();
    }

    @Override
    public void removeBotStation(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        botLogic.removeBotStation(user, id);
    }

    @Override
    public void updateBotStation(User user, BotStation bs) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bs);
        botLogic.updateBotStation(user, bs);
    }

    @Override
    public Bot getBot(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        return botLogic.getBotNotNull(user, id);
    }

    @Override
    public void removeBot(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        botLogic.removeBot(user, id);
    }

    @Override
    public List<Bot> getBots(User user, Long botStationId) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(botStationId);
        return botLogic.getBots(user, botStationId);
    }

    @Override
    public Bot createBot(User user, Bot bot) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bot);
        return botLogic.createBot(user, bot);
    }

    @Override
    public void updateBot(User user, Bot bot) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bot);
        botLogic.updateBot(user, bot);
    }

    @Override
    public List<BotTask> getBotTasks(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        return botLogic.getBotTasks(user, id);
    }

    @Override
    public BotTask createBotTask(User user, BotTask task) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(task);
        return botLogic.createBotTask(user, task);
    }

    @Override
    public void updateBotTask(User user, BotTask task) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(task);
        botLogic.updateBotTask(user, task);
    }

    @Override
    public void removeBotTask(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        botLogic.removeBotTask(user, id);
    }

    @Override
    public BotTask getBotTask(User user, Long id) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(id);
        return botLogic.getBotTaskNotNull(user, id);
    }

    @Override
    public byte[] exportBot(User user, Bot bot) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bot);
        List<BotTask> tasks = botLogic.getBotTasks(user, bot.getId());
        return exportBotWithTasks(bot, tasks);
    }

    private byte[] exportBotWithTasks(Bot bot, List<BotTask> tasks) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("script.xml"));
            byte[] script = WfeScriptForBotStations.createScriptForBotLoading(bot, tasks);
            zipStream.write(script);
            for (BotTask task : tasks) {
                byte[] conf = task.getConfiguration();
                if (conf == null || conf.length == 0) {
                    continue;
                }
                zipStream.putNextEntry(new ZipEntry(task.getName() + ".conf"));
                zipStream.write(conf);
            }
            zipStream.close();
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public byte[] exportBotStation(User user, BotStation station) {
        try {
            Preconditions.checkNotNull(user);
            Preconditions.checkNotNull(station);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("botstation"));
            zipStream.write(station.getName().getBytes());
            zipStream.write('\n');
            zipStream.write(station.getAddress().getBytes());
            for (Bot bot : getBots(user, station.getId())) {
                zipStream.putNextEntry(new ZipEntry(bot.getUsername() + ".bot"));
                byte[] botArchive = exportBot(user, bot);
                zipStream.write(botArchive);
            }
            zipStream.close();
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void importBot(User user, BotStation station, byte[] archive, boolean replace) {
        try {
            Preconditions.checkNotNull(user);
            Preconditions.checkNotNull(station);
            Preconditions.checkNotNull(archive);
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
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
            WfeScriptForBotStations wfeScriptForBotStations = new WfeScriptForBotStations(user, replace);
            ApplicationContextFactory.autowireBean(wfeScriptForBotStations);
            wfeScriptForBotStations.setBotStation(station);
            wfeScriptForBotStations.setConfigs(files);
            wfeScriptForBotStations.runScript(script);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void importBotStation(User user, byte[] archive, boolean replace) {
        try {
            Preconditions.checkNotNull(user);
            Preconditions.checkNotNull(archive);
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
            ZipEntry entry;
            BotStation station = null;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals("botstation")) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(zin));
                    String name = r.readLine();
                    String addr = r.readLine();
                    station = getBotStationByName(name);
                    if (station == null) {
                        station = createBotStation(user, new BotStation(name, addr));
                    }
                    continue;
                }
                if (station == null) {
                    throw new IOException("Incorrect bot archive");
                }
                importBot(user, station, ByteStreams.toByteArray(zin), replace);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public byte[] exportBotTask(User user, Bot bot, String botTaskName) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(bot);
        Preconditions.checkNotNull(botTaskName);
        List<BotTask> tasks = Lists.newArrayList();
        tasks.add(botLogic.getBotTaskNotNull(user, bot.getId(), botTaskName));
        return exportBotWithTasks(bot, tasks);
    }

}
