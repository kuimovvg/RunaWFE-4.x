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
package ru.runa.wf.webservice;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.AttributeRequiredException;
import ru.runa.WSLoggerInterceptor;
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
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPrincipal;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

@Stateless
@WebService(name = "BotRunner", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "BotWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class BotBean {
    @Autowired
    private BotLogic botLogic;
    @Autowired
    private ExecutorLogic executorLogic;

    @WebMethod
    public void createBotStation(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "address", targetNamespace = "http://runa.ru/workflow/webservices") String address)
            throws BotStationAlreadyExistsException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        BotStation botStation = new BotStation(name, address);
        botLogic.create(subject, botStation);
    }

    @WebMethod
    public void updateBotStation(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "newName", targetNamespace = "http://runa.ru/workflow/webservices") String newName,
            @WebParam(mode = Mode.IN, name = "address", targetNamespace = "http://runa.ru/workflow/webservices") String address)
            throws BotStationAlreadyExistsException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        BotStation botStation = new BotStation(name);
        botStation = botLogic.getBotStation(subject, botStation);
        if (newName != null) {
            botStation.setName(newName);
        }
        if (address != null) {
            botStation.setAddress(address);
        }
        botLogic.update(subject, botStation);
    }

    @WebMethod
    public void removeBotStation(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws BotStationDoesNotExistException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        BotStation botStation = new BotStation(name);
        botLogic.remove(subject, botStation);
    }

    @WebMethod
    public void createBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "password", targetNamespace = "http://runa.ru/workflow/webservices") String password,
            @WebParam(mode = Mode.IN, name = "starttimeout", targetNamespace = "http://runa.ru/workflow/webservices") String starttimeout)
            throws ExecutorAlreadyExistsException, ExecutorDoesNotExistException, WeakPasswordException, BotAlreadyExistsException,
            AuthorizationException, AuthenticationException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);
        if (botStationName == null || botStationName.length() == 0) {
            throw new AttributeRequiredException("BotRunner", "botStationName");
        }
        BotStation station = new BotStation(botStationName);
        station = botLogic.getBotStation(subject, station);

        Actor actor = new Actor(name, "bot");
        executorLogic.create(subject, actor);
        executorLogic.setPassword(subject, actor, password);

        Bot bot = new Bot();
        bot.setWfeUser(name);
        bot.setWfePass(password);
        if (starttimeout != null && !starttimeout.equals("")) {
            bot.setLastInvoked(Long.parseLong(starttimeout));
        }
        bot.setBotStation(station);
        botLogic.create(subject, bot);
    }

    @WebMethod
    public void updateBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "newName", targetNamespace = "http://runa.ru/workflow/webservices") String newName,
            @WebParam(mode = Mode.IN, name = "botStation", targetNamespace = "http://runa.ru/workflow/webservices") String botStation,
            @WebParam(mode = Mode.IN, name = "newBotStation", targetNamespace = "http://runa.ru/workflow/webservices") String newBotStation,
            @WebParam(mode = Mode.IN, name = "password", targetNamespace = "http://runa.ru/workflow/webservices") String password,
            @WebParam(mode = Mode.IN, name = "starttimeout", targetNamespace = "http://runa.ru/workflow/webservices") String starttimeout)
            throws BotAlreadyExistsException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        Bot bot = new Bot();
        bot.setWfeUser(name);
        if (botStation != null) {
            BotStation station = new BotStation(botStation);
            station = botLogic.getBotStation(subject, station);
            bot.setBotStation(station);
        }
        bot = botLogic.getBot(subject, bot);
        if (newName != null) {
            bot.setWfeUser(newName);
        }
        if (password != null) {
            bot.setWfePass(password);
        }
        if (starttimeout != null) {
            bot.setLastInvoked(Long.parseLong(starttimeout));
        }
        if (newBotStation != null) {
            bot.getBotStation().setName(newBotStation);
        }
        botLogic.update(subject, bot);
    }

    @WebMethod
    public void removeBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthorizationException, AuthenticationException, BotDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Bot bot = new Bot();
        bot.setWfeUser(name);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botLogic.getBotStation(subject, bs));
        }
        botLogic.remove(subject, bot);
    }

    @WebMethod
    public void addConfigurationsToBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "botName", targetNamespace = "http://runa.ru/workflow/webservices") String botName,
            @WebParam(mode = Mode.IN, name = "configurations", targetNamespace = "http://runa.ru/workflow/webservices") BotConfigurationDescr[] botConfigurations)
            throws IOException, AuthorizationException, AuthenticationException, BotTaskAlreadyExistsException {
        Subject subject = getSubject(actorPrincipal);
        BotStation bs = null;
        if (!Strings.isNullOrEmpty(botStationName)) {
            bs = new BotStation(botStationName);
            bs = botLogic.getBotStation(subject, bs);
        }

        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (bs != null) {
            bot.setBotStation(bs);
        }
        bot = botLogic.getBot(subject, bot);

        for (BotConfigurationDescr botConfigurationDescr : botConfigurations) {
            String name = botConfigurationDescr.getName();
            String handler = botConfigurationDescr.getHandler();
            String config = botConfigurationDescr.getConfig();
            BotTask task = new BotTask();
            task.setBot(bot);
            task.setName(name);
            if (handler == null) {
                handler = "";
            }
            if (config == null) {
                config = "";
            }
            task.setClazz(handler);
            if (config.equals("")) {
                task.setConfiguration(new byte[0]);
            }
            byte[] configuration = getBotTaskConfiguration(config);
            task.setConfiguration(configuration);
            botLogic.create(subject, task);
        }
    }

    @WebMethod
    public void removeConfigurationsToBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "botName", targetNamespace = "http://runa.ru/workflow/webservices") String botName,
            @WebParam(mode = Mode.IN, name = "configurations", targetNamespace = "http://runa.ru/workflow/webservices") BotConfigurationDescr[] botConfigurations)
            throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        BotStation bs = null;
        if (botStationName != null && botStationName.length() > 0) {
            bs = new BotStation(botStationName);
            bs = botLogic.getBotStation(subject, bs);
        }

        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (bs != null) {
            bot.setBotStation(bs);
        }
        bot = botLogic.getBot(subject, bot);

        for (BotConfigurationDescr botConfigurationDescr : botConfigurations) {
            String name = botConfigurationDescr.getName();
            BotTask task = new BotTask();
            task.setName(name);
            task.setBot(bot);
            botLogic.remove(subject, task);
        }
    }

    @WebMethod
    public void removeAllConfigurationsFromBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "botName", targetNamespace = "http://runa.ru/workflow/webservices") String botName)
            throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botLogic.getBotStation(subject, bs));
        }
        bot = botLogic.getBot(subject, bot);
        if (bot == null) {
            throw new BotDoesNotExistException(botName);
        }
        for (BotTask task : botLogic.getBotTaskList(subject, bot)) {
            botLogic.remove(subject, task);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "BotConfigurationDescr", namespace = "http://runa.ru/workflow/webservices")
    public static class BotConfigurationDescr {

        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String name;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String handler;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String config;

        public BotConfigurationDescr() {
        };

        public String getName() {
            return name;
        }

        public String getHandler() {
            return handler;
        }

        public String getConfig() {
            return config;
        }
    }

    private byte[] getBotTaskConfiguration(String config) throws IOException {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(config);
            byte[] result = new byte[is.available()];
            is.read(result);
            return result;
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private Subject getSubject(ActorPrincipal actor) {
        Subject result = new Subject();
        result.getPrincipals().add(actor);
        return result;
    }
}
