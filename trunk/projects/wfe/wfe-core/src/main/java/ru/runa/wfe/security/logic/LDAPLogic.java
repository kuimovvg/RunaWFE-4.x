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
package ru.runa.wfe.security.logic;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Imports users and group from LDAP directory.
 * 
 * @since 4.0.4
 */
@SuppressWarnings("unchecked")
public class LDAPLogic {
    private static final Log log = LogFactory.getLog(LDAPLogic.class);

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
    private static final String PHONE = "telephoneNumber";

    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;

    private List<String> ous = SystemProperties.getResources().getMultipleStringProperty("ldap.synchronizer.ou");

    private DirContext getContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                SystemProperties.getResources().getStringProperty("ldap.context.factory", "com.sun.jndi.ldap.LdapCtxFactory"));
        env.put(Context.PROVIDER_URL, SystemProperties.getResources().getStringPropertyNotNull("ldap.connection.provider.url"));
        env.put(Context.SECURITY_AUTHENTICATION, SystemProperties.getResources().getStringProperty("ldap.connection.authentication", "simple"));
        env.put(Context.SECURITY_PRINCIPAL, SystemProperties.getResources().getStringPropertyNotNull("ldap.connection.principal"));
        env.put(Context.SECURITY_CREDENTIALS, SystemProperties.getResources().getStringPropertyNotNull("ldap.connection.password"));
        env.put(Context.REFERRAL, SystemProperties.getResources().getStringProperty("ldap.connection.referral", "follow"));
        env.put("java.naming.ldap.version", SystemProperties.getResources().getStringProperty("ldap.connection.version", "3"));
        return new InitialDirContext(env);
    }

    public void synchronizeExecutors() {
        if (ous == null) {
            throw new NullPointerException("LDAP property is not configured 'ldap.synchronizer.ou'");
        }
        try {
            Group ldapGroup = new Group(IMPORTED_FROM_LDAP_GROUP_NAME, IMPORTED_FROM_LDAP_GROUP_DESCRIPION);
            if (!executorDAO.isExecutorExist(ldapGroup.getName())) {
                ldapGroup = executorDAO.create(ldapGroup);
                permissionDAO.setPermissions(ldapGroup, Lists.newArrayList(Permission.READ, SystemPermission.LOGIN_TO_SYSTEM), ASystem.INSTANCE);
            } else {
                ldapGroup = executorDAO.getGroup(ldapGroup.getName());
            }
            DirContext dirContext = getContext();
            Map<String, Actor> actors = syncronizeActors(dirContext, ldapGroup);
            synchronizeGroups(dirContext, ldapGroup, actors);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Map<String, Actor> syncronizeActors(DirContext dirContext, Group ldapUsersGroup) throws Exception {
        Map<String, Actor> actors = Maps.newHashMap();
        Set<String> addedNameSet = Sets.newHashSet();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        for (String ou : ous) {
            NamingEnumeration<SearchResult> list = dirContext.search(ou, attrs);
            while (list.hasMore()) {
                SearchResult searchResult = list.next();
                String name = getStringAttribute(searchResult, SAM_ACCOUNT_NAME);
                if (!addedNameSet.add(name)) {
                    log.debug("Ignoring duplicated user " + name);
                    continue;
                }
                String fullName = getStringAttribute(searchResult, DISPLAY_NAME);
                String email = getStringAttribute(searchResult, EMAIL);
                String description = getStringAttribute(searchResult, TITLE);
                String phone = getStringAttribute(searchResult, PHONE);
                Actor actor = new Actor(name, description, fullName, null, email, phone);
                actor = createExecutorIfNotExists(ldapUsersGroup, actor);
                actors.put(searchResult.getNameInNamespace(), actor);
            }
        }
        return actors;
    }

    private String getStringAttribute(SearchResult searchResult, String name) throws NamingException {
        Attribute attribute = searchResult.getAttributes().get(name);
        if (attribute != null) {
            return attribute.get().toString();
        }
        return null;
    }

    private void synchronizeGroups(DirContext dirContext, Group ldapUsersGroup, Map<String, Actor> allActors) throws NamingException {
        Attributes attributes = new BasicAttributes();
        Set<String> synchronizedGroupNames = Sets.newHashSet();
        attributes.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_GROUP_VALUE);
        for (String ou : ous) {
            NamingEnumeration<SearchResult> list = dirContext.search(ou, attributes);
            while (list.hasMore()) {
                SearchResult searchResult = list.next();
                String name = getStringAttribute(searchResult, SAM_ACCOUNT_NAME);
                if (!synchronizedGroupNames.add(name)) {
                    continue;
                }
                String description = getStringAttribute(searchResult, DISPLAY_NAME);
                Group group = new Group(name, description);
                group = createExecutorIfNotExists(ldapUsersGroup, group);

                Attribute memberAttribute = searchResult.getAttributes().get(MEMBER);
                if (memberAttribute == null) {
                    continue;
                }
                List<Actor> groupActors = Lists.newArrayList();
                NamingEnumeration<String> namingEnum = (NamingEnumeration<String>) memberAttribute.getAll();
                while (namingEnum.hasMore()) {
                    String actorDN = namingEnum.next();
                    Actor actor = allActors.get(actorDN);
                    if (actor == null) {
                        log.warn("No actor found for " + actorDN);
                        continue;
                    }
                    if (!executorDAO.isExecutorInGroup(actor, group)) {
                        groupActors.add(actor);
                    }
                }
                if (groupActors.size() > 0) {
                    executorDAO.addExecutorsToGroup(groupActors, group);
                }
            }
        }
    }

    private <T extends Executor> T createExecutorIfNotExists(Group ldapGroup, T executor) {
        if (!executorDAO.isExecutorExist(executor.getName())) {
            log.info("Importing " + executor.getName());
            executorDAO.create(executor);
            executorDAO.addExecutorsToGroup(Lists.newArrayList(executor), ldapGroup);
            permissionDAO.setPermissions(ldapGroup, Lists.newArrayList(Permission.READ), executor);
            return executor;
        }
        return (T) executorDAO.getExecutor(executor.getName());
    }

}
