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

package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/*
 */
public class ExecutorServiceDelegateAddManyExecutorsToGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateAddManyExecutorsToGroupsTest.class.getName();

    private Group additionalGroup;
    private List<Group> additionalGroups;

    private Actor additionalActor;
    private List<Actor> additionalActors;

    private List<Executor> additionalActorGroupsMixed;

    private final Collection<Permission> addToGroupPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> readlistPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateAddManyExecutorsToGroupsTest.class);
    }

    private List<Actor> getAdditionalActors() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        List<Long> ids = Lists.newArrayList();
        for (Actor actor : additionalActors) {
            ids.add(actor.getId());
        }
        List<Actor> executors = (List) executorService.getExecutors(th.getAdminSubject(), ids);
        return executors;
    }

    private List<Group> getAdditionalGroups() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        List<Long> ids = Lists.newArrayList();
        for (Group group : additionalGroups) {
            ids.add(group.getId());
        }
        return executorService.getGroups(th.getAdminSubject(), ids);
    }

    private List<Executor> getAdditionalGroupsMixed() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            ids.add(executor.getId());
        }
        return executorService.getExecutors(th.getAdminSubject(), ids);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getGroup(th.getAdminSubject(), additionalGroup.getId());
    }

    private Actor getAdditionalActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getActor(th.getAdminSubject(), additionalActor.getId());
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalGroups = th.createGroupArray("additionalG", "Additional Group");

        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        additionalActors = th.createActorArray("additionalA", "Additional Actor");

        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformer(readPermissions, additionalActor);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActors);

        th.setPermissionsToAuthorizedPerformer(readlistPermissions, additionalGroup);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readlistPermissions, additionalGroups);

        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActorGroupsMixed);

        super.setUp();
    }

    public void testAddActorsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalActors, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalActors, additionalGroup);
            assertTrue("Executors added to group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalActors, additionalGroup);

        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testAddGroupsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalGroups, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalGroups, additionalGroup);
            assertTrue("Executors added to group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalGroups, getAdditionalGroup());

        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalGroups(), getAdditionalGroup()));
    }

    public void testAddMixedActorsGroupsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalActorGroupsMixed, additionalGroup);
            assertTrue("Executors added to group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalActorGroupsMixed, getAdditionalGroup());

        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalGroupsMixed(), getAdditionalGroup()));
    }

    public void testAddActorToGroupsByAuthorizedPerformer() throws Exception {
        assertFalse("Executor not added to groups ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
        Executor executor = getAdditionalActor();
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("Executor added to groups without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());

        assertTrue("Executor not added to groups ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testAddGroupToGroupsByAuthorizedPerformer() throws Exception {

        assertFalse("Executor not added to groups ", th.isExecutorInGroups(additionalGroup, additionalGroups));
        Executor executor = additionalGroup;
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), executor, additionalGroups);
            assertTrue("Executor added to groups without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);

        executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());

        assertTrue("Executor not added to groups ", th.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testAddExecutorsToGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.addExecutorsToGroup(th.getUnauthorizedPerformerSubject(), additionalActorGroupsMixed, additionalGroup);
            assertTrue("Executors not added to group ", th.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorToGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = additionalActor;
        try {
            executorService.addExecutorToGroups(th.getUnauthorizedPerformerSubject(), executor, additionalGroups);
            assertTrue("Executor not added to groups ", th.isExecutorInGroups(additionalActor, additionalGroups));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutorsToGroup() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        List<Executor> fakeExecutors = Lists.newArrayList(th.getFakeActor(), th.getFakeGroup());
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), fakeExecutors, additionalGroup);
            assertTrue("Executors added to group ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutorToGroups() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);

        Executor fakeExecutor = th.getFakeActor();
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), fakeExecutor, additionalGroups);
            assertTrue("Executor added to groups ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testAddNullExecutorToGroups() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);
        Executor nullExecutor = null;
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), nullExecutor, additionalGroups);
            fail("Null Executor added to groups ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorsToGroupWithNullSubject() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(null, additionalActorGroupsMixed, additionalGroup);
            assertTrue("Executors added to group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorToGroupsWithNullSubject() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);
        Executor executor = additionalActor;
        try {
            executorService.addExecutorToGroups(null, executor, additionalGroups);
            assertTrue("Executor added to group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {

        th.releaseResources();
        executorService = null;

        additionalActor = null;
        additionalGroup = null;
        additionalGroups = null;
        additionalActors = null;
        additionalActorGroupsMixed = null;
        super.tearDown();
    }

}
