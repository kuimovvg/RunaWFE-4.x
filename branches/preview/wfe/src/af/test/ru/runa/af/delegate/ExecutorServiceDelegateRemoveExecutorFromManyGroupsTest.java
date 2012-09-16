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
public class ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest.class.getName();

    private long additionalGroupId;
    private long additionalActorId;

    private List<Long> additionalGroupsIds;

    private final Collection<Permission> addToGroupReadListPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> removeFromGroupReadPermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        Actor additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        Group additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        List<Group> additionalGroups = th.createGroupArray("additionalGroups", "Additional Groups");

        additionalActorId = additionalActor.getId();
        additionalGroupId = additionalGroup.getId();
        additionalGroupsIds = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIds.add(group.getId());
        }

        th.setPermissionsToAuthorizedPerformer(readPermissions, getAdditionalActor());
        th.setPermissionsToAuthorizedPerformer(readPermissions, getAdditionalGroup());
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupReadListPermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), getAdditionalActor(), getAdditionalGroups());
        executorService.addExecutorToGroups(th.getAuthorizedPerformerSubject(), getAdditionalGroup(), getAdditionalGroups());
        super.setUp();
    }

    private List<Group> getAdditionalGroups() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getGroups(th.getAdminSubject(), additionalGroupsIds);
    }

    private Actor getAdditionalActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getActor(th.getAdminSubject(), additionalActorId);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getGroup(th.getAdminSubject(), additionalGroupId);
    }

    public void testRemoveActorFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("Executors removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(removeFromGroupReadPermissions, getAdditionalGroups());

        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testRemoveGrouprFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("Executors removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(removeFromGroupReadPermissions, getAdditionalGroups());

        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testRemoveActorFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("Executor is removed from groups ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveGroupFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("Executor is removed from groups ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActorFromGroups() throws Exception {
        Executor executor = th.getFakeActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("FakeExecutor removed from groups ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeGroupFromGroups() throws Exception {
        Executor executor = th.getFakeGroup();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("FakeExecutor removed from groups ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testRemoveNullExecutorFromGroups() throws Exception {
        Executor executor = null;
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, getAdditionalGroups());
            assertTrue("NullExecutor removed from groups ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveExecutorFromNullGroups() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerSubject(), executor, null);
            assertTrue("Executor removed from Null groups ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveActorWithNullSubjectFromGroups() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(null, executor, getAdditionalGroups());
            assertTrue("Executor removed from groups with null subject", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveGroupWithNullSubjectFromGroups() throws Exception {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(null, executor, getAdditionalGroups());
            assertTrue("Executor removed from groups with null subject", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        super.tearDown();
    }
}
