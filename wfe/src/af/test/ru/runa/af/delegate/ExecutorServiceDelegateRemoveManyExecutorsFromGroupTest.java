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

public class ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest.class.getName();

    private Group additionalGroup;

    private List<Executor> additionalActorGroupsMixed;

    private final Collection<Permission> addToGroupReadListPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> removeFromGroupReadPermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest.class);
    }

    private List<Executor> getAdditionalExecutorsMixed() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            ids.add(executor.getId());
        }
        return executorService.getExecutors(th.getAdminSubject(), ids);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return executorService.getGroup(th.getAuthorizedPerformerSubject(), additionalGroup.getId());
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformer(addToGroupReadListPermissions, additionalGroup);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActorGroupsMixed);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), additionalActorGroupsMixed, additionalGroup);

        super.setUp();
    }

    public void testRemoveExecutorsFromGroupByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorsInGroup(getAdditionalExecutorsMixed(), getAdditionalGroup()));

        List<Executor> executors = getAdditionalExecutorsMixed();
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), executors, getAdditionalGroup());
            assertTrue("Executors removed from group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), getAdditionalExecutorsMixed(), getAdditionalGroup());

        assertFalse("Executor not removed from group ", th.isExecutorsInGroup(getAdditionalExecutorsMixed(), getAdditionalGroup()));
    }

    public void testRemoveExecutorsFromGroupByUnAuthorizedPerformer() throws Exception {
        List<Executor> executors = getAdditionalExecutorsMixed();
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerSubject(), executors, getAdditionalGroup());
            assertTrue("Executors is removed from group ByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());
        List<Executor> executors = th.getFakeExecutors();
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), executors, getAdditionalGroup());
            assertTrue("FakeExecutors removed from group ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testRemoveNullExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());
        List<Executor> executors = Lists.newArrayList((Executor) null, null, null);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerSubject(), executors, getAdditionalGroup());
            assertTrue("NullExecutors removed from group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveExecutorsWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, getAdditionalExecutorsMixed(), getAdditionalGroup());
            assertTrue("Executors removed from group with null subject", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalGroup = null;
        additionalActorGroupsMixed = null;
        super.tearDown();
    }
}
