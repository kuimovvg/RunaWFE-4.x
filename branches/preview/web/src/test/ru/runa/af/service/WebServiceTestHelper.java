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
package ru.runa.af.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

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
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.Profile;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 04.08.2004
 */
public class WebServiceTestHelper {

    private final static String ADMINISTRATOR_NAME = "Administrator";

    private final static String ADMINISTRATOR_PASSWORD = "wf";

    private final static String ADMINISTRATORS_NAME = "Administrators";

    private final Set<Executor> createdExecutorsSet = new HashSet<Executor>();

    private final Map<String, Executor> defaultExecutorsMap = new HashMap<String, Executor>();

    public final static String AUTHORIZED_PERFORMER_NAME = "AUTHORIZED_PERFORMER_NAME";

    public final static String AUTHORIZED_PERFORMER_DESCRIPTION = "AUTHORIZED_PERFORMER_DESCRIPTION";

    public final static String AUTHORIZED_PERFORMER_PASSWORD = "AUTHORIZED_PERFORMER_PASSWORD";

    public final static String UNAUTHORIZED_PERFORMER_NAME = "UNAUTHORIZED_PERFORMER_NAME";

    public final static String UNAUTHORIZED_PERFORMER_DESCRIPTION = "UNAUTHORIZED_PERFORMER_DESCRIPTION";

    public final static String UNAUTHORIZED_PERFORMER_PASSWORD = "UNAUTHORIZED_PERFORMER_PASSWORD";

    public final static String BASE_GROUP_NAME = "BASE_GROUP_NAME";

    public final static String BASE_GROUP_DESC = "BASE_GROUP_DESC";

    public final static String BASE_GROUP_ACTOR_NAME = "BASE_GROUP_ACTOR_NAME";

    public final static String BASE_GROUP_ACTOR_DESC = "BASE_GROUP_ACTOR_DESC";

    public final static String SUB_GROUP_NAME = "SUB_GROUP_NAME";

    public final static String SUB_GROUP_DESC = "SUB_GROUP_DESC";

    public final static String SUB_GROUP_ACTOR_NAME = "SUB_GROUP_ACTOR_NAME";

    public final static String SUB_GROUP_ACTOR_DESC = "SUB_GROUP_ACTOR_DESC";

    public final static String FAKE_ACTOR_NAME = "FAKE_ACTOR_NAME";

    public final static String FAKE_ACTOR_DESC = "FAKE_ACTOR_DESC";

    public final static String FAKE_GROUP_NAME = "FAKE_GROUP_NAME";

    public final static String FAKE_GROUP_DESC = "FAKE_GROUP_DESC";

    private ExecutorService executorService;

    private RelationService relationService;

    protected AuthorizationService authorizationService;

    private AuthenticationService authenticationService;

    private SystemService systemService;

    private ProfileService profileService;

    private Actor fakeActor;

    private List<Executor> fakeExecutors;

    private Actor baseGroupActor;

    private Actor subGroupActor;

    private Group fakeGroup;

    private Group baseGroup;

    private Group subGroup;

    private Subject fakeSubject;

    private Subject authorizedPerformerSubject, unauthorizedPerformerSubject;

    private final String testClassName;

    protected Subject adminSubject;

    private Set<Subject> subjectOfActorsWithProfileSet = new HashSet<Subject>();

    public WebServiceTestHelper(String testClassName) throws ExecutorOutOfDateException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, InternalApplicationException, WeakPasswordException {
        this.testClassName = testClassName;
        createExecutorServiceDelegate();
        createRelationServiceDelegate();
        createAuthorizationServiceDelegate();
        createAuthenticationServiceDelegate();
        createSystemServiceDelegate();
        createAdminSubject();
        createPerformersAndPerformesSubjects();
        createFakeExecutors();
        createFakeSubject();
        createProfileServiceDelegate();
    }

