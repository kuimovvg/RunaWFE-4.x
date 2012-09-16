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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.logic.AuthenticationLogic.AuthType;
import ru.runa.af.logic.AuthenticationLogic.LoginHandler;

import com.google.common.collect.Lists;

/**
 * Created on 13.09.2004
 * 
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class LDAPImporter implements LoginHandler {

    private static final Log log = LogFactory.getLog(LDAPImporter.class);

    private static final String OBJECT_CLASS_ATTR_NAME = "objectClass";

    private static final String OBJECT_CLASS_ATTR_USER_VALUE = "user";

    private static final String OBJECT_CLASS_ATTR_GROUP_VALUE = "group";

    private static final String IMPORTED_FROM_LDAP_GROUP_NAME = "ldap users";

    private static final String IMPORTED_FROM_LDAP_GROUP_DESCRIPION = "users imported from ldap";

    private static final String DISPLAY_NAME = "name";

    private static final String SAM_ACCOUNT_NAME = "sAMAccountName";

    private static final String TITLE = "title";

    private static final String EMAIL = "mail";

    private static final String MEMBER = "member";

    @Autowired
    private ExecutorLogic executorLogic;

    private DirContext dirContext;

    private Subject subject;

    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Autowired
    protected ExecutorDAO executorDAO;

    public void importExecutors(String username, String password) {
        authorizationLogic = new AuthorizationLogic();
        try {
            subject = new AuthenticationLogic().authenticate(username, password);
            dirContext = getContext();
            executorLogic = new ExecutorLogic();
            importExecutors(getActorList());
            importExecutors(getGroupList());
            addExecutorsToGroups();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InternalApplicationException(e.getMessage());
        }
    }

    private Set<String> getGroups(String executor, Set<String> visited, Pattern pattern) throws NamingException {
        if (visited == null) {
            visited = new HashSet<String>();
        }
        Attribute memberOf = dirContext.getAttributes(executor).get("memberof");
        if (memberOf == null) {
            return visited;
        }
        NamingEnumeration<String> namingEnum = (NamingEnumeration<String>) memberOf.getAll();
        while (namingEnum.hasMore()) {
            String executorNC = namingEnum.next();
            Matcher m = pattern.matcher(executorNC);
            String executorPath = m.replaceAll("");
            String groupName = dirContext.getAttributes(executorPath).get(SAM_ACCOUNT_NAME).get().toString();
            if (visited.contains(groupName)) {
                continue;
            }
            visited.add(groupName);
            getGroups(executorPath, visited, pattern);
        }
        return visited;
    }

    private void addUserToGroup(Actor actor, String groupName) {
        try {
            Group group = executorDAO.getGroup(groupName);
            if (!executorDAO.isExecutorInGroup(actor, group)) {
                executorDAO.addExecutorsToGroup(Lists.newArrayList((Executor) actor), group);
            }
        } catch (ExecutorAlreadyInGroupException e) {
        } catch (ExecutorOutOfDateException e) {
        }
    }

    private void removeUserFromGroup(Actor actor, String groupName) {
        try {
            Group group = executorDAO.getGroup(groupName);
            if (executorDAO.isExecutorInGroup(actor, group)) {
                executorDAO.removeExecutorsFromGroup(Lists.newArrayList((Executor) actor), group);
            }
        } catch (ExecutorNotInGroupException e) {
        } catch (ExecutorOutOfDateException e) {
        }
    }

    public void onUserLogin(Actor actor, AuthType type) {
        if (type != AuthType.kerberos_auth && type != AuthType.ntlm_auth) {
            return;
        }
        String dc = LDAPImporterResources.getDC();
        Pattern pattern = Pattern.compile("," + dc, Pattern.CASE_INSENSITIVE);
        HashMap<String, String> groupsReflection = new HashMap<String, String>();
        try {
            dirContext = getContext();
            for (Group group : executorDAO.getAllGroups()) {
                if (group.getActiveDirectoryGroup() == null || "".equals(group.getActiveDirectoryGroup())) {
                    continue;
                }
                groupsReflection.put(group.getName(), group.getActiveDirectoryGroup());
            }
            if (groupsReflection.isEmpty()) {
                return;
            }
            Set<String> LDAPGroups = null;
            try {
                SearchControls sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> s = dirContext.search("", SAM_ACCOUNT_NAME + "=" + actor.getName(), sc);
                if (s.hasMore()) {
                    LDAPGroups = getGroups(pattern.matcher(s.next().getName()).replaceAll(""), null, pattern);
                }
            } catch (Exception e) {
            }
            if (LDAPGroups == null) {
                log.warn("Active directory group synchronization error. Couldn't get groups for actor " + actor.getName());
                return;
            }
            for (String wfGroupName : groupsReflection.keySet()) {
                if (LDAPGroups.contains(groupsReflection.get(wfGroupName))) {
                    addUserToGroup(actor, wfGroupName);
                } else {
                    removeUserFromGroup(actor, wfGroupName);
                }
            }
        } catch (Exception e) {
            log.warn("Couldn't synchronize wf groups with LDAP.", e);
        }
    }

    private void importExecutors(List<? extends Executor> executorList) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, ExecutorAlreadyExistsException, UnapplicablePermissionException, ExecutorAlreadyInGroupException {
        List<Executor> existExecutorList = new ArrayList<Executor>();
        List<Executor> executorToImportList = new ArrayList<Executor>();
        for (Executor executor : executorList) {
            if (executorLogic.isExecutorExist(subject, executor.getName())) {
                executor = executorLogic.getExecutor(subject, executor.getName());
                log.info(executor.getName() + " already exists. Skipping.");
                existExecutorList.add(executor);
            } else {
                log.info("Importing " + executor.getName());
                executorToImportList.add(executor);
            }
        }
        executorLogic.create(subject, executorToImportList);
        Group ldapUsersGroup = getLDAPUsersGroup();
        executorLogic.addExecutorsToGroup(subject, executorToImportList, ldapUsersGroup);
        authorizationLogic.setPermissions(subject, ldapUsersGroup, Lists.newArrayList(Permission.READ), executorToImportList);
    }

    private String getUserAttribute(String userId, String attributeName) throws NamingException {
        Attribute attribute = dirContext.getAttributes(userId).get(attributeName);
        if (attribute == null) {
            return null;
        }
        return attribute.get().toString();
    }

    private List<? extends Executor> getActorList() throws NamingException {
        String[] ouNames = LDAPImporterResources.getOU();
        List<Actor> actors = new ArrayList<Actor>();
        Set<String> addedNameSet = new HashSet<String>();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration list = dirContext.search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = (NameClassPair) list.next();
                String name = getUserAttribute(nc.getName() + "," + ou, SAM_ACCOUNT_NAME);
                if (!addedNameSet.add(name)) {
                    log.warn("Duplicated user name " + name);
                    continue;// we have imported user with such name already.
                }
                String fullName = getUserAttribute(nc.getName() + "," + ou, DISPLAY_NAME);
                String email = getUserAttribute(nc.getName() + "," + ou, EMAIL);
                String description = getUserAttribute(nc.getName() + "," + ou, TITLE);
                Actor actor = new Actor(name, description, fullName, null, email, null);
                actors.add(actor);
            }
        }
        return actors;
    }

    private List<? extends Executor> getGroupList() throws NamingException {
        String[] ouNames = LDAPImporterResources.getOU();
        List<Group> groups = new ArrayList<Group>();
        Attributes attrs = new BasicAttributes();
        Set<String> addedNameSet = new HashSet<String>();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_GROUP_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration list = dirContext.search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = (NameClassPair) list.next();
                String name = dirContext.getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get().toString();
                if (!addedNameSet.add(name)) {
                    log.warn("Duplicated group name " + name);
                    continue;// we have imported user with such name already.
                }
                String description = dirContext.getAttributes(nc.getName() + "," + ou).get(DISPLAY_NAME).get().toString();
                Group group = new Group(name, description);
                groups.add(group);
            }
        }
        return groups;
    }

    private void addExecutorsToGroups() {
        String[] ouNames = LDAPImporterResources.getOU();
        String dc = LDAPImporterResources.getDC();
        Pattern pattern = Pattern.compile("," + dc, Pattern.CASE_INSENSITIVE);
        for (String ou : ouNames) {
            try {
                Attributes attrs = new BasicAttributes();
                attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_GROUP_VALUE);
                NamingEnumeration list = dirContext.search(ou, attrs);
                while (list.hasMore()) {
                    NameClassPair nc = (NameClassPair) list.next();
                    String groupName = dirContext.getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get().toString();
                    try {
                        Group group = executorLogic.getGroup(subject, groupName);
                        List<Executor> executorsList = new ArrayList<Executor>();
                        Attribute groupAttr = dirContext.getAttributes(nc.getName() + "," + ou).get(MEMBER);
                        if (groupAttr == null) {
                            continue;
                        }
                        NamingEnumeration namingEnum = groupAttr.getAll();
                        while (namingEnum.hasMore()) {
                            try {
                                String executorNC = (String) namingEnum.next();
                                Matcher m = pattern.matcher(executorNC);
                                String executorPath = m.replaceAll("");
                                Attribute samAttr = dirContext.getAttributes(executorPath).get(SAM_ACCOUNT_NAME);
                                if (samAttr != null) {
                                    String executorName = samAttr.get().toString();
                                    if (executorLogic.isExecutorExist(subject, executorName)) {
                                        Executor executorToAdd = executorLogic.getExecutor(subject, executorName);
                                        if (!executorLogic.isExecutorInGroup(subject, executorToAdd, group)) {
                                            executorsList.add(executorToAdd);
                                        }
                                    } else {
                                        log.warn("Executor=" + executorName + " does not exist,check wheter the  principal has read permissions on "
                                                + executorNC);
                                    }
                                }
                            } catch (AuthorizationException e) {
                                log.warn("Failed to get executor", e);
                            } catch (ExecutorOutOfDateException e) {
                                log.warn("Failed to get executor", e);
                            } catch (AuthenticationException e) {
                                log.warn("Failed to get executor", e);
                            }
                        }
                        if (executorsList.size() > 0) {// small performance optimization
                            executorLogic.addExecutorsToGroup(subject, executorsList, group);
                        }
                    } catch (AuthorizationException e) {
                        log.warn("Failed to perform operation " + e.getMessage(), e);
                    } catch (ExecutorOutOfDateException e) {
                        log.warn("This is probably application error." + e.getMessage(), e);
                    } catch (NamingException e) {
                        log.warn(e.getMessage(), e);
                    } catch (AuthenticationException e) {
                        log.warn(e.getMessage(), e);
                    } catch (ExecutorAlreadyInGroupException e) {
                        log.warn("Failed to add executor=" + e.getMessage() + " to group" + e.getGroupName(), e);
                    }
                }
            } catch (NamingException e) {
                throw new InternalApplicationException(e.getMessage());
            }
        }
    }

    private Group getLDAPUsersGroup() throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, UnapplicablePermissionException {
        Group ldapUsersGroup = new Group(IMPORTED_FROM_LDAP_GROUP_NAME, IMPORTED_FROM_LDAP_GROUP_DESCRIPION);
        if (!executorLogic.isExecutorExist(subject, ldapUsersGroup.getName())) {
            ldapUsersGroup = executorLogic.create(subject, ldapUsersGroup);
            authorizationLogic.setPermissions(subject, ldapUsersGroup, Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM), ASystem.SYSTEM);
        } else {
            ldapUsersGroup = executorLogic.getGroup(subject, ldapUsersGroup.getName());
        }
        return ldapUsersGroup;
    }

    private DirContext getContext() throws NamingException {
        String serverURL = LDAPImporterResources.getServerURL();
        String dc = LDAPImporterResources.getDC();
        String principal = LDAPImporterResources.getSECURITY_PRINCIPAL();
        String password = LDAPImporterResources.getLdapPassword();
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, serverURL + "/" + dc);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.REFERRAL, "follow");
        env.put("java.naming.ldap.version", "3");
        return new InitialDirContext(env);
    }
}
