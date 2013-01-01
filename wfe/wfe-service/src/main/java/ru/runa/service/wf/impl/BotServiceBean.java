package ru.runa.service.wf.impl;

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
import javax.security.auth.Subject;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.w3c.dom.Document;

import ru.runa.service.delegate.WfeScriptForBotStations;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.service.wf.BotServiceLocal;
import ru.runa.service.wf.BotServiceRemote;
import ru.runa.wfe.ApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

/**
 * Implements BotsService as bean.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class BotServiceBean implements BotServiceLocal, BotServiceRemote {
    @Autowired
    private BotLogic botLogic;

    @Override
    public BotStation createBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException,
            BotStationAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        return botLogic.createBotStation(subject, bs);
    }

    @Override
    public BotStation getBotStation(Long id) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(id);
        return botLogic.getBotStationNotNull(id);
    }

    @Override
    public BotStation getBotStation(String name) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(name);
        return botLogic.getBotStation(name);
    }

    @Override
    public List<BotStation> getBotStations() {
        return botLogic.getBotStations();
    }

    @Override
    public void removeBotStation(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        botLogic.removeBotStation(subject, id);
    }

    @Override
    public void updateBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException,
            BotStationAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        botLogic.updateBotStation(subject, bs);
    }

    @Override
    public Bot getBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        return botLogic.getBotNotNull(subject, id);
    }

    @Override
    public void removeBot(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        botLogic.removeBot(subject, id);
    }

    @Override
    public List<Bot> getBots(Subject subject, Long botStationId) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(botStationId);
        return botLogic.getBots(subject, botStationId);
    }

    @Override
    public Bot createBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        return botLogic.createBot(subject, bot);
    }

    @Override
    public void updateBot(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        botLogic.updateBot(subject, bot);
    }

    @Override
    public List<BotTask> getBotTasks(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        return botLogic.getBotTasks(subject, id);
    }

    @Override
    public BotTask createBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        return botLogic.createBotTask(subject, task);
    }

    @Override
    public void updateBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        botLogic.updateBotTask(subject, task);
    }

    @Override
    public void removeBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        botLogic.removeBotTask(subject, id);
    }

    @Override
    public BotTask getBotTask(Subject subject, Long id) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(id);
        return botLogic.getBotTaskNotNull(subject, id);
    }

    @Override
    public byte[] exportBot(Subject subject, Bot bot) throws BotDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("script.xml"));
            List<BotTask> tasks = botLogic.getBotTasks(subject, bot.getId());
            Document script = WfeScriptForBotStations.createScriptForBotLoading(bot, tasks);
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
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public byte[] exportBotStation(Subject subject, BotStation station) throws BotStationDoesNotExistException {
        try {
            Preconditions.checkNotNull(subject);
            Preconditions.checkNotNull(station);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("botstation"));
            zipStream.write(station.getName().getBytes());
            zipStream.write('\n');
            zipStream.write(station.getAddress().getBytes());
            for (Bot bot : getBots(subject, station.getId())) {
                zipStream.putNextEntry(new ZipEntry(bot.getUsername() + ".bot"));
                byte[] botArchive = exportBot(subject, bot);
                zipStream.write(botArchive);
            }
            zipStream.close();
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void importBot(Subject subject, BotStation station, byte[] archive, boolean replace) throws BotStationDoesNotExistException {
        try {
            Preconditions.checkNotNull(subject);
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
            WfeScriptForBotStations wfeScriptForBotStations = new WfeScriptForBotStations(subject, replace);
            ApplicationContextFactory.autowireBean(wfeScriptForBotStations);
            wfeScriptForBotStations.setBotStation(station);
            wfeScriptForBotStations.setConfigs(files);
            wfeScriptForBotStations.runScript(script);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public void importBotStation(Subject subject, byte[] archive, boolean replace) {
        try {
            Preconditions.checkNotNull(subject);
            Preconditions.checkNotNull(archive);
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
            ZipEntry entry;
            BotStation station = null;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals("botstation")) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(zin));
                    String name = r.readLine();
                    String addr = r.readLine();
                    station = getBotStation(name);
                    if (station == null) {
                        station = createBotStation(subject, new BotStation(name, addr));
                    }
                    continue;
                }
                if (station == null) {
                    throw new IOException("Incorrect bot archive");
                }
                importBot(subject, station, ByteStreams.toByteArray(zin), replace);
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
}
