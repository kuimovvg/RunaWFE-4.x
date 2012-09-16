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
package ru.runa.af.webservice;

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

import ru.runa.af.Actor;
import ru.runa.af.ActorPrincipal;
import ru.runa.af.AttributeRequiredException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Bot;
import ru.runa.af.BotAlreadyExistsException;
import ru.runa.af.BotDoesNotExistsException;
import ru.runa.af.BotStation;
import ru.runa.af.BotStationAlreadyExistsException;
import ru.runa.af.BotStationDoesNotExistsException;
import ru.runa.af.BotTask;
import ru.runa.af.BotTaskAlreadyExistsException;
import ru.runa.af.BotTaskDoesNotExistsException;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ObjectDoesNotExistException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.logic.BotsLogic;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

@Stateless
@WebService(name = "Bot", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "BotWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class BotBean {
    @Autowired
    private BotsLogic botsLogic;
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
        botsLogic.create(subject, botStation);
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
        botStation = botsLogic.getBotStation(subject, botStation);
        if (newName != null) {
            botStation.setName(newName);
        }
        if (address != null) {
            botStation.setAddress(address);
        }
        botsLogic.update(subject, botStation);
    }

    @WebMethod
    public void removeBotStation(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws BotStationDoesNotExistsException, AuthorizationException, AuthenticationException {
        Subject subject = getSubject(actorPrincipal);
        BotStation botStation = new BotStation(name);
        botsLogic.remove(subject, botStation);
    }

    @WebMethod
    public void createBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "password", targetNamespace = "http://runa.ru/workflow/webservices") String password,
            @WebParam(mode = Mode.IN, name = "starttimeout", targetNamespace = "http://runa.ru/workflow/webservices") String starttimeout)
            throws ExecutorAlreadyExistsException, ExecutorOutOfDateException, WeakPasswordException, BotAlreadyExistsException,
            AuthorizationException, AuthenticationException, AttributeRequiredException {
        Subject subject = getSubject(actorPrincipal);
        if (botStationName == null || botStationName.length() == 0) {
            throw new AttributeRequiredException("Bot", "botStationName");
        }
        BotStation station = new BotStation(botStationName);
        station = botsLogic.getBotStation(subject, station);

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
        botsLogic.create(subject, bot);
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
            station = botsLogic.getBotStation(subject, station);
            bot.setBotStation(station);
        }
        bot = botsLogic.getBot(subject, bot);
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
        botsLogic.update(subject, bot);
    }

    @WebMethod
    public void removeBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name)
            throws AuthorizationException, AuthenticationException, BotDoesNotExistsException {
        Subject subject = getSubject(actorPrincipal);
        Bot bot = new Bot();
        bot.setWfeUser(name);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botsLogic.getBotStation(subject, bs));
        }
        botsLogic.remove(subject, bot);
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
            bs = botsLogic.getBotStation(subject, bs);
        }

        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (bs != null) {
            bot.setBotStation(bs);
        }
        bot = botsLogic.getBot(subject, bot);

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
            botsLogic.create(subject, task);
        }
    }

    @WebMethod
    public void removeConfigurationsToBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "botName", targetNamespace = "http://runa.ru/workflow/webservices") String botName,
            @WebParam(mode = Mode.IN, name = "configurations", targetNamespace = "http://runa.ru/workflow/webservices") BotConfigurationDescr[] botConfigurations)
            throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistsException {
        Subject subject = getSubject(actorPrincipal);
        BotStation bs = null;
        if (botStationName != null && botStationName.length() > 0) {
            bs = new BotStation(botStationName);
            bs = botsLogic.getBotStation(subject, bs);
        }

        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (bs != null) {
            bot.setBotStation(bs);
        }
        bot = botsLogic.getBot(subject, bot);

        for (BotConfigurationDescr botConfigurationDescr : botConfigurations) {
            String name = botConfigurationDescr.getName();
            BotTask task = new BotTask();
            task.setName(name);
            task.setBot(bot);
            botsLogic.remove(subject, task);
        }
    }

    @WebMethod
    public void removeAllConfigurationsFromBot(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") ActorPrincipal actorPrincipal,
            @WebParam(mode = Mode.IN, name = "botStationName", targetNamespace = "http://runa.ru/workflow/webservices") String botStationName,
            @WebParam(mode = Mode.IN, name = "botName", targetNamespace = "http://runa.ru/workflow/webservices") String botName)
            throws AuthorizationException, AuthenticationException, BotTaskDoesNotExistsException, ObjectDoesNotExistException {
        Subject subject = getSubject(actorPrincipal);
        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botsLogic.getBotStation(subject, bs));
        }
        bot = botsLogic.getBot(subject, bot);
        if (bot == null) {
            throw new ObjectDoesNotExistException(botName);
        }
        for (BotTask task : botsLogic.getBotTaskList(subject, bot)) {
            botsLogic.remove(subject, task);
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
