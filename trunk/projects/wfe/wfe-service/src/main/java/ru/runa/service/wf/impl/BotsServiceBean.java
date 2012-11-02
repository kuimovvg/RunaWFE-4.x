package ru.runa.service.wf.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import ru.runa.service.wf.BotsServiceLocal;
import ru.runa.service.wf.BotsServiceRemote;
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
public class BotsServiceBean implements BotsServiceLocal, BotsServiceRemote {
    @Autowired
    private BotLogic botLogic;

    @Override
    public BotStation create(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        return botLogic.create(subject, bs);
    }

    @Override
    public BotStation getBotStation(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        return botLogic.getBotStation(subject, bs);
    }

    @Override
    public Bot getBot(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(b);
        return botLogic.getBot(subject, b);
    }

    @Override
    public List<Bot> getBotList(Subject subject, Bot b) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(b);
        return botLogic.getBotList(subject, b);
    }

    @Override
    public List<BotStation> getBotStationList(Subject subject) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        return botLogic.getBotStationList(subject);
    }

    @Override
    public void remove(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        botLogic.remove(subject, bs);
    }

    @Override
    public void remove(Subject subject, Bot b) throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(b);
        botLogic.remove(subject, b);
    }

    @Override
    public BotStation update(Subject subject, BotStation bs) throws AuthorizationException, AuthenticationException, BotStationAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bs);
        return botLogic.update(subject, bs);
    }

    @Override
    public List<Bot> getBotList(Subject subject, BotStation station) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(station);
        return botLogic.getBotList(subject, station);
    }

    @Override
    public Bot create(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        return botLogic.create(subject, bot);
    }

    @Override
    public Bot update(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException, BotAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        return botLogic.update(subject, bot);
    }

    @Override
    public List<BotTask> getBotTaskList(Subject subject, Bot bot) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        return botLogic.getBotTaskList(subject, bot);
    }

    @Override
    public BotTask create(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        return botLogic.create(subject, task);
    }

    @Override
    public BotTask update(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        return botLogic.update(subject, task);
    }

    @Override
    public void remove(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        botLogic.remove(subject, task);
    }

    @Override
    public BotTask getBotTask(Subject subject, BotTask task) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(task);
        return botLogic.getBotTask(subject, task);
    }

    @Override
    public void saveBot(Subject subject, Bot bot, OutputStream out) throws IOException, BotDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(bot);
        Preconditions.checkNotNull(out);
        try {
            ZipOutputStream zipStream = new ZipOutputStream(out);
            zipStream.putNextEntry(new ZipEntry("script.xml"));
            List<BotTask> tasks = new BotLogic().getBotTaskList(subject, bot);
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
            out.flush();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void saveBotStation(Subject subject, BotStation station, OutputStream out) throws IOException, BotStationDoesNotExistException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(station);
        Preconditions.checkNotNull(out);
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
    }

    @Override
    public void deployBot(Subject subject, BotStation station, InputStream in, boolean replace) throws BotStationDoesNotExistException, IOException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(station);
        Preconditions.checkNotNull(in);
        Preconditions.checkArgument(in != null, "Incorrect bot archive");
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
        WfeScriptForBotStations wfeScriptForBotStations = new WfeScriptForBotStations(subject, replace);
        wfeScriptForBotStations.setBotStation(station);
        wfeScriptForBotStations.setConfigs(files);
        wfeScriptForBotStations.runScript(script);
    }

    @Override
    public void deployBotStation(Subject subject, InputStream in, boolean replace) throws IOException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(in);
        Preconditions.checkArgument(in != null, "Incorrect bot archive");
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
}
