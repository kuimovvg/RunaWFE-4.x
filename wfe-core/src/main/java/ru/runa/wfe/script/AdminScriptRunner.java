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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.user.logic.ProfileLogic;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

/**
 * 
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class AdminScriptRunner {
    private final static Log log = LogFactory.getLog(AdminScriptRunner.class);
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
    protected final static String ID_FROM_ATTRIBUTE_NAME = "idFrom";
    protected final static String ID_TO_ATTRIBUTE_NAME = "idTo";
    protected final static String ONLY_FINISHED_ATTRIBUTE_NAME = "onlyFinished";
    protected final static String START_DATE_FROM_ATTRIBUTE_NAME = "startDateFrom";
    protected final static String START_DATE_TO_ATTRIBUTE_NAME = "startDateTo";
    protected final static String END_DATE_FROM_ATTRIBUTE_NAME = "endDateFrom";
    protected final static String END_DATE_TO_ATTRIBUTE_NAME = "endDateTo";
    protected final static String VERSION_ATTRIBUTE_NAME = "version";
    protected final static String VERSION_FROM_ATTRIBUTE_NAME = "versionFrom";
    protected final static String VERSION_TO_ATTRIBUTE_NAME = "versionTo";
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
    private User user;
    private byte[][] processDefinitionsBytes;
    private final Map<String, Set<String>> namedProcessDefinitionIdentities = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> namedExecutorIdentities = new HashMap<String, Set<String>>();
    private int processDeployed;

    public void setUser(User user) {
        this.user = user;
    }

    public void setProcessDefinitionsBytes(byte[][] processDefinitionsBytes) {
        this.processDefinitionsBytes = processDefinitionsBytes;
    }

    public void runScript(byte[] scriptXml) throws AdminScriptException {
        processDeployed = 0;
        namedProcessDefinitionIdentities.clear();
        namedExecutorIdentities.clear();
        Document document = XmlUtils.parseWithXSDValidation(scriptXml, "workflowScript.xsd");
        Element scriptElement = document.getRootElement();
        List<Element> elements = scriptElement.elements();
        for (Element element : elements) {
            handleElement(element);
        }
    }

    private void handleElement(Element element) throws AdminScriptException {
        try {
            String name = element.getName();
            log.info("Processing element " + name + ".");
            if ("custom".equals(name)) {
                CustomAdminScriptJob job = ClassLoaderUtil.instantiate(element.attributeValue("job"));
                job.execute(user, element);
            } else {
                Method method = this.getClass().getMethod(name, new Class[] { Element.class });
                method.invoke(this, new Object[] { element });
            }
            log.info("Processing complete " + name + ".");
        } catch (Throwable th) {
            Throwables.propagateIfInstanceOf(th, AdminScriptException.class);
            throw new AdminScriptException(element, th);
        }
    }

    public Set<String> namedIdentitySet(Element element) {
        return namedIdentitySet(element, true);
    }

    private Set<String> namedIdentitySet(Element element, boolean isGlobal) {
        Set<String> result = new HashSet<String>();
        Map<String, Set<String>> storage = null;
        if ("ProcessDefinition".equals(element.attributeValue("type"))) {
            storage = namedProcessDefinitionIdentities;
        } else {
            storage = namedExecutorIdentities;
        }
        String name = element.attributeValue("name");
        List<Element> identities = element.elements(IDENTITY_ELEMENT_NAME);
        for (Element e : identities) {
            result.add(e.attributeValue("name"));
        }
        List<Element> nestedSet = element.elements(NAMED_IDENTITY_ELEMENT_NAME);
        for (Element e : nestedSet) {
            if (!e.attributeValue("type").equals(element.attributeValue("type"))) {
                throw new InternalApplicationException("Nested nameIdentitySet type is differs from parent type (For element " + name + " and type "
                        + element.attributeValue("type"));
            }
            if (storage.containsKey(e.attributeValue("name"))) {
                result.addAll(storage.get(e.attributeValue("name")));
            } else {
                result.addAll(namedIdentitySet(e, isGlobal));
            }
        }
        if (isGlobal && name != null) {
            if (storage.put(name, result) != null) {
                throw new InternalApplicationException("Duplicate nameIdentitySet is found for name " + name + " and type "
                        + element.attributeValue("type"));
            }
        }
        return result;
    }

    public void setActorInactive(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Actor actor = executorLogic.getActor(user, name);
        executorLogic.setStatus(user, actor, false, true);
    }

    public void createActor(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String fullName = element.attributeValue(FULL_NAME_ATTRIBUTE_NAME);
        String description = element.attributeValue(DESCRIPTION_ATTRIBUTE_NAME);
        String password = element.attributeValue(PASSWORD_ATTRIBUTE_NAME);
        Actor actor = new Actor(name, description, fullName);
        actor = executorLogic.create(user, actor);
        if (password != null) {
            executorLogic.setPassword(user, actor, password);
        }
    }

    public void createGroup(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String description = element.attributeValue(DESCRIPTION_ATTRIBUTE_NAME);
        Group newGroup = new Group(name, description);
        executorLogic.create(user, newGroup);
    }

    public void deleteExecutors(Element element) {
        List<Element> children = element.elements();
        ArrayList<Long> idsList = new ArrayList<Long>();
        for (Element e : children) {
            if (!Objects.equal(e.getName(), "deleteExecutor")) {
                continue;
            }
            String name = e.attributeValue(NAME_ATTRIBUTE_NAME);
            idsList.add(executorLogic.getExecutor(user, name).getId());
        }
        executorLogic.remove(user, idsList);
    }

    public void deleteExecutor(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        executorLogic.remove(user, Lists.newArrayList(executorLogic.getExecutor(user, name).getId()));
    }

    private List<Executor> getExecutors(Element element) {
        List<Executor> result = Lists.newArrayList();
        List<Element> processDefSet = element.elements(NAMED_IDENTITY_ELEMENT_NAME);
        for (Element setElement : processDefSet) {
            if (!"Executor".equals(setElement.attributeValue("type"))) {
                continue;
            }
            if (setElement.attributeValue("name") != null && namedExecutorIdentities.containsKey(setElement.attributeValue("name"))) {
                for (String executorName : namedExecutorIdentities.get(setElement.attributeValue("name"))) {
                    result.add(executorLogic.getExecutor(user, executorName));
                }
            } else {
                for (String executorName : namedIdentitySet(setElement, false)) {
                    result.add(executorLogic.getExecutor(user, executorName));
                }
            }
        }
        List<Element> executorNodeList = element.elements(EXECUTOR_ELEMENT_NAME);
        for (Element executorElement : executorNodeList) {
            String executorName = executorElement.attributeValue(NAME_ATTRIBUTE_NAME);
            result.add(executorLogic.getExecutor(user, executorName));
        }
        return result;
    }

    public void addExecutorsToGroup(Element element) {
        String groupName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        List<Executor> executors = getExecutors(element);
        executorLogic.addExecutorsToGroup(user, executors, executorLogic.getGroup(user, groupName));
    }

    public void removeExecutorsFromGroup(Element element) {
        String groupName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        List<Executor> executors = getExecutors(element);
        executorLogic.removeExecutorsFromGroup(user, executors, executorLogic.getGroup(user, groupName));
    }

    public void addPermissionsOnActor(Element element) {
        for (Actor actor : getActor(element)) {
            addPermissionOnIdentifiable(element, actor);
        }
    }

    public void setPermissionsOnActor(Element element) {
        for (Actor actor : getActor(element)) {
            setPermissionOnIdentifiable(element, actor);
        }
    }

    public void removePermissionsOnActor(Element element) {
        for (Actor actor : getActor(element)) {
            removePermissionOnIdentifiable(element, actor);
        }
    }

    private Set<String> getNestedNamedIdentityNames(Element element, String identityType, Map<String, Set<String>> storage) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Set<String> result = new HashSet<String>();
        if (name != null && !name.equals("")) {
            result.add(name);
        }
        List<Element> identitySet = element.elements(NAMED_IDENTITY_ELEMENT_NAME);
        for (Element setElement : identitySet) {
            if (!identityType.equals(setElement.attributeValue("type"))) {
                continue;
            }
            if (setElement.attributeValue("name") != null && storage.containsKey(setElement.attributeValue("name"))) {
                result.addAll(storage.get(setElement.attributeValue("name")));
            } else {
                result.addAll(namedIdentitySet(setElement, false));
            }
        }
        return result;
    }

    private Set<Actor> getActor(Element element) {
        Set<Actor> result = new HashSet<Actor>();
        for (String executorName : getNestedNamedIdentityNames(element, "Executor", namedExecutorIdentities)) {
            result.add(executorLogic.getActor(user, executorName));
        }
        return result;
    }

    public void addPermissionsOnGroup(Element element) {
        for (Group group : getGroup(element)) {
            addPermissionOnIdentifiable(element, group);
        }
    }

    public void setPermissionsOnGroup(Element element) {
        for (Group group : getGroup(element)) {
            setPermissionOnIdentifiable(element, group);
        }
    }

    public void removePermissionsOnGroup(Element element) {
        for (Group group : getGroup(element)) {
            removePermissionOnIdentifiable(element, group);
        }
    }

    private Set<Group> getGroup(Element element) {
        Set<Group> result = new HashSet<Group>();
        for (String executorName : getNestedNamedIdentityNames(element, "Executor", namedExecutorIdentities)) {
            result.add(executorLogic.getGroup(user, executorName));
        }
        return result;
    }

    public void addPermissionsOnProcesses(Element element) {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.attributeValue(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            addPermissionOnIdentifiable(element, pi);
        }
    }

    public void setPermissionsOnProcesses(Element element) {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.attributeValue(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            setPermissionOnIdentifiable(element, pi);
        }
    }

    public void removePermissionsOnProcesses(Element element) {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.attributeValue(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            removePermissionOnIdentifiable(element, pi);
        }
    }

    private List<WfProcess> getProcesses(Element element) {
        String processName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        return executionLogic.getProcessesForDefinitionName(user, processName);
    }

    public void addPermissionsOnDefinition(Element element) {
        for (WfDefinition definition : getProcessDefinitions(element)) {
            addPermissionOnIdentifiable(element, definition);
        }
    }

    public void setPermissionsOnDefinition(Element element) {
        for (WfDefinition definition : getProcessDefinitions(element)) {
            setPermissionOnIdentifiable(element, definition);
        }
    }

    public void removePermissionsOnDefinition(Element element) {
        for (WfDefinition definition : getProcessDefinitions(element)) {
            removePermissionOnIdentifiable(element, definition);
        }
    }

    private Set<WfDefinition> getProcessDefinitions(Element element) {
        Set<WfDefinition> result = new HashSet<WfDefinition>();
        for (String name : getNestedNamedIdentityNames(element, "ProcessDefinition", namedProcessDefinitionIdentities)) {
            result.add(definitionLogic.getLatestProcessDefinition(user, name));
        }
        return result;
    }

    public void deployProcessDefinition(Element element) {
        String type = element.attributeValue(TYPE_ATTRIBUTE_NAME);
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
        definitionLogic.deployProcessDefinition(user, processDefinitionsBytes[processDeployed++], parsedType);
    }

    public void redeployProcessDefinition(Element element) {
        String file = element.attributeValue(FILE_ATTRIBUTE_NAME);
        String type = element.attributeValue(TYPE_ATTRIBUTE_NAME);
        String definitionName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Long definitionId;
        if (definitionName != null) {
            definitionId = ApplicationContextFactory.getDeploymentDAO().findLatestDeployment(definitionName).getId();
        } else {
            String id = element.attributeValue(DEFINITION_ID_ATTRIBUTE_NAME);
            Preconditions.checkNotNull(id, DEFINITION_ID_ATTRIBUTE_NAME);
            definitionId = Long.parseLong(id);
        }
        List<String> parsedType = null;
        if (type != null) {
            parsedType = Lists.newArrayList();
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
        try {
            byte[] scriptBytes = Files.toByteArray(new File(file));
            definitionLogic.redeployProcessDefinition(user, definitionId, scriptBytes, parsedType);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void addPermissionsOnSystem(Element element) {
        addPermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void setPermissionsOnSystem(Element element) {
        setPermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void removePermissionsOnSystem(Element element) {
        removePermissionOnIdentifiable(element, ASystem.INSTANCE);
    }

    public void removeAllPermissionsFromProcessDefinition(Element element) {
        for (WfDefinition definition : getProcessDefinitions(element)) {
            removeAllPermissionOnIdentifiable(definition);
        }
    }

    public void removeAllPermissionsFromProcesses(Element element) {
        List<WfProcess> list = getProcesses(element);
        if (list.size() == 0) {
            log.warn("Process " + element.attributeValue(NAME_ATTRIBUTE_NAME) + "doesn't exist");
        }
        for (WfProcess pi : list) {
            removeAllPermissionOnIdentifiable(pi);
        }
    }

    public void removeAllPermissionsFromExecutor(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Executor executor = executorLogic.getExecutor(user, name);
        removeAllPermissionOnIdentifiable(executor);
    }

    public void removeAllPermissionsFromSystem(Element element) {
        removeAllPermissionOnIdentifiable(ASystem.INSTANCE);
    }

    private void addPermissionOnIdentifiable(Element element, Identifiable identifiable) {
        Executor executor = executorLogic.getExecutor(user, element.attributeValue(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        List<Permission> ownPermissions = authorizationLogic.getIssuedPermissions(user, executor, identifiable);
        permissions = Permission.mergePermissions(permissions, ownPermissions);
        authorizationLogic.setPermissions(user, executor, permissions, identifiable);
    }

    private void setPermissionOnIdentifiable(Element element, Identifiable identifiable) {
        Executor executor = executorLogic.getExecutor(user, element.attributeValue(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        authorizationLogic.setPermissions(user, executor, permissions, identifiable);
    }

    private void removePermissionOnIdentifiable(Element element, Identifiable identifiable) {
        Executor executor = executorLogic.getExecutor(user, element.attributeValue(EXECUTOR_ATTRIBUTE_NAME));
        Collection<Permission> permissions = getPermissions(element, identifiable);
        List<Permission> ownPermissions = authorizationLogic.getIssuedPermissions(user, executor, identifiable);
        permissions = Permission.subtractPermissions(ownPermissions, permissions);
        authorizationLogic.setPermissions(user, executor, permissions, identifiable);
    }

    private void removeAllPermissionOnIdentifiable(Identifiable identifiable) {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<? extends Executor> executors = authorizationLogic.getExecutorsWithPermission(user, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            if (!authorizationLogic.isPrivelegedExecutor(user, executor, identifiable)) {
                authorizationLogic.setPermissions(user, executor, Permission.getNoPermissions(), identifiable);
            }
        }
    }

    private Collection<Permission> getPermissions(Element element, Identifiable identifiable) {
        Permission noPermission = identifiable.getSecuredObjectType().getNoPermission();
        List<Element> permissionElements = element.elements(PERMISSION_ELEMENT_NAME);
        Set<Permission> permissions = Sets.newHashSet();
        for (Element permissionElement : permissionElements) {
            String permissionName = permissionElement.attributeValue(NAME_ATTRIBUTE_NAME);
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

    private BatchPresentation readBatchPresentation(Element element) {
        String actorName = element.attributeValue("actorName");
        String batchName = element.attributeValue("batchName");
        String batchId = element.attributeValue("batchId");
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
        return getBatchFromProfile(profileLogic.getProfile(user, executorLogic.getExecutor(user, actorName).getId()), batchId, batchName);
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
        ALL, CHANGED, NONE
    };

    private setActiveMode readSetActiveMode(Element element) {
        String mode = element.attributeValue("setActive");
        if (mode != null && mode.equals("all")) {
            return setActiveMode.ALL;
        }
        if (mode != null && mode.equals("changed")) {
            return setActiveMode.CHANGED;
        }
        return setActiveMode.NONE;
    }

    private boolean isTemplatesActive(Element element) {
        String mode = element.attributeValue("useTemplates");
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

    public void replicateBatchPresentation(Map<BatchPresentation, ReplicationDescr> replicationDescr) {
        if (replicationDescr.isEmpty()) {
            return;
        }
        List<Actor> allActors = (List<Actor>) executorLogic.getExecutors(user, BatchPresentationFactory.ACTORS.createNonPaged());
        List<Long> actorsIds = Lists.newArrayListWithExpectedSize(allActors.size());
        for (Actor actor : allActors) {
            actorsIds.add(actor.getId());
        }
        List<Profile> profiles = profileLogic.getProfiles(user, actorsIds);
        // For all profiles
        for (Profile profile : profiles) {
            // Replicate all batches
            for (BatchPresentation replicateMe : replicationDescr.keySet()) {
                boolean useTemplates = replicationDescr.get(replicateMe).useTemplates;
                setActiveMode activeMode = replicationDescr.get(replicateMe).setActive;
                Set<BatchPresentation> templates = replicationDescr.get(replicateMe).templates;
                if (useTemplates && !isBatchReplaceNeeded(getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()), templates)) {
                    if (activeMode.equals(setActiveMode.ALL)
                            && getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()) != null) {
                        profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
                    }
                    continue;
                }
                BatchPresentation clon = replicateMe.clone();
                clon.setName(replicateMe.getName());
                profile.addBatchPresentation(clon);
                if (activeMode.equals(setActiveMode.ALL) || activeMode.equals(setActiveMode.CHANGED)) {
                    profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
                }
            }
        }
        profileLogic.updateProfiles(user, profiles);
    }

    public void replicateBatchPresentation(Element element) {
        String batchPresentationNewName = element.attributeValue("batchName");
        boolean useTemplates = isTemplatesActive(element);
        List<Element> batchPresentations = element.elements("batchPresentation");
        setActiveMode activeMode = readSetActiveMode(element);
        BatchPresentation srcBatch = null;
        Set<BatchPresentation> replaceableBatchPresentations = new HashSet<BatchPresentation>();
        for (Element batchElement : batchPresentations) {
            String name = batchElement.attributeValue(NAME_ATTRIBUTE_NAME);
            if (name.equals("source")) {
                if (srcBatch != null) {
                    throw new AdminScriptException("Only one source batchPresentation is allowed inside replicateBatchPresentation.");
                }
                srcBatch = readBatchPresentation(batchElement);
                continue;
            }
            if (name.equals("template")) {
                replaceableBatchPresentations.add(readBatchPresentation(batchElement));
                continue;
            }
            throw new AdminScriptException("BatchPresentation with name '" + name + "' is not allowed inside replicateBatchPresentation.");
        }
        if (srcBatch == null) {
            throw new AdminScriptException("No source BatchPresentation in replicateBatchPresentation found.");
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

    private Set<Actor> getActors(Element element) {
        Set<Actor> retVal = new HashSet<Actor>();
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Executor executor = executorLogic.getExecutor(user, name);
        if (executor instanceof Actor) {
            retVal.add((Actor) executor);
        } else {
            retVal.addAll(executorLogic.getGroupActors(user, (Group) executor));
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
            return (isStringMatch(substitutionCriteria.getName(), matcher.getName()) && isStringMatch(substitutionCriteria.getConfiguration(),
                    matcher.getConfiguration()));
        }
    }

    private String tuneOrgFunc(String orgFunction, Actor actor) {
        if (orgFunction == null) {
            return null;
        }
        String result = orgFunction.replaceAll(ACTOR_CODE_VARIABLE, Long.toString(actor.getCode()));
        result = result.replaceAll(ACTOR_ID_VARIABLE, Long.toString(actor.getId()));
        result = result.replaceAll(ACTOR_NAME_VARIABLE, actor.getName());
        return result;
    }

    private List<Long> getSubstitutionIdsToDelete(List<Element> elements, Set<Actor> actors) {
        if (elements.size() == 0) {
            return new ArrayList<Long>();
        }
        List<Long> result = Lists.newArrayList();
        List<SubstitutionCriteria> criterias = Lists.newArrayList();
        List<String> orgFunctions = Lists.newArrayList();
        for (Element element : elements) {
            orgFunctions.add(element.attributeValue(ORGFUNC_ATTRIBUTE_NAME));
            String criteria = element.attributeValue(CRITERIA_ATTRIBUTE_NAME);
            if (!Strings.isNullOrEmpty(criteria)) {
                criterias.add(substitutionLogic.getCriteria(user, Long.parseLong(criteria)));
            } else {
                criterias.add(null);
            }
        }
        for (Actor actor : actors) {
            for (Substitution substitution : substitutionLogic.getSubstitutions(user, actor.getId())) {
                for (int i = 0; i < elements.size(); ++i) {
                    if (isCriteriaMatch(substitution.getCriteria(), criterias.get(i))
                            && isStringMatch(substitution.getOrgFunction(), tuneOrgFunc(orgFunctions.get(i), actor))) {
                        result.add(substitution.getId());
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void addSubstitutions(List<Element> elements, Set<Actor> actors) {
        for (Element element : elements) {
            String orgFunction = element.attributeValue(ORGFUNC_ATTRIBUTE_NAME);
            SubstitutionCriteria criteria = null;
            boolean enabled = Boolean.parseBoolean(element.attributeValue(ISENABLED_ATTRIBUTE_NAME, "true"));
            boolean first = Boolean.parseBoolean(element.attributeValue(ISFIRST_ATTRIBUTE_NAME, "true"));
            if (element.attribute(CRITERIA_ATTRIBUTE_NAME) != null) {
                criteria = substitutionLogic.getCriteria(user, Long.parseLong(element.attributeValue(CRITERIA_ATTRIBUTE_NAME)));
            }
            for (Actor actor : actors) {
                Substitution substitution;
                if (orgFunction == null) {
                    substitution = new TerminatorSubstitution();
                } else {
                    substitution = new Substitution();
                    substitution.setOrgFunction(orgFunction);
                }
                substitution.setActorId(actor.getId());
                substitution.setCriteria(criteria);
                substitution.setEnabled(enabled);
                if (first) {
                    substitution.setPosition(0);
                }
                substitutionLogic.create(user, substitution);
            }
        }
    }

    public void changeSubstitutions(Element element) {
        Set<Actor> actors = new HashSet<Actor>();
        List<Element> executorsElements = element.elements(EXECUTOR_ELEMENT_NAME);
        for (Element e : executorsElements) {
            actors.addAll(getActors(e));
        }
        List<Long> idsToDelete = getSubstitutionIdsToDelete(element.elements("delete"), actors);
        substitutionLogic.delete(user, idsToDelete);
        addSubstitutions(element.elements("add"), actors);
    }

    public void createBotStation(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String addr = element.attributeValue(ADDRESS_ATTRIBUTE_NAME);
        botLogic.createBotStation(user, new BotStation(name, addr));
    }

    public void createBot(Element element) throws Exception {
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        if (botStationName == null || botStationName.length() == 0) {
            log.warn("BotStation name doesn`t specified");
            return;
        }
        BotStation station = botLogic.getBotStationNotNull(botStationName);
        createBotCommon(element, station);
    }

    protected void createBotCommon(Element element, BotStation botStation) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String pass = element.attributeValue(PASSWORD_ATTRIBUTE_NAME);
        String timeout = element.attributeValue(STARTTIMEOUT_ATTRIBUTE_NAME);
        if (!executorLogic.isExecutorExist(user, name)) {
            Actor actor = new Actor(name, "bot");
            executorLogic.create(user, actor);
            executorLogic.setPassword(user, actor, pass);
        }
        Bot bot = botLogic.getBot(user, botStation.getId(), name);
        if (bot == null) {
            bot = new Bot();
            bot.setBotStation(botStation);
            bot.setUsername(name);
            bot.setPassword(pass);
            if (!Strings.isNullOrEmpty(timeout)) {
                bot.setStartTimeout(Long.parseLong(timeout));
            }
            botLogic.createBot(user, bot);
        }
    }

    public void updateBot(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String newName = element.attributeValue(NEW_NAME_ATTRIBUTE_NAME);
        String pass = element.attributeValue(PASSWORD_ATTRIBUTE_NAME);
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        String timeout = element.attributeValue(STARTTIMEOUT_ATTRIBUTE_NAME);
        Bot bot = botLogic.getBotNotNull(user, botLogic.getBotStationNotNull(botStationName).getId(), name);
        if (!Strings.isNullOrEmpty(newName)) {
            bot.setUsername(newName);
        }
        if (!Strings.isNullOrEmpty(pass)) {
            bot.setPassword(pass);
        }
        if (!Strings.isNullOrEmpty(timeout)) {
            bot.setStartTimeout(Long.parseLong(timeout));
        }
        String newBotStationName = element.attributeValue(NEW_BOTSTATION_ATTRIBUTE_NAME);
        if (newBotStationName != null) {
            bot.getBotStation().setName(newBotStationName);
        }
        botLogic.updateBot(user, bot);
    }

    public void updateBotStation(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String newName = element.attributeValue(NEW_NAME_ATTRIBUTE_NAME);
        String address = element.attributeValue(ADDRESS_ATTRIBUTE_NAME);
        BotStation station = botLogic.getBotStationNotNull(name);
        if (!Strings.isNullOrEmpty(newName)) {
            station.setName(newName);
        }
        if (!Strings.isNullOrEmpty(address)) {
            station.setAddress(address);
        }
        botLogic.updateBotStation(user, station);
    }

    public void deleteBotStation(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        BotStation botStation = botLogic.getBotStationNotNull(name);
        botLogic.removeBotStation(user, botStation.getId());
    }

    public void deleteBot(Element element) {
        String name = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        Bot bot = botLogic.getBotNotNull(user, botLogic.getBotStationNotNull(botStationName).getId(), name);
        botLogic.removeBot(user, bot.getId());
    }

    public void addPermissionsOnBotStations(Element element) {
        addPermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    public void setPermissionsOnBotStations(Element element) {
        setPermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    public void removePermissionsOnBotStations(Element element) {
        removePermissionOnIdentifiable(element, BotStation.INSTANCE);
    }

    protected void addConfigurationsToBotCommon(Element element, BotStation botStation) {
        String botName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Bot bot = botLogic.getBotNotNull(user, botStation.getId(), botName);
        List<Element> taskNodeList = element.elements(BOT_CONFIGURATION_ELEMENT_NAME);
        for (Element taskElement : taskNodeList) {
            String name = taskElement.attributeValue(NAME_ATTRIBUTE_NAME);
            String handler = taskElement.attributeValue(HANDLER_ATTRIBUTE_NAME, "");
            BotTask task = new BotTask();
            task.setBot(bot);
            task.setName(name);
            task.setTaskHandlerClassName(handler);
            String config = taskElement.attributeValue(CONFIGURATION_STRING_ATTRIBUTE_NAME);
            byte[] configuration;
            if (Strings.isNullOrEmpty(config)) {
                String configContent = taskElement.attributeValue(CONFIGURATION_CONTENT_ATTRIBUTE_NAME);
                if (configContent == null) {
                    configContent = taskElement.getTextTrim();
                }
                if (configContent != null) {
                    configuration = configContent.trim().getBytes(Charsets.UTF_8);
                } else {
                    configuration = new byte[0];
                }
            } else {
                configuration = getBotTaskConfiguration(config);
            }
            log.info("adding bot configuration element: " + name + " with conf: " + config);
            task.setConfiguration(configuration);
            botLogic.createBotTask(user, task);
        }
    }

    protected byte[] getBotTaskConfiguration(String config) {
        try {
            return ByteStreams.toByteArray(ClassLoaderUtil.getAsStreamNotNull(config, getClass()));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void addConfigurationsToBot(Element element) {
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        Preconditions.checkNotNull(botStationName, BOTSTATION_ATTRIBUTE_NAME);
        BotStation bs = botLogic.getBotStationNotNull(botStationName);
        addConfigurationsToBotCommon(element, bs);
    }

    public void removeConfigurationsFromBot(Element element) {
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        Preconditions.checkNotNull(botStationName, BOTSTATION_ATTRIBUTE_NAME);
        BotStation bs = botLogic.getBotStationNotNull(botStationName);
        removeConfigurationsFromBotCommon(element, bs);
    }

    public void removeConfigurationsFromBotCommon(Element element, BotStation botStation) {
        String botName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        Bot bot = botLogic.getBotNotNull(user, botStation.getId(), botName);
        List<Element> taskNodeList = element.elements(BOT_CONFIGURATION_ELEMENT_NAME);
        for (Element taskElement : taskNodeList) {
            String name = taskElement.attributeValue(NAME_ATTRIBUTE_NAME);
            BotTask botTask = botLogic.getBotTask(user, bot.getId(), name);
            if (botTask != null) {
                botLogic.removeBotTask(user, botTask.getId());
            }
        }
    }

    public void removeAllConfigurationsFromBot(Element element) {
        String botName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String botStationName = element.attributeValue(BOTSTATION_ATTRIBUTE_NAME);
        Bot bot = botLogic.getBotNotNull(user, botLogic.getBotStationNotNull(botStationName).getId(), botName);
        for (BotTask task : botLogic.getBotTasks(user, bot.getId())) {
            botLogic.removeBotTask(user, task.getId());
        }
    }

    private ProcessFilter createProcessFilter(Element element) {
        ProcessFilter filter = new ProcessFilter();
        String finishedOnlyString = element.attributeValue(ONLY_FINISHED_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(finishedOnlyString)) {
            filter.setFinishedOnly(Boolean.parseBoolean(finishedOnlyString));
        }
        String definitionName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(definitionName)) {
            filter.setDefinitionName(definitionName);
        }
        String definitionVersionString = element.attributeValue(VERSION_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(definitionVersionString)) {
            filter.setDefinitionVersion(Long.parseLong(definitionVersionString));
        }
        String idString = element.attributeValue(ID_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(idString)) {
            filter.setId(Long.parseLong(idString));
        } else {
            String idFromString = element.attributeValue(ID_FROM_ATTRIBUTE_NAME);
            if (!Strings.isNullOrEmpty(idFromString)) {
                filter.setIdFrom(Long.parseLong(idFromString));
            }
            String idToString = element.attributeValue(ID_TO_ATTRIBUTE_NAME);
            if (!Strings.isNullOrEmpty(idToString)) {
                filter.setIdTo(Long.parseLong(idToString));
            }
        }
        String startDateFromString = element.attributeValue(START_DATE_FROM_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(startDateFromString)) {
            filter.setStartDateFrom(CalendarUtil.convertToDate(startDateFromString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
        }
        String startDateToString = element.attributeValue(START_DATE_TO_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(startDateToString)) {
            filter.setStartDateTo(CalendarUtil.convertToDate(startDateToString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
        }
        String endDateFromString = element.attributeValue(END_DATE_FROM_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(endDateFromString)) {
            filter.setStartDateFrom(CalendarUtil.convertToDate(endDateFromString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
        }
        String endDateToString = element.attributeValue(END_DATE_TO_ATTRIBUTE_NAME);
        if (!Strings.isNullOrEmpty(endDateToString)) {
            filter.setEndDateTo(CalendarUtil.convertToDate(endDateToString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
        }
        return filter;
    }

    public void cancelProcesses(Element element) {
        ProcessFilter filter = createProcessFilter(element);
        executionLogic.cancelProcesses(user, filter);
    }

    public void removeProcesses(Element element) {
        ProcessFilter filter = createProcessFilter(element);
        executionLogic.deleteProcesses(user, filter);
    }

    public void removeProcessDefinitions(Element element) {
        // TODO
        throw new UnsupportedOperationException("Unimplemented yet");
        // String definitionName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        // String versionString =
        // element.attributeValue(VERSION_ATTRIBUTE_NAME);
        // if (!Strings.isNullOrEmpty(versionString)) {
        // throw new UnsupportedOperationException("Unimplemented yet");
        // } else {
        // String versionFromString =
        // element.attributeValue(VERSION_FROM_ATTRIBUTE_NAME);
        // String versionToString =
        // element.attributeValue(VERSION_TO_ATTRIBUTE_NAME);
        // }
        // definitionLogic.undeployProcessDefinition(user, definitionName)
    }

    public void relation(Element element) {
        String relationName = element.attributeValue(NAME_ATTRIBUTE_NAME);
        String relationDescription = element.attributeValue(DESCRIPTION_ATTRIBUTE_NAME);
        try {
            relationLogic.getRelation(user, relationName);
        } catch (RelationDoesNotExistException e) {
            relationLogic.createRelation(user, relationName, relationDescription);
        }
        Collection<Executor> leftExecutors = getExecutors(element.element("left"));
        Collection<Executor> rightExecutors = getExecutors(element.element("right"));
        if (leftExecutors.isEmpty() || rightExecutors.isEmpty()) {
            return;
        }
        for (Executor right : rightExecutors) {
            for (Executor left : leftExecutors) {
                relationLogic.addRelationPair(user, relationName, left, right);
            }
        }
    }

    public void stopProcess(Element element) {
        String id = element.attributeValue(ID_ATTRIBUTE_NAME);
        Long processId = Strings.isNullOrEmpty(id) ? null : Long.parseLong(id);
        executionLogic.cancelProcess(user, processId);
    }
}
