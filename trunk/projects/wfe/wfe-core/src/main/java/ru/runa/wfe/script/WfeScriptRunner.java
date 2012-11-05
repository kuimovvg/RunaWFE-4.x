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
package ru.runa.wfe.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.user.logic.ProfileLogic;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

/**
 * Created on 26.09.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class WfeScriptRunner {
    private final static Log log = LogFactory.getLog(WfeScriptRunner.class);
    private final static String EXECUTOR_ELEMENT_NAME = "executor";
    private final static String IDENTITY_ELEMENT_NAME = "identity";
    private final static String NAMED_IDENTITY_ELEMENT_NAME = "namedIdentitySet";
    private final static String PERMISSION_ELEMENT_NAME = "permission";
    private final static String BOT_CONFIGURATION_ELEMENT_NAME = "botConfiguration";
    protected final static String NAME_ATTRIBUTE_NAME = "name";
    private final static String NEW_NAME_ATTRIBUTE_NAME = "newName";
    private final static String FULL_NAME_ATTRIBUTE_NAME = "fullName";
    private final static String DESCRIPTION_ATTRIBUTE_NAME = "description";
    protected final static String PASSWORD_ATTRIBUTE_NAME = "password";
    private final static String EXECUTOR_ATTRIBUTE_NAME = "executor";
    private final static String ADDRESS_ATTRIBUTE_NAME = "address";
    private final static String BOTSTATION_ATTRIBUTE_NAME = "botStation";
    private final static String NEW_BOTSTATION_ATTRIBUTE_NAME = "newBotStation";
    protected final static String STARTTIMEOUT_ATTRIBUTE_NAME = "startTimeout";
    protected final static String HANDLER_ATTRIBUTE_NAME = "handler";
    protected final static String TYPE_ATTRIBUTE_NAME = "type";
    protected final static String FILE_ATTRIBUTE_NAME = "file";
    protected final static String DEFINITION_ID_ATTRIBUTE_NAME = "definitionId";
    protected final static String CONFIGURATION_STRING_ATTRIBUTE_NAME = "configuration";
    protected final static String CONFIGURATION_CONTENT_ATTRIBUTE_NAME = "configurationContent";
    protected final static String ID_ATTRIBUTE_NAME = "id";
    protected final static String ID_TILL_ATTRIBUTE_NAME = "idTill";
    protected final static String ONLY_FINISHED_ATTRIBUTE_NAME = "onlyFinished";
    protected final static String DATE_INTERVAL_ATTRIBUTE_NAME = "dateInterval";
    protected final static String START_DATE_ATTRIBUTE_NAME = "startDate";
    protected final static String END_DATE_ATTRIBUTE_NAME = "endDate";
    protected final static String VERSION_ATTRIBUTE_NAME = "version";
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private RelationLogic relationLogic;
    @Autowired
    private AuthorizationLogic authorizationLogic;
    @Autowired
    private DefinitionLogic definitionLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProfileLogic profileLogic;
    @Autowired
    private SubstitutionLogic substitutionLogic;
    @Autowired
    private BotLogic botLogic;
    private Subject subject;
    private byte[][] processDefinitionsBytes;
    private final Map<String, Set<String>> namedProcessDefinitionIdentities = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> namedExecutorIdentities = new HashMap<String, Set<String>>();
    private static final String XSD_PATH = "/workflowScript.xsd";
    protected static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);
    private int processDeployed;

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setProcessDefinitionsBytes(byte[][] processDefinitionsBytes) {
        this.processDefinitionsBytes = processDefinitionsBytes;
    }

    public void runScript(InputStream inputStream) throws WfeScriptException {
        try {
            processDeployed = 0;
            namedProcessDefinitionIdentities.clear();
            namedExecutorIdentities.clear();
            Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);
            Element scriptElement = document.getDocumentElement();
            NodeList scriptNodeList = scriptElement.getChildNodes();
            for (int i = 0; i < scriptNodeList.getLength(); i++) {
                Node node = scriptNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    handleElement(element);
                }
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, WfeScriptException.class);
            throw new WfeScriptException(e.getMessage());
        }
    }

    private void handleElement(Element element) throws WfeScriptException {
        try {
            String name = element.getNodeName();
            log.info("Processing element " + name + ".");
            if ("custom".equals(name)) {
                CustomWfeScriptJob job = ClassLoaderUtil.instantiate(element.getAttribute("job"));
                job.execute(subject, element);
            } else {
                Method method = this.getClass().getMethod(name, new Class[] { Element.class });
                method.invoke(this, new Object[] { element });
            }
            // HibernateSessionFactory.getSession().flush(); // TODO flushMode (PostgreSQL)
            log.info("Processing complete " + name + ".");
        } catch (Throwable e) {
            log.error("Script execution error", e);
            throwWfeScriptException(element, e);
        }
    }

    private void throwWfeScriptException(Element element, Throwable e) throws WfeScriptException {
        // if (e instanceof InvocationTargetException) {
        // e = ((InvocationTargetException) e).getTargetException();
        // }
        Throwable th = Throwables.getRootCause(e);
        Throwables.propagateIfInstanceOf(th, WfeScriptException.class);
        throw new WfeScriptException(element, th);
    }

    public Set<String> namedIdentitySet(Element element) {
        return namedIdentitySet(element, true);
    }

    private Set<String> namedIdentitySet(Element element, boolean isGlobal) {
        Set<String> result = new HashSet<String>();
        Map<String, Set<String>> storage = null;
        if (element.getAttribute("type").equals("ProcessDefinition")) {
            storage = namedProcessDefinitionIdentities;
        } else {
            storage = namedExecutorIdentities;
        }
        String name = element.getAttribute("name");
        NodeList identities = element.getElementsByTagName(IDENTITY_ELEMENT_NAME);
        for (int i = 0; i < identities.getLength(); ++i) {
            result.add(((Element) identities.item(i)).getAttribute("name"));
        }
        NodeList nestedSet = element.getElementsByTagName(NAMED_IDENTITY_ELEMENT_NAME);
        for (int i = 0; i < nestedSet.getLength(); ++i) {
            if (!((Element) nestedSet.item(i)).getAttribute("type").equals(element.getAttribute("type"))) {
                throw new InternalApplicationException("Nested nameIdentitySet type is differs from parent type (For element " + name + " and type "
                        + element.getAttribute("type"));
            }
            if (storage.containsKey(((Element) nestedSet.item(i)).getAttribute("name"))) {
                result.addAll(storage.get(((Element) nestedSet.item(i)).getAttribute("name")));
            } else {
                result.addAll(namedIdentitySet((Element) nestedSet.item(i), isGlobal));
            }
        }
        if (isGlobal && name != null) {
            if (storage.put(name, result) != null) {
                throw new InternalApplicationException("Duplicate nameIdentitySet is found for name " + name + " and type "
                        + element.getAttribute("type"));
            }
        }
        return result;
    }

    public void setActorInactive(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Actor actor = executorLogic.getActor(subject, name);
        executorLogic.setStatus(subject, actor.getId(), false);
    }

    public void createActor(Element element) throws Exception, AuthenticationException, WeakPasswordException {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String fullName = element.getAttribute(FULL_NAME_ATTRIBUTE_NAME);
        String description = element.getAttribute(DESCRIPTION_ATTRIBUTE_NAME);
        String passwd = element.getAttribute(PASSWORD_ATTRIBUTE_NAME);
        Actor actor = new Actor(name, description, fullName);
        actor = executorLogic.create(subject, actor);
        executorLogic.setPassword(subject, actor, passwd);
    }

    public void createGroup(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String description = element.getAttribute(DESCRIPTION_ATTRIBUTE_NAME);
        Group newGroup = new Group(name, description);
        executorLogic.create(subject, newGroup);
    }

    public void deleteExecutors(Element element) throws Exception {
        NodeList child = element.getChildNodes();
        ArrayList<Long> idsList = new ArrayList<Long>();
        for (int i = 0; i < child.getLength(); ++i) {
            Node node = child.item(i);
            if (node.getNodeName() != "deleteExecutor" || node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String name = ((Element) node).getAttribute(NAME_ATTRIBUTE_NAME);
            idsList.add(executorLogic.getExecutor(subject, name).getId());
        }
        executorLogic.remove(subject, idsList);
    }

    public void deleteExecutor(Element element) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        executorLogic.remove(subject, Lists.newArrayList(executorLogic.getExecutor(subject, name).getId()));
    }

    private List<Executor> getExecutors(Element element) throws Exception {
        List<Executor> result = Lists.newArrayList();
        NodeList processDefSet = element.getElementsByTagName(NAMED_IDENTITY_ELEMENT_NAME);
        for (int i = 0; i < processDefSet.getLength(); ++i) {
            Element setElement = (Element) processDefSet.item(i);
            if (!setElement.getAttribute("type").equals("Executor")) {
                continue;
            }
            if (setElement.getAttribute("name") != null && namedExecutorIdentities.containsKey(setElement.getAttribute("name"))) {
                for (String executorName : namedExecutorIdentities.get(setElement.getAttribute("name"))) {
                    result.add(executorLogic.getExecutor(subject, executorName));
                }
            } else {
                for (String executorName : namedIdentitySet(setElement, false)) {
                    result.add(executorLogic.getExecutor(subject, executorName));
                }
            }
        }
        NodeList executorNodeList = element.getElementsByTagName(EXECUTOR_ELEMENT_NAME);
        for (int i = 0; i < executorNodeList.getLength(); i++) {
            Element executorElement = (Element) executorNodeList.item(i);
            String executorName = executorElement.getAttribute(NAME_ATTRIBUTE_NAME);
            result.add(executorLogic.getExecutor(subject, executorName));
        }
        return result;
    }

    public void addExecutorsToGroup(Element element) throws Exception {
        String groupName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        List<Executor> executors = getExecutors(element);
        executorLogic.addExecutorsToGroup(subject, executors, executorLogic.getGroup(subject, groupName));
    }

    public void removeExecutorsFromGroup(Element element) throws Exception {
        String groupName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        List<Executor> executors = getExecutors(element);
        executorLogic.removeExecutorsFromGroup(subject, executors, executorLogic.getGroup(subject, groupName));
    }

    public void addPermissionsOnActor(Element element) throws Exception {
        for (Actor actor : getActor(element)) {
            addPermissionOnIdentifiable(element, actor);
        }
    }

    public void setPermissionsOnActor(Element element) throws Exception {
        for (Actor actor : getActor(element)) {
            setPermissionOnIdentifiable(element, actor);
        }
    }

    public void removePermissionsOnActor(Element element) throws Exception {
        for (Actor actor : getActor(element)) {
            removePermissionOnIdentifiable(element, actor);
        }
    }

    private Set<String> getNestedNamedIdentityNames(Element element, String identityType, Map<String, Set<String>> storage) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Set<String> result = new HashSet<String>();
        if (name != null && !name.equals("")) {
            result.add(name);
        }
        NodeList identitySet = element.getElementsByTagName(NAMED_IDENTITY_ELEMENT_NAME);
        for (int i = 0; i < identitySet.getLength(); ++i) {
            Element setElement = (Element) identitySet.item(i);
            if (!setElement.getAttribute("type").equals(identityType)) {
                continue;
            }
            if (setElement.getAttribute("name") != null && storage.containsKey(setElement.getAttribute("name"))) {
                result.addAll(storage.get(setElement.getAttribute("name")));
            } else {
                result.addAll(namedIdentitySet(setElement, false));
            }
        }
        return result;
    }

    private Set<Actor> getActor(Element element) throws Exception {
        Set<Actor> result = new HashSet<Actor>();
        for (String executorName : getNestedNamedIdentityNames(element, "Executor", namedExecutorIdentities)) {
            result.add(executorLogic.getActor(subject, executorName));
        }
        return result;
    }

    public void addPermissionsOnGroup(Element element) throws Exception {
        for (Group group : getGroup(element)) {
            addPermissionOnIdentifiable(element, group);
        }
    }

    public void setPermissionsOnGroup(Element element) throws Exception {
        for (Group group : getGroup(element)) {
            setPermissionOnIdentifiable(element, group);
        }
    }

    public void removePermissionsOnGroup(Element element) throws Exception {
        for (Group group : getGroup(element)) {
            removePermissionOnIdentifiable(element, group);
        }
    }

    private Set<Group> getGroup(Element element) throws Exception {
        Set<Group> result = new HashSet<Group>();
        for (String executorName : getNestedNamedIdentityNames(element, "Executor", namedExecutorIdentities)) {
            result.add(executorLogic.getGroup(subject, executorName));
        }
        return result;
    }

    public void addPermissionsOnProcesses(Element element) throws Exception {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.getAttribute(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            addPermissionOnIdentifiable(element, pi);
        }
    }

    public void setPermissionsOnProcesses(Element element) throws Exception {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.getAttribute(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            setPermissionOnIdentifiable(element, pi);
        }
    }

    public void removePermissionsOnProcesses(Element element) throws Exception {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.getAttribute(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            removePermissionOnIdentifiable(element, pi);
        }
    }

    private List<WfProcess> getProcesses(Element element) throws AuthenticationException {
        String processName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        return executionLogic.getProcessesForDefinitionName(subject, processName);
    }

    public void addPermissionsOnDefinition(Element element) throws Exception {
        for (WfDefinition definition : getProcessDefinitionStubs(element)) {
            addPermissionOnIdentifiable(element, definition);
        }
    }

    public void setPermissionsOnDefinition(Element element) throws Exception {
        for (WfDefinition definition : getProcessDefinitionStubs(element)) {
            setPermissionOnIdentifiable(element, definition);
        }
    }

    public void removePermissionsOnDefinition(Element element) throws Exception {
        for (WfDefinition definition : getProcessDefinitionStubs(element)) {
            removePermissionOnIdentifiable(element, definition);
        }
    }

    private Set<WfDefinition> getProcessDefinitionStubs(Element element) throws Exception {
        Set<WfDefinition> result = new HashSet<WfDefinition>();
        for (String name : getNestedNamedIdentityNames(element, "ProcessDefinition", namedProcessDefinitionIdentities)) {
            result.add(definitionLogic.getLatestProcessDefinition(subject, name));
        }
        return result;
    }

    public void deployProcessDefinition(Element element) throws Exception {
        String type = element.getAttribute(TYPE_ATTRIBUTE_NAME);
        List<String> parsedType = Lists.newArrayList();
        if (type == null || type.equals("")) {
            parsedType.add("Script");
        } else {
            int slashIdx = 0;
            while (true) {
                if (type.indexOf('/', slashIdx) != -1) {
                    parsedType.add(type.substring(slashIdx, type.indexOf('/', slashIdx)));
                    slashIdx = type.indexOf('/', slashIdx) + 1;
                } else {
                    parsedType.add(type.substring(slashIdx));
                    break;
                }
            }
        }
        definitionLogic.deployProcessDefinition(subject, processDefinitionsBytes[processDeployed++], parsedType);
    }

    public void redeployProcessDefinition(Element element) throws Exception {
        String file = element.getAttribute(FILE_ATTRIBUTE_NAME);
        String type = element.getAttribute(TYPE_ATTRIBUTE_NAME);
        String id = element.getAttribute(DEFINITION_ID_ATTRIBUTE_NAME);
        Long definitionId = Strings.isNullOrEmpty(id) ? null : new Long(id);
        List<String> parsedType = Lists.newArrayList();
        if (type == null || type.equals("")) {
            parsedType.add("Script");
        } else {
            int slashIdx = 0;
            while (true) {
                if (type.indexOf('/', slashIdx) != -1) {
                    parsedType.add(type.substring(slashIdx, type.indexOf('/', slashIdx)));
                    slashIdx = type.indexOf('/', slashIdx) + 1;
                } else {
                    parsedType.add(type.substring(slashIdx));
                    break;
                }
            }
        }
        byte[] scriptBytes = Files.toByteArray(new File(file));
        definitionLogic.redeployProcessDefinition(subject, definitionId, scriptBytes, parsedType);
    }

    public void addPermissionsOnSystem(Element element) throws Exception {
        addPermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void setPermissionsOnSystem(Element element) throws Exception {
        setPermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void removePermissionsOnSystem(Element element) throws Exception {
        removePermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void removeAllPermissionsFromProcessDefinition(Element element) throws Exception {
        for (WfDefinition definition : getProcessDefinitionStubs(element)) {
            removeAllPermissionOnIdentifiable(definition);
        }
    }

    public void removeAllPermissionsFromProcesses(Element element) throws Exception {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.getAttribute(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            removeAllPermissionOnIdentifiable(pi);
        }
    }

    public void removeAllPermissionsFromExecutor(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Executor executor = executorLogic.getExecutor(subject, name);
        removeAllPermissionOnIdentifiable(executor);
    }

    public void removeAllPermissionsFromSystem(Element element) throws Exception {
        removeAllPermissionOnIdentifiable(ASystem.INSTANCE);
    }

    private void addPermissionOnIdentifiable(Element element, Identifiable identifiable) throws Exception {
        Executor executor = executorLogic.getExecutor(subject, element.getAttribute(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.mergePermissions(permissions, ownPermissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void setPermissionOnIdentifiable(Element element, Identifiable identifiable) throws Exception {
        Executor executor = executorLogic.getExecutor(subject, element.getAttribute(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removePermissionOnIdentifiable(Element element, Identifiable identifiable) throws Exception {
        Executor executor = executorLogic.getExecutor(subject, element.getAttribute(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        Collection<Permission> ownPermissions = authorizationLogic.getOwnPermissions(subject, executor, identifiable);
        permissions = Permission.subtractPermissions(ownPermissions, permissions);
        authorizationLogic.setPermissions(subject, executor, permissions, identifiable);
    }

    private void removeAllPermissionOnIdentifiable(Identifiable identifiable) throws Exception {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        List<Executor> executors = authorizationLogic.getExecutorsWithPermission(subject, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            if (!authorizationLogic.isPrivelegedExecutor(subject, executor, identifiable)) {
                authorizationLogic.setPermissions(subject, executor, Permission.getNoPermissions(), identifiable);
            }
        }
    }

    private Collection<Permission> getPermissions(Element element, Identifiable identifiable) throws WfeScriptException {
        Permission noPermission = identifiable.getSecuredObjectType().getNoPermission();
        NodeList permissionNodeList = element.getElementsByTagName(PERMISSION_ELEMENT_NAME);
        Set<Permission> permissions = Sets.newHashSet();
        for (int i = 0; i < permissionNodeList.getLength(); i++) {
            Element permissionElement = (Element) permissionNodeList.item(i);
            String permissionName = permissionElement.getAttribute(NAME_ATTRIBUTE_NAME);
            Permission permission = noPermission.getPermission(permissionName);
            permissions.add(permission);
        }
        return permissions;
    }

    private BatchPresentation getBatchFromProfile(Profile profile, String batchID, String batchName) {
        for (BatchPresentation batch : profile.getBatchPresentations(batchID)) {
            if (batch.getName().equals(batchName)) {
                return batch;
            }
        }
        return null;
    }

    private BatchPresentation readBatchPresentation(Element element) throws Exception {
        String actorName = element.getAttribute("actorName");
        String batchName = element.getAttribute("batchName");
        String batchId = element.getAttribute("batchId");
        if (Strings.isNullOrEmpty(actorName) && Strings.isNullOrEmpty(batchName)) {
            if (BatchPresentationConsts.ID_TASKS.equals(batchId)) {
                return BatchPresentationFactory.TASKS.createDefault(batchId);
            } else if (BatchPresentationConsts.ID_PROCESSES.equals(batchId)) {
                return BatchPresentationFactory.PROCESSES.createDefault(batchId);
            } else if (BatchPresentationConsts.ID_DEFINITIONS.equals(batchId)) {
                return BatchPresentationFactory.DEFINITIONS.createDefault(batchId);
            } else {
                return BatchPresentationFactory.EXECUTORS.createDefault(batchId);
            }
        }
        return getBatchFromProfile(profileLogic.getProfile(subject, executorLogic.getExecutor(subject, actorName).getId()), batchId, batchName);
    }

    private boolean isBatchReplaceNeeded(BatchPresentation batch, Collection<BatchPresentation> templates) {
        if (batch == null) {
            return true;
        }
        for (BatchPresentation template : templates) {
            if (template.fieldEquals(batch)) {
                return true;
            }
        }
        return false;
    }

    public enum setActiveMode {
        all, changed, none
    };

    private setActiveMode readSetActiveMode(Element element) {
        String mode = element.getAttribute("setActive");
        if (mode != null && mode.equals("all")) {
            return setActiveMode.all;
        }
        if (mode != null && mode.equals("changed")) {
            return setActiveMode.changed;
        }
        return setActiveMode.none;
    }

    private boolean isTemplatesActive(Element element) {
        String mode = element.getAttribute("useTemplates");
        if (mode != null && mode.equals("no")) {
            return false;
        }
        return true;
    }

    public static class ReplicationDescr {
        private final Set<BatchPresentation> templates;
        private final setActiveMode setActive;
        private final boolean useTemplates;

        public ReplicationDescr(Set<BatchPresentation> templates, setActiveMode setActive, boolean useTemplates) {
            this.templates = templates;
            this.setActive = setActive;
            this.useTemplates = useTemplates;
        }
    }

    public void replicateBatchPresentation(Map<BatchPresentation, ReplicationDescr> replicationDescr) throws Exception {
        if (replicationDescr.isEmpty()) {
            return;
        }
        List<Actor> allActors = executorLogic.getActors(subject, BatchPresentationFactory.EXECUTORS.createDefault());
        List<Long> actorsIds = Lists.newArrayListWithExpectedSize(allActors.size());
        for (Actor actor : allActors) {
            actorsIds.add(actor.getId());
        }
        List<Profile> profiles = profileLogic.getProfiles(subject, actorsIds);
        // For all profiles
        for (Profile profile : profiles) {
            // Replicate all batches
            for (BatchPresentation replicateMe : replicationDescr.keySet()) {
                boolean useTemplates = replicationDescr.get(replicateMe).useTemplates;
                setActiveMode activeMode = replicationDescr.get(replicateMe).setActive;
                Set<BatchPresentation> templates = replicationDescr.get(replicateMe).templates;
                if (useTemplates && !isBatchReplaceNeeded(getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()), templates)) {
                    if (activeMode.equals(setActiveMode.all)
                            && getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()) != null) {
                        profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
                    }
                    continue;
                }
                BatchPresentation clon = replicateMe.clone();
                clon.setName(replicateMe.getName());
                profile.addBatchPresentation(clon);
                if (activeMode.equals(setActiveMode.all) || activeMode.equals(setActiveMode.changed)) {
                    profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
                }
            }
        }
        profileLogic.saveProfiles(subject, profiles);
    }

    public void replicateBatchPresentation(Element element) throws Exception {
        String batchPresentationNewName = element.getAttribute("batchName");
        boolean useTemplates = isTemplatesActive(element);
        NodeList batchPresentations = element.getElementsByTagName("batchPresentation");
        setActiveMode activeMode = readSetActiveMode(element);
        BatchPresentation srcBatch = null;
        Set<BatchPresentation> replaceableBatchPresentations = new HashSet<BatchPresentation>();
        for (int i = 0; i < batchPresentations.getLength(); ++i) {
            Element batchElement = (Element) batchPresentations.item(i);
            String name = batchElement.getAttribute(NAME_ATTRIBUTE_NAME);
            if (name.equals("source")) {
                if (srcBatch != null) {
                    throw new WfeScriptException("Only one source batchPresentation is allowed inside replicateBatchPresentation.");
                }
                srcBatch = readBatchPresentation(batchElement);
                continue;
            }
            if (name.equals("template")) {
                replaceableBatchPresentations.add(readBatchPresentation(batchElement));
                continue;
            }
            throw new WfeScriptException("BatchPresentation with name '" + name + "' is not allowed inside replicateBatchPresentation.");
        }
        if (srcBatch == null) {
            throw new WfeScriptException("No source BatchPresentation in replicateBatchPresentation found.");
        }
        if (batchPresentationNewName == null || batchPresentationNewName.equals("")) {
            batchPresentationNewName = srcBatch.getName();
        }
        Map<BatchPresentation, ReplicationDescr> replicationDescr = new HashMap<BatchPresentation, ReplicationDescr>();
        srcBatch = srcBatch.clone();
        srcBatch.setName(batchPresentationNewName);
        replicationDescr.put(srcBatch, new ReplicationDescr(replaceableBatchPresentations, activeMode, useTemplates));
        replicateBatchPresentation(replicationDescr);
    }

    private Set<Actor> getActors(Element element) throws ExecutorDoesNotExistException, AuthorizationException, AuthenticationException {
        Set<Actor> retVal = new HashSet<Actor>();
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Executor executor = executorLogic.getExecutor(subject, name);
        if (executor instanceof Actor) {
            retVal.add((Actor) executor);
        } else {
            retVal.addAll(executorLogic.getGroupActors(subject, (Group) executor));
        }
        return retVal;
    }

    private final static String ORGFUNC_ATTRIBUTE_NAME = "orgFunc";
    private final static String CRITERIA_ATTRIBUTE_NAME = "criteria";
    private final static String ISENABLED_ATTRIBUTE_NAME = "isEnabled";
    private final static String ISFIRST_ATTRIBUTE_NAME = "isFirst";
    private final static String ACTOR_CODE_VARIABLE = "%self_code%";
    private final static String ACTOR_ID_VARIABLE = "%self_id%";
    private final static String ACTOR_NAME_VARIABLE = "%self_name%";

    private boolean isStringMatch(String criteria, String matcher) {
        if (matcher == null) {
            return true;
        }
        if (criteria == null) {
            return false;
        }
        return criteria.equals(matcher);
    }

    private boolean isCriteriaMatch(SubstitutionCriteria substitutionCriteria, SubstitutionCriteria matcher) {
        if (substitutionCriteria == null && matcher == null) {
            return true;
        } else if (substitutionCriteria == null || matcher == null) {
            return false;
        } else {
            return (isStringMatch(substitutionCriteria.getName(), matcher.getName()) && isStringMatch(substitutionCriteria.getConf(),
                    matcher.getConf()));
        }
    }

    private String tuneOrgFunc(String orgFunc, Actor self) {
        String retVal = null;
        if (orgFunc == null) {
            return null;
        }
        retVal = orgFunc.replaceAll(ACTOR_CODE_VARIABLE, Long.toString(self.getCode()));
        retVal = retVal.replaceAll(ACTOR_ID_VARIABLE, Long.toString(self.getId()));
        retVal = retVal.replaceAll(ACTOR_NAME_VARIABLE, self.getName());
        return retVal;
    }

    private List<Substitution> getDeletedSubstitution(NodeList elements, Set<Actor> actors) throws Exception {
        if (elements.getLength() == 0) {
            return new ArrayList<Substitution>();
        }
        List<Substitution> retVal = new ArrayList<Substitution>();
        SubstitutionCriteria[] criterias = new SubstitutionCriteria[elements.getLength()];
        String[] orgFuncs = new String[elements.getLength()];
        for (int i = 0; i < elements.getLength(); ++i) {
            criterias[i] = null;
            orgFuncs[i] = null;
            if (((Element) elements.item(i)).hasAttribute(ORGFUNC_ATTRIBUTE_NAME)) {
                orgFuncs[i] = ((Element) elements.item(i)).getAttribute(ORGFUNC_ATTRIBUTE_NAME);
            }
            if (((Element) elements.item(i)).hasAttribute(CRITERIA_ATTRIBUTE_NAME)) {
                criterias[i] = substitutionLogic.getSubstitutionCriteria(subject,
                        Long.parseLong(((Element) elements.item(i)).getAttribute(CRITERIA_ATTRIBUTE_NAME)));
            }
        }
        for (Actor actor : actors) {
            for (Substitution substitution : substitutionLogic.getSubstitutions(subject, actor.getId())) {
                for (int i = 0; i < elements.getLength(); ++i) {
                    if (isCriteriaMatch(substitution.getCriteria(), criterias[i])
                            && isStringMatch(substitution.getSubstitutionOrgFunction(), tuneOrgFunc(orgFuncs[i], actor))) {
                        retVal.add(substitution);
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    private void addSubstitution(NodeList elements, Set<Actor> actors) throws Exception {
        List<Substitution> firstSub = new ArrayList<Substitution>();
        List<Substitution> lastSub = new ArrayList<Substitution>();
        if (elements.getLength() == 0) {
            return;
        }
        for (int i = 0; i < elements.getLength(); ++i) {
            String orgFunc = null;
            SubstitutionCriteria criteria = null;
            boolean isEnabled = true;
            boolean isFirst = true;
            if (((Element) elements.item(i)).hasAttribute(ORGFUNC_ATTRIBUTE_NAME)) {
                orgFunc = ((Element) elements.item(i)).getAttribute(ORGFUNC_ATTRIBUTE_NAME);
            }
            if (((Element) elements.item(i)).hasAttribute(CRITERIA_ATTRIBUTE_NAME)) {
                criteria = substitutionLogic.getSubstitutionCriteria(subject,
                        Long.parseLong(((Element) elements.item(i)).getAttribute(CRITERIA_ATTRIBUTE_NAME)));
            }
            if (((Element) elements.item(i)).hasAttribute(ISENABLED_ATTRIBUTE_NAME)) {
                isEnabled = Boolean.parseBoolean(((Element) elements.item(i)).getAttribute(ISENABLED_ATTRIBUTE_NAME));
            }
            if (((Element) elements.item(i)).hasAttribute(ISFIRST_ATTRIBUTE_NAME)) {
                isFirst = Boolean.parseBoolean(((Element) elements.item(i)).getAttribute(ISFIRST_ATTRIBUTE_NAME));
            }
            Substitution sub = null;
            if (orgFunc == null) {
                sub = new TerminatorSubstitution();
            } else {
                sub = new Substitution();
                sub.setSubstitutionOrgFunction(orgFunc);
            }
            sub.setCriteria(criteria);
            sub.setEnabled(isEnabled);
            if (isFirst) {
                firstSub.add(sub);
            } else {
                lastSub.add(sub);
            }
        }
        List<Substitution> deletedSubstitutions = new ArrayList<Substitution>();
        List<Substitution> createdSubstitutions = new ArrayList<Substitution>();
        for (Actor actor : actors) {
            List<Substitution> existing = substitutionLogic.getSubstitutions(subject, actor.getId());
            if (!firstSub.isEmpty()) {
                for (Substitution sub : existing) {
                    deletedSubstitutions.add(sub);
                }
            }
            int subIdx = 0;
            for (Substitution sub : firstSub) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
            for (Substitution sub : existing) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
            for (Substitution sub : lastSub) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
        }
        for (Substitution substitution : deletedSubstitutions) {
            substitutionLogic.delete(subject, substitution);
        }
        for (Substitution substitution : createdSubstitutions) {
            substitutionLogic.store(subject, substitution);
        }
    }

    public void changeSubstitutions(Element element) throws Exception {
        Set<Actor> actors = new HashSet<Actor>();
        NodeList executorsElements = element.getElementsByTagName(EXECUTOR_ELEMENT_NAME);
        for (int i = 0; i < executorsElements.getLength(); ++i) {
            actors.addAll(getActors((Element) executorsElements.item(i)));
        }
        try {
            List<Substitution> deleted = getDeletedSubstitution(element.getElementsByTagName("delete"), actors);
            for (Substitution substitution : deleted) {
                substitutionLogic.delete(subject, substitution);
            }
            addSubstitution(element.getElementsByTagName("add"), actors);
        } catch (SubstitutionDoesNotExistException e) {
            log.warn("Error in Substitution changing.", e);
        }
    }

    public void createBotStation(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String addr = element.getAttribute(ADDRESS_ATTRIBUTE_NAME);
        BotStation botStation = new BotStation(name, addr);
        botLogic.create(subject, botStation);
    }

    public void createBot(Element element) throws Exception {
        String botStation = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        if (botStation == null || botStation.length() == 0) {
            log.warn("BotStation name doesn`t specified");
            return;
        }
        BotStation station = new BotStation(botStation);
        station = botLogic.getBotStation(subject, station);
        createBotCommon(element, station);
    }

    protected void createBotCommon(Element element, BotStation station) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String pass = element.getAttribute(PASSWORD_ATTRIBUTE_NAME);
        String timeout = element.getAttribute(STARTTIMEOUT_ATTRIBUTE_NAME);
        if (!executorLogic.isExecutorExist(subject, name)) {
            Actor actor = new Actor(name, "bot");
            executorLogic.create(subject, actor);
            executorLogic.setPassword(subject, actor, pass);
        }
        Bot bot = new Bot();
        bot.setWfeUser(name);
        bot.setWfePass(pass);
        if (timeout != null && !timeout.equals("")) {
            bot.setLastInvoked(Long.parseLong(timeout));
        }
        bot.setBotStation(station);
        botLogic.create(subject, bot);
    }

    public void updateBot(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String newName = element.getAttribute(NEW_NAME_ATTRIBUTE_NAME);
        String pass = element.getAttribute(PASSWORD_ATTRIBUTE_NAME);
        String botStation = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        String timeout = element.getAttribute(STARTTIMEOUT_ATTRIBUTE_NAME);
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
        if (pass != null) {
            bot.setWfePass(pass);
        }
        if (timeout != null) {
            bot.setLastInvoked(Long.parseLong(timeout));
        }
        String newBotStationName = element.getAttribute(NEW_BOTSTATION_ATTRIBUTE_NAME);
        if (newBotStationName != null) {
            bot.getBotStation().setName(newBotStationName);
        }
        botLogic.update(subject, bot);
    }

    public void updateBotStation(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String newName = element.getAttribute(NEW_NAME_ATTRIBUTE_NAME);
        String address = element.getAttribute(ADDRESS_ATTRIBUTE_NAME);
        BotStation station = new BotStation(name);
        station = botLogic.getBotStation(subject, station);
        if (newName != null) {
            station.setName(newName);
        }
        if (address != null) {
            station.setAddress(address);
        }
        botLogic.update(subject, station);
    }

    public void deleteBotStation(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        BotStation botStation = new BotStation(name);
        botLogic.remove(subject, botStation);
    }

    public void deleteBot(Element element) throws Exception {
        String name = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Bot bot = new Bot();
        bot.setWfeUser(name);
        String botStationName = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botLogic.getBotStation(subject, bs));
        }
        botLogic.remove(subject, bot);
    }

    public void addPermissionsOnBotStations(Element element) throws Exception {
        addPermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    public void setPermissionsOnBotStations(Element element) throws Exception {
        setPermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    public void removePermissionsOnBotStations(Element element) throws Exception {
        removePermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    protected void addConfigurationsToBotCommon(Element element, BotStation station) throws Exception {
        String botName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (station != null) {
            bot.setBotStation(station);
        }
        bot = botLogic.getBot(subject, bot);
        NodeList taskNodeList = element.getElementsByTagName(BOT_CONFIGURATION_ELEMENT_NAME);
        for (int i = 0; i < taskNodeList.getLength(); i++) {
            Element taskElement = (Element) taskNodeList.item(i);
            String name = taskElement.getAttribute(NAME_ATTRIBUTE_NAME);
            String handler = taskElement.getAttribute(HANDLER_ATTRIBUTE_NAME);
            BotTask task = new BotTask();
            task.setBot(bot);
            task.setName(name);
            if (handler == null) {
                handler = "";
            }
            task.setClazz(handler);
            String config = taskElement.getAttribute(CONFIGURATION_STRING_ATTRIBUTE_NAME);
            byte[] configuration;
            if (config == null || config.length() == 0) {
                String configContent = taskElement.getAttribute(CONFIGURATION_CONTENT_ATTRIBUTE_NAME);
                if (configContent == null) {
                    configContent = taskElement.getNodeValue();
                }
                if (configContent != null) {
                    configuration = configContent.trim().getBytes("UTF-8");
                } else {
                    configuration = new byte[0];
                }
            } else {
                configuration = getBotTaskConfiguration(config);
            }
            log.info("adding bot configuration element: " + name + " with conf: " + config);
            task.setConfiguration(configuration);
            botLogic.create(subject, task);
        }
    }

    protected byte[] getBotTaskConfiguration(String config) throws IOException {
        InputStream is = null;
        try {
            is = ClassLoaderUtil.getResourceAsStream(config, getClass());
            Preconditions.checkNotNull(is, "No resource available: " + config);
            byte[] result = new byte[is.available()];
            is.read(result);
            return result;
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    public void addConfigurationsToBot(Element element) throws Exception {
        String botstationName = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        BotStation bs = null;
        if (botstationName != null && botstationName.length() > 0) {
            bs = new BotStation(botstationName);
            bs = botLogic.getBotStation(subject, bs);
        }
        addConfigurationsToBotCommon(element, bs);
    }

    public void removeConfigurationsFromBot(Element element) throws Exception {
        String botStationName = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        BotStation bs = null;
        if (botStationName != null) {
            bs = new BotStation(botStationName);
            bs = botLogic.getBotStation(subject, bs);
        }
        removeConfigurationsFromBotCommon(element, bs);
    }

    public void removeConfigurationsFromBotCommon(Element element, BotStation botstation) throws Exception {
        String botName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Bot bot = new Bot();
        bot.setWfeUser(botName);
        if (botstation != null) {
            bot.setBotStation(botstation);
        }
        bot = botLogic.getBot(subject, bot);
        NodeList taskNodeList = element.getElementsByTagName(BOT_CONFIGURATION_ELEMENT_NAME);
        for (int i = 0; i < taskNodeList.getLength(); i++) {
            Element taskElement = (Element) taskNodeList.item(i);
            String name = taskElement.getAttribute(NAME_ATTRIBUTE_NAME);
            BotTask task = new BotTask();
            task.setName(name);
            task.setBot(bot);
            botLogic.remove(subject, task);
        }
    }

    public void removeAllConfigurationsFromBot(Element element) throws Exception {
        String botName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        Bot bot = new Bot();
        bot.setWfeUser(botName);
        String botStationName = element.getAttribute(BOTSTATION_ATTRIBUTE_NAME);
        if (botStationName != null) {
            BotStation bs = new BotStation(botStationName);
            bot.setBotStation(botLogic.getBotStation(subject, bs));
        }
        bot = botLogic.getBot(subject, bot);
        if (bot == null) {
            log.warn("BotRunner " + botName + "doesn't exist");
        }
        for (BotTask task : botLogic.getBotTaskList(subject, bot)) {
            botLogic.remove(subject, task);
        }
    }

    public void removeOldProcesses(Element element) throws Exception {
        operationWithOldProcesses(element, 1);
    }

    public void removeOldProcessDefinitionVersion(Element element) throws Exception {
        operationWithOldProcessDefinitionVersion(element, 1);
    }

    public void archiveOldProcesses(Element element) throws Exception {
        operationWithOldProcesses(element, 2);
    }

    public void archiveOldProcessDefinitionVersion(Element element) throws Exception {
        operationWithOldProcessDefinitionVersion(element, 2);
    }

    public void retrieveOldProcesses(Element element) throws Exception {
        operationWithOldProcesses(element, 3);
    }

    public void retrieveOldProcessDefinitionVersion(Element element) throws Exception {
        operationWithOldProcessDefinitionVersion(element, 3);
    }

    /* @param operation: remove - 1; archiving - 2; retrieve from archive - 3 */
    private void operationWithOldProcesses(Element element, int operation) throws Exception {
//        String prInstName = element.getAttribute(NAME_ATTRIBUTE_NAME);
//        String prInstVersion = element.getAttribute(VERSION_ATTRIBUTE_NAME);
//        String prInstId = element.getAttribute(ID_ATTRIBUTE_NAME);
//        String prInstIdTill = element.getAttribute(ID_TILL_ATTRIBUTE_NAME);
//        String prInstStartDate = element.getAttribute(START_DATE_ATTRIBUTE_NAME);
//        String prInstEndDate = element.getAttribute(END_DATE_ATTRIBUTE_NAME);
//        String prInstOnlyFinished = element.getAttribute(ONLY_FINISHED_ATTRIBUTE_NAME);
//        String prInstDateInterval = element.getAttribute(DATE_INTERVAL_ATTRIBUTE_NAME);
//        boolean onlyFinished = prInstOnlyFinished == null || prInstOnlyFinished.trim().length() == 0 ? true : Boolean
//                .parseBoolean(prInstOnlyFinished);
//        boolean dateInterval = prInstDateInterval == null || prInstDateInterval.trim().length() == 0 ? false : Boolean
//                .parseBoolean(prInstDateInterval);
//        int version = prInstVersion == null || prInstVersion.trim().length() == 0 ? 0 : Integer.parseInt(prInstVersion);
//        Long id = Strings.isNullOrEmpty(prInstId) ? null : Long.parseLong(prInstId);
//        Long idTill = Strings.isNullOrEmpty(prInstIdTill) ? null : Long.parseLong(prInstIdTill);
//        Date startDate = null;
//        Date finishDate = null;
//        if (prInstId == null) {
//            if (prInstStartDate == null && prInstEndDate == null) {
//                return;
//            }
//        }
//        if (prInstStartDate != null && prInstStartDate.trim().length() > 0) {
//            startDate = CalendarUtil.convertToDate(prInstStartDate, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
//        }
//        if (prInstEndDate != null && prInstEndDate.trim().length() > 0) {
//            finishDate = CalendarUtil.convertToDate(prInstEndDate, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
//        }
        // switch (operation) {
        // case 1:
        // archLogic.removeProcess(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
        // break;
        // case 2:
        // archLogic.archiveProcesses(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
        // break;
        // case 3:
        // archLogic.restoreProcesses(subject, startDate, finishDate, prInstName, version, id, idTill, onlyFinished, dateInterval);
        // break;
        // }
    }

    /* @param number: remove - 1; archiving - 2; retrieve from archive - 3 */
    private void operationWithOldProcessDefinitionVersion(Element element, int operation) throws Exception {
//        String defName = element.getAttribute(NAME_ATTRIBUTE_NAME);
//        String version = element.getAttribute(VERSION_ATTRIBUTE_NAME);
//        String versionTo = element.getAttribute("versionTo");
        // switch (operation) {
        // case 1:
        // archLogic.removeProcessDefinition(subject, defName, version == null || version.trim().length() == 0 ? 0 : Integer.parseInt(version),
        // versionTo == null || versionTo.trim().length() == 0 ? 0 : Integer.parseInt(versionTo));
        // break;
        // case 2:
        // archLogic.archiveProcessDefinition(subject, defName, version == null || version.trim().length() == 0 ? 0 : Integer.parseInt(version));
        // break;
        // case 3:
        // archLogic.restoreProcessDefinitionFromArchive(subject, defName,
        // version == null || version.trim().length() == 0 ? 0 : Integer.parseInt(version));
        // break;
        // }
    }

    public void relation(Element element) throws Exception {
        String relationName = element.getAttribute(NAME_ATTRIBUTE_NAME);
        String relationDescription = element.getAttribute(DESCRIPTION_ATTRIBUTE_NAME);
        boolean isExists = false;
        for (Relation group : relationLogic.getRelations(subject, BatchPresentationFactory.RELATION_GROUPS.createDefault())) {
            isExists = isExists || (group.getName().compareToIgnoreCase(relationName) == 0);
        }
        if (!isExists) {
            relationLogic.createRelation(subject, relationName, relationDescription);
        }
        Collection<Executor> leftExecutors = getExecutors((Element) element.getElementsByTagName("left").item(0));
        Collection<Executor> rightExecutors = getExecutors((Element) element.getElementsByTagName("right").item(0));
        if (leftExecutors.isEmpty() || rightExecutors.isEmpty()) {
            return;
        }
        for (Executor right : rightExecutors) {
            for (Executor left : leftExecutors) {
                relationLogic.addRelationPair(subject, relationName, left, right);
            }
        }
    }

    public void stopProcess(Element element) throws Exception {
        String id = element.getAttribute(ID_ATTRIBUTE_NAME);
        Long processId = Strings.isNullOrEmpty(id) ? null : Long.parseLong(id);
        executionLogic.cancelProcess(subject, processId);
    }
}