    private void createProfileServiceDelegate() {
        profileService = DelegateFactory.getInstance().getProfileService();
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public Subject getAdminSubject() {
        return adminSubject;
    }

    public ASystem getAASystem() {
        return ASystem.SYSTEM;
    }

    /**
     * Creates groups and actors group contains subGroup and subActor subGroup
     * contains subGroupActor
     */
    public void createDefaultExecutorsMap() throws ExecutorOutOfDateException, ExecutorAlreadyInGroupException, ExecutorAlreadyExistsException,
            AuthorizationException, AuthenticationException, InternalApplicationException {
        baseGroup = executorService.create(adminSubject, new Group(testClassName + BASE_GROUP_NAME, testClassName + BASE_GROUP_DESC));
        defaultExecutorsMap.put(BASE_GROUP_NAME, baseGroup);

        baseGroupActor = executorService.create(adminSubject, new Actor(testClassName + BASE_GROUP_ACTOR_NAME, testClassName
                + BASE_GROUP_ACTOR_NAME));
        defaultExecutorsMap.put(BASE_GROUP_ACTOR_NAME, baseGroupActor);

        subGroup = executorService.create(adminSubject, new Group(testClassName + SUB_GROUP_NAME, testClassName + SUB_GROUP_DESC));
        defaultExecutorsMap.put(SUB_GROUP_NAME, subGroup);

        subGroupActor = executorService.create(adminSubject, new Actor(testClassName + SUB_GROUP_ACTOR_NAME, testClassName
                + SUB_GROUP_ACTOR_DESC));
        defaultExecutorsMap.put(SUB_GROUP_ACTOR_NAME, subGroupActor);

        Executor[] baseGroupExecutors = { baseGroupActor, subGroup };
        executorService.addExecutorsToGroup(adminSubject, Lists.newArrayList(baseGroupExecutors), baseGroup);
        Executor[] subGroupExecutors = { subGroupActor };
        subGroup = executorService.getGroup(adminSubject, subGroup.getId());
        executorService.addExecutorsToGroup(adminSubject, Lists.newArrayList(subGroupExecutors), subGroup);
    }

    public void releaseResources() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        removeCreatedProfiles();
        removeCreatedExecutors();
        removeDefaultExecutors();
        executorService = null;
        executorService = null;
        authorizationService = null;
        authorizationService = null;
        authenticationService = null;
        authenticationService = null;
        systemService = null;
        systemService = null;

        fakeActor = null;
        fakeGroup = null;

            fakeExecutors.clear();
        fakeExecutors = null;

        baseGroup = null;
        baseGroupActor = null;
        subGroup = null;
        subGroupActor = null;
        fakeSubject = null;
    }
    
    public List<Long> toIds(Collection<? extends Identifiable> list) {
        List<Long> ids = Lists.newArrayList();
        for (Identifiable identifiable : list) {
            ids.add(identifiable.getId());
        }
        return ids;
    }

