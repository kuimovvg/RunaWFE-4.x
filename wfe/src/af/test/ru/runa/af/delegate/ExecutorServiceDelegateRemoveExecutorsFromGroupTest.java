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

public class ExecutorServiceDelegateRemoveExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveExecutorsFromGroupTest.class.getName();

    private Actor actor;

    private Group group;

    private Group subGroup;

    private final Collection<Permission> removeFromGroupReadPermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateRemoveExecutorsFromGroupTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();

        actor = th.getBaseGroupActor();
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = th.getBaseGroup();
        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, group);
        subGroup = th.getSubGroup();
        th.setPermissionsToAuthorizedPerformer(readPermissions, subGroup);

        super.setUp();
    }

    private Actor getActor() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return executorService.getActor(th.getAdminSubject(), actor.getId());
    }

    private Group getGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return executorService.getGroup(th.getAdminSubject(), group.getId());
    }

    private Group getSubGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return executorService.getGroup(th.getAdminSubject(), subGroup.getId());
    }

    public void testRemoveActorFromGroupByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorInGroup(actor, group));

        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(actor), group);
            assertTrue("Actor removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, group);

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(actor), group);

        assertFalse("Executor not removed from group ", th.isExecutorInGroup(getActor(), getGroup()));
    }

    public void testRemoveSubGroupFromGroupByAuthorizedPerformerWithReadPermissionOnGroup() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorInGroup(subGroup, group));

        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(subGroup), group);
            assertTrue("Subgroup removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, group);

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(subGroup), group);

        assertFalse("Executor not removed from group ", th.isExecutorInGroup(getSubGroup(), getGroup()));
    }

    public void testRemoveActorFromGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerSubject(), Lists.newArrayList(actor), group);
            assertTrue("Actor is removed from group ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveSubGroupFromGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerSubject(), Lists.newArrayList(subGroup), group);
            assertTrue("SubGroup is removed from group ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActor() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(th.getFakeActor()), group);
            assertTrue("FakeActor removed from group ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeSubGroup() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(th.getFakeGroup()), group);
            assertTrue("FakeGroup removed from group ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testRemoveNullExecutor() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList((Executor) null), group);
            assertTrue("NullExecutor removed from group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }

        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), null, group);
            assertTrue("NullExecutor removed from group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveActorWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, Lists.newArrayList(actor), group);
            assertTrue("Actor removed from group with null subject", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveSubGroupWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, Lists.newArrayList(subGroup), group);
            assertTrue("SubGroup removed from group with null subject", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;

        super.tearDown();
    }
}
