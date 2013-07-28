package ru.runa.gpd.connector.wfe.rmi.jboss4;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.wfe.WFEServerConnector;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WFEJboss423RMIConnector extends WFEServerConnector {
    private InitialContext remoteContext;
    private User user;

    private User getUser() {
        if (user == null) {
            connect();
        } else {
            try {
                getExecutorService().getExecutor(user, user.getActor().getId());
            } catch (AuthenticationException e) {
                connect();
            }
        }
        return user;
    }

    @Override
    public void connect() {
        try {
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
            environment.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            String url = Activator.getPrefString(P_WFE_CONNECTION_HOST) + ":" + Activator.getPrefString(P_WFE_CONNECTION_PORT);
            environment.put(Context.PROVIDER_URL, url);
            remoteContext = new InitialContext(environment);
            AuthenticationService authenticationService = getService("AuthenticationServiceBean");
            if (LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_WFE_CONNECTION_LOGIN_MODE))) {
                String login = Activator.getPrefString(P_WFE_CONNECTION_LOGIN);
                String password = getPassword();
                if (password == null) {
                    return;
                }
                user = authenticationService.authenticateByLoginPassword(login, password);
            } else {
                user = authenticationService.authenticateByKerberos(getKerberosToken());
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void disconnect() throws Exception {
        user = null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(String beanName) {
        String jndiName = "runawfe/" + beanName + "/remote";
        try {
            return (T) remoteContext.lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to locale EJB by name " + jndiName, e);
        }
    }

    private ExecutorService getExecutorService() {
        return getService("ExecutorServiceBean");
    }

    /**
     * @return Map<Executor name, Is group>
     */
    @Override
    public Map<String, Boolean> getExecutors() {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<? extends Executor> executors = getExecutorService().getExecutors(getUser(), batchPresentation);
        Map<String, Boolean> result = Maps.newHashMapWithExpectedSize(executors.size());
        for (Executor executor : executors) {
            result.put(executor.getName(), executor instanceof Group);
        }
        return result;
    }

    @Override
    public List<String> getRelationNames() {
        RelationService executorService = getService("RelationServiceBean");
        BatchPresentation batchPresentation = BatchPresentationFactory.RELATIONS.createNonPaged();
        List<Relation> relations = executorService.getRelations(getUser(), batchPresentation);
        List<String> result = Lists.newArrayListWithExpectedSize(relations.size());
        for (Relation relation : relations) {
            result.add(relation.getName());
        }
        return result;
    }

    private DefinitionService getDefinitionService() {
        return getService("DefinitionServiceBean");
    }

    @Override
    public Map<WfDefinition, List<WfDefinition>> getProcessDefinitions(IProgressMonitor monitor) {
        DefinitionService definitionService = getDefinitionService();
        BatchPresentation batch = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        List<WfDefinition> latestDefinitions = definitionService.getLatestProcessDefinitions(getUser(), batch);
        Map<WfDefinition, List<WfDefinition>> result = Maps.newHashMapWithExpectedSize(latestDefinitions.size());
        monitor.worked(30);
        double perDefinition = (double) 70 / latestDefinitions.size();
        for (WfDefinition latestDefinition : latestDefinitions) {
            List<WfDefinition> historyDefinitions;
            try {
                historyDefinitions = definitionService.getProcessDefinitionHistory(getUser(), latestDefinition.getName());
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("definition '" + latestDefinition.getName() + "' sync", e);
                historyDefinitions = Lists.newArrayList();
            }
            result.put(latestDefinition, historyDefinitions);
            monitor.internalWorked(perDefinition);
        }
        return result;
    }

    @Override
    public byte[] getProcessDefinitionArchive(WfDefinition definition) {
        return getDefinitionService().getProcessDefinitionFile(getUser(), definition.getId(), "par");
    }

    @Override
    public WfDefinition deployProcessDefinitionArchive(byte[] par) {
        return getDefinitionService().deployProcessDefinition(getUser(), par, Lists.newArrayList("GPD"));
    }

    @Override
    public WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types) {
        return getDefinitionService().redeployProcessDefinition(getUser(), definitionId, par, types);
    }

    private BotService getBotService() {
        return getService("BotServiceBean");
    }

    @Override
    public Map<Bot, List<BotTask>> getBots() {
        Map<Bot, List<BotTask>> result = Maps.newHashMap();
        List<BotStation> botStations = getBotService().getBotStations();
        for (BotStation botStation : botStations) {
            for (Bot bot : getBotService().getBots(getUser(), botStation.getId())) {
                result.put(bot, getBotService().getBotTasks(getUser(), bot.getId()));
            }
        }
        return result;
    }

    @Override
    public byte[] getBotFile(Bot bot) {
        return getBotService().exportBot(getUser(), bot);
    }

    @Override
    public byte[] getBotTaskFile(Bot bot, String botTask) {
        return getBotService().exportBotTask(getUser(), bot, botTask);
    }

    @Override
    public void deployBot(String botStationName, byte[] archive) {
        BotStation botStation = getBotService().getBotStationByName(botStationName);
        if (botStation == null) {
            throw new BotStationDoesNotExistException(botStationName);
        }
        getBotService().importBot(getUser(), botStation, archive, true);
    }

    @Override
    public byte[] getBotStationFile(BotStation botStation) {
        return getBotService().exportBotStation(getUser(), botStation);
    }

    @Override
    public void deployBotStation(byte[] archive) {
        getBotService().importBotStation(getUser(), archive, true);
    }

    @Override
    public List<BotStation> getBotStations() {
        return getBotService().getBotStations();
    }
}