    private void removeCreatedProfiles() throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        for (Subject subject : subjectOfActorsWithProfileSet) {
            profileService.deleteProfile(adminSubject, authenticationService.getActor(subject).getId());
        }
        subjectOfActorsWithProfileSet.clear();
        subjectOfActorsWithProfileSet = null;
        profileService = null;
    }

    /**
     * This method removes executor from createdExecutors set bun not from db,
     * call this method when you want to remove executor manually.
     */
    public void removeCreatedExecutor(Executor executor) {
        createdExecutorsSet.remove(executor);
    }

    public void removeExecutorIfExists(Executor executor) throws InternalApplicationException, AuthorizationException, AuthenticationException {
        if (executor != null) {
            try {
                List<Long> executorIds = Lists.newArrayList();
                if (executor instanceof Actor) {
                    executorIds.add(executorService.getActor(adminSubject, executor.getName()).getId());
                }
                if (executor instanceof Group) {
                    executorIds.add(executorService.getGroup(adminSubject, executor.getName()).getId());
                }
                executorService.remove(adminSubject, executorIds);
            } catch (ExecutorOutOfDateException e) {
            }
        }
    }

    public void addExecutorToGroup(Executor executor, Group group) throws ExecutorAlreadyInGroupException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException, InternalApplicationException {
        Executor[] executors = { executor };
        executorService.addExecutorsToGroup(adminSubject, Lists.newArrayList(executors), group);
    }

    public void removeExecutorFromGroup(Executor executor, Group group) throws ExecutorNotInGroupException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException, InternalApplicationException {
        Executor[] executors = { executor };
        executorService.removeExecutorsFromGroup(adminSubject, Lists.newArrayList(executors), group);
    }

    public boolean isExecutorExist(Executor executor) throws AuthorizationException, AuthenticationException, InternalApplicationException {
        boolean isExecutorExist = true;
        try {
            if (executor instanceof Actor) {
                executorService.getActor(adminSubject, executor.getName());
            }
            if (executor instanceof Group) {
                executorService.getGroup(adminSubject, executor.getName());
            }
        } catch (ExecutorOutOfDateException e) {
            isExecutorExist = false;
        }
        return isExecutorExist;
    }

    public boolean isPasswordCorrect(String name, String password) {
        boolean isPasswordCorrect = false;
        try {
            authenticationService.authenticate(name, password);
            isPasswordCorrect = true;
        } catch (InternalApplicationException e) {
        } catch (AuthenticationException e) {
        }
        return isPasswordCorrect;
    }

    public void setPermissionsToAuthorizedPerformer(Collection<Permission> permissions, Executor executor) throws UnapplicablePermissionException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, executor);
    }

    public void setPermissionsToAuthorizedPerformerOnExecutors(Collection<Permission> permissions, List<? extends Executor> executors)
            throws UnapplicablePermissionException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        for (Executor executor : executors) {
            authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, executor);
        }
    }

    public void setPermissionsToAuthorizedPerformerOnSystem(Collection<Permission> permissions) throws UnapplicablePermissionException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        authorizationService.setPermissions(adminSubject, getAuthorizedPerformerActor(), permissions, ASystem.SYSTEM);
    }

    public Actor createActor(String name, String description) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        Actor actor = executorService.create(adminSubject, new Actor(name, description));
        createdExecutorsSet.add(actor);
        return actor;
    }

    public List<Actor> createActorArray(String name, String description) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        Actor[] actorArray = new Actor[5];
        actorArray[0] = executorService.create(adminSubject, new Actor(name + "0", description + "0"));
        actorArray[1] = executorService.create(adminSubject, new Actor(name + "1", description + "1"));
        actorArray[2] = executorService.create(adminSubject, new Actor(name + "2", description + "2"));
        actorArray[3] = executorService.create(adminSubject, new Actor(name + "3", description + "3"));
        actorArray[4] = executorService.create(adminSubject, new Actor(name + "4", description + "4"));
        createdExecutorsSet.add(actorArray[0]);
        createdExecutorsSet.add(actorArray[1]);
        createdExecutorsSet.add(actorArray[2]);
        createdExecutorsSet.add(actorArray[3]);
        createdExecutorsSet.add(actorArray[4]);
        return Lists.newArrayList(actorArray);
    }

    public Group createGroup(String name, String description) throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        Group group = executorService.create(adminSubject, new Group(name, description));
        createdExecutorsSet.add(group);
        return group;
    }

    public List<Group> createGroupArray(String name, String description) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        Group[] groupArray = new Group[5];
        groupArray[0] = executorService.create(adminSubject, new Group(name + "0", description + "0"));
        groupArray[1] = executorService.create(adminSubject, new Group(name + "1", description + "1"));
        groupArray[2] = executorService.create(adminSubject, new Group(name + "2", description + "2"));
        groupArray[3] = executorService.create(adminSubject, new Group(name + "3", description + "3"));
        groupArray[4] = executorService.create(adminSubject, new Group(name + "4", description + "4"));
        createdExecutorsSet.add(groupArray[0]);
        createdExecutorsSet.add(groupArray[1]);
        createdExecutorsSet.add(groupArray[2]);
        createdExecutorsSet.add(groupArray[3]);
        createdExecutorsSet.add(groupArray[4]);
        return Lists.newArrayList(groupArray);
    }

    public List<Executor> createMixedActorsGroupsArray(String name, String description) throws ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        Executor[] mixedArray = new Executor[5];
        mixedArray[0] = executorService.create(adminSubject, new Actor(name + "0", description + "0"));
        mixedArray[1] = executorService.create(adminSubject, new Group(name + "1", description + "1"));
        mixedArray[2] = executorService.create(adminSubject, new Actor(name + "2", description + "2"));
        mixedArray[3] = executorService.create(adminSubject, new Group(name + "3", description + "3"));
        mixedArray[4] = executorService.create(adminSubject, new Actor(name + "4", description + "4"));
        createdExecutorsSet.add(mixedArray[0]);
        createdExecutorsSet.add(mixedArray[1]);
        createdExecutorsSet.add(mixedArray[2]);
        createdExecutorsSet.add(mixedArray[3]);
        createdExecutorsSet.add(mixedArray[4]);
        return Lists.newArrayList(mixedArray);
    }

    public Subject getAuthorizedPerformerSubject() {
        return authorizedPerformerSubject;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Subject getUnauthorizedPerformerSubject() {
        return unauthorizedPerformerSubject;
    }

    public Map<String, Executor> getDefaultExecutorsMap() {
        return defaultExecutorsMap;
    }

    public Actor getFakeActor() {
        return fakeActor;
    }

    public Group getFakeGroup() {
        return fakeGroup;
    }

    public List<Executor> getFakeExecutors() {
        return fakeExecutors;
    }

    public boolean isExecutorInGroup(Executor executor, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        return executorService.isExecutorInGroup(adminSubject, executor, group);
    }

    public boolean isExecutorInGroups(Executor executor, List<Group> groups) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        for (Group group : groups) {
            if (executorService.isExecutorInGroup(adminSubject, executor, group) == false) {
                return false;
            }
        }
        return true;
    }

    public boolean isExecutorsInGroup(List<? extends Executor> executors, Group group) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        for (Executor executor : executors) {
            if (executorService.isExecutorInGroup(adminSubject, executor, group) == false) {
                return false;
            }
        }
        return true;
    }

    private void createExecutorServiceDelegate() {
        executorService = DelegateFactory.getInstance().getExecutorService();
    }

    private void createRelationServiceDelegate() {
        relationService = DelegateFactory.getInstance().getRelationService();
    }

    private void createAuthorizationServiceDelegate() {
        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
    }

    private void createAuthenticationServiceDelegate() {
        authenticationService = DelegateFactory.getInstance().getAuthenticationService();
    }

    private void createSystemServiceDelegate() {
        systemService = DelegateFactory.getInstance().getSystemService();
    }

    private void createPerformersAndPerformesSubjects() throws ExecutorOutOfDateException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, InternalApplicationException, WeakPasswordException {
        Actor authorizedPerformerActor = executorService.create(adminSubject, new Actor(testClassName + AUTHORIZED_PERFORMER_NAME,
                AUTHORIZED_PERFORMER_DESCRIPTION));
        executorService.setPassword(adminSubject, authorizedPerformerActor, AUTHORIZED_PERFORMER_PASSWORD);
        Actor unauthorizedPerformerActor = executorService.create(adminSubject, new Actor(testClassName + UNAUTHORIZED_PERFORMER_NAME,
                UNAUTHORIZED_PERFORMER_DESCRIPTION));
        executorService.setPassword(adminSubject, unauthorizedPerformerActor, UNAUTHORIZED_PERFORMER_PASSWORD);

        authorizedPerformerSubject = authenticationService.authenticate(authorizedPerformerActor.getName(), AUTHORIZED_PERFORMER_PASSWORD);
        unauthorizedPerformerSubject = authenticationService.authenticate(unauthorizedPerformerActor.getName(),
                UNAUTHORIZED_PERFORMER_PASSWORD);
    }

    private void createAdminSubject() throws InternalApplicationException, AuthenticationException {
        adminSubject = authenticationService.authenticate(ADMINISTRATOR_NAME, ADMINISTRATOR_PASSWORD);
    }

    /** Removes all created executors from DB. */
    private void removeCreatedExecutors() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        executorService.remove(adminSubject, toIds(createdExecutorsSet));
        List<Long> performerIds = Lists.newArrayList(getAuthorizedPerformerActor().getId(), getUnauthorizedPerformerActor().getId());
        executorService.remove(adminSubject, performerIds);
    }

    public Collection<Permission> getOwnPermissions(Executor performer, Executor executor) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        return authorizationService.getOwnPermissions(adminSubject, performer, executor);
    }

    public Collection<Permission> getOwnPermissionsAASystem(Executor performer) throws ExecutorOutOfDateException, AuthorizationException,
            AuthenticationException, InternalApplicationException {
        return authorizationService.getOwnPermissions(adminSubject, performer, ASystem.SYSTEM);
    }

    /** check if default executors still exists in db, and id so removes them */
    private void removeDefaultExecutors() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        List<Long> undeletedExecutorIds = Lists.newArrayList();
        for (Executor executor : defaultExecutorsMap.values()) {
            boolean canRemove = false;
            try {
                executor = executorService.getExecutor(adminSubject, executor.getId());
                canRemove = true;
            } catch (ExecutorOutOfDateException e) {
                // do nothing, this executor was deleted
            }
            if (canRemove) {
                undeletedExecutorIds.add(executor.getId());
            }
        }
        executorService.remove(adminSubject, undeletedExecutorIds);
    }

    private void createFakeExecutors() {
        fakeActor = new Actor(testClassName + FAKE_ACTOR_NAME, testClassName + FAKE_ACTOR_DESC);
        fakeGroup = new Group(testClassName + FAKE_GROUP_NAME, testClassName + FAKE_GROUP_DESC);
        fakeExecutors = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            fakeExecutors.add(new Actor(testClassName + FAKE_ACTOR_NAME + i, testClassName + FAKE_ACTOR_DESC + i));
        }
    }

    private void createFakeSubject() {
        fakeSubject = new Subject();
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public SystemService getSystemService() {
        return systemService;
    }

    public Subject getFakeSubject() {
        return fakeSubject;
    }

    public Actor getAuthorizedPerformerActor() throws AuthenticationException, InternalApplicationException {
        return authenticationService.getActor(authorizedPerformerSubject);
    }

    public Actor getUnauthorizedPerformerActor() throws AuthenticationException, InternalApplicationException {
        return authenticationService.getActor(unauthorizedPerformerSubject);
    }

    public Group getAdministratorsGroup() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        return executorService.getGroup(adminSubject, ADMINISTRATORS_NAME);
    }

    public Actor getAdministrator() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        return executorService.getActor(adminSubject, ADMINISTRATOR_NAME);
    }

    public String getAdministratorPassword() {
        return ADMINISTRATOR_PASSWORD;
    }

    public Group getBaseGroup() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        return executorService.getGroup(adminSubject, baseGroup.getId());
        // return baseGroup; we can't cache executor that changes it's state in
        // db
    }

    public Actor getBaseGroupActor() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        return executorService.getActor(adminSubject, baseGroupActor.getId());
        // return baseGroupActor;
    }

    public Group getSubGroup() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        return executorService.getGroup(adminSubject, subGroup.getId());
        // return subGroup;
    }

    public Actor getSubGroupActor() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        return executorService.getActor(adminSubject, subGroupActor.getId());
        // return subGroupActor;
    }

    public Executor getExecutor(String name) throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException,
            InternalApplicationException {
        return executorService.getExecutor(adminSubject, name);
    }

    public Profile getDefaultProfile(Subject subject) throws InternalApplicationException, AuthenticationException {
        Profile profile = profileService.getProfile(subject);
        subjectOfActorsWithProfileSet.add(subject);
        return profile;
    }

    public Identifiable getFakeIdentifiable() {
        return new Identifiable() {
            @Override
            public Long getId() {
                return 0L;
            }
            @Override
            public int identifiableType() {
                return 0;
            }
        };
    }

    public BatchPresentation getExecutorBatchPresentation() {
        return AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
    }

    public BatchPresentation getExecutorBatchPresentation(String presentationId) {
        return AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME,
                presentationId);
    }

    public Relation createRelation(String name, String description) throws RelationExistException, AuthorizationException, AuthenticationException {
        Relation relation = relationService.createRelation(adminSubject, name, description);
        return relation;
    }

    public RelationPair addRelationPair(String relationName, Executor left, Executor right) throws RelationDoesNotExistsException,
            AuthorizationException, AuthenticationException {
        RelationPair relationPair = relationService.addRelationPair(adminSubject, relationName, left, right);
        return relationPair;
    }

    public void removeRelation(Long relationId) throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        relationService.removeRelation(adminSubject, relationId);
    }

}
