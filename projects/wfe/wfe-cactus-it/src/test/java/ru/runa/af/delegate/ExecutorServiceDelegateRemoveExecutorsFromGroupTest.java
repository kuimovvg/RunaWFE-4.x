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

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;

import java.util.Collection;

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
        executorService = Delegates.getExecutorService();
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

    private Actor getActor() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), actor.getId());
    }

    private Group getGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), group.getId());
    }

    private Group getSubGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), subGroup.getId());
    }

    public void testRemoveActorFromGroupByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorInGroup(actor, group));

        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(actor.getId()), group.getId());
            assertTrue("Actor removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, group);

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(actor.getId()), group.getId());

        assertFalse("Executor not removed from group ", th.isExecutorInGroup(getActor(), getGroup()));
    }

    public void testRemoveSubGroupFromGroupByAuthorizedPerformerWithReadPermissionOnGroup() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorInGroup(subGroup, group));

        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(subGroup.getId()), group.getId());
            assertTrue("Subgroup removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, group);

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(subGroup.getId()), group.getId());

        assertFalse("Executor not removed from group ", th.isExecutorInGroup(getSubGroup(), getGroup()));
    }

    public void testRemoveActorFromGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerUser(), Lists.newArrayList(actor.getId()), group.getId());
            assertTrue("Actor is removed from group ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveSubGroupFromGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerUser(), Lists.newArrayList(subGroup.getId()), group.getId());
            assertTrue("SubGroup is removed from group ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActor() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(th.getFakeActor().getId()), group.getId());
            assertTrue("FakeActor removed from group ", false);
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeSubGroup() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList(th.getFakeGroup().getId()), group.getId());
            assertTrue("FakeGroup removed from group ", false);
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
        }
    }

    /*
    public void testRemoveNullExecutor() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), Lists.newArrayList((Executor) null), group.getId());
            assertTrue("NullExecutor removed from group ", false);
        } catch (NullPointerException e) {
            // this is supposed result
        }

        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), null, group);
            assertTrue("NullExecutor removed from group ", false);
        } catch (NullPointerException e) {
            // this is supposed result
        }
    }
    */

    public void testRemoveActorWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, Lists.newArrayList(actor.getId()), group.getId());
            assertTrue("Actor removed from group with null subject", false);
        } catch (NullPointerException e) {
            // this is supposed result
        }
    }

    public void testRemoveSubGroupWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, Lists.newArrayList(subGroup.getId()), group.getId());
            assertTrue("SubGroup removed from group with null subject", false);
        } catch (NullPointerException e) {
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
