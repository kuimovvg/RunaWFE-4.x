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

import ru.runa.af.Actor;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/*
 */
public class ExecutorServiceDelegateAddExecutorsToGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateAddExecutorsToGroupTest.class.getName();

    private Actor actor;

    private Group group;

    private Group additionalGroup;

    private Actor additionalActor;

    private final Collection<Permission> addToGroupPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> readlistPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateAddExecutorsToGroupTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> updatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);

        actor = th.getBaseGroupActor();
        th.setPermissionsToAuthorizedPerformer(updatePermissions, actor);
        group = th.getBaseGroup();
        th.setPermissionsToAuthorizedPerformer(updatePermissions, group);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        th.setPermissionsToAuthorizedPerformer(readPermissions, additionalActor);
        th.setPermissionsToAuthorizedPerformer(readlistPermissions, additionalGroup);

        super.setUp();
    }

    public void testAddExecutorByAuthorizedPerformer() throws Exception {
        assertFalse("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(additionalActor), additionalGroup);
            assertTrue("Executor added to group without corresponding permissions", false);
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(additionalActor), additionalGroup);

        additionalActor = executorService.getActor(th.getAdminSubject(), additionalActor.getId());
        additionalGroup = executorService.getGroup(th.getAdminSubject(), additionalGroup.getId());

        assertTrue("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
    }

    public void testAddExecutorByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.addExecutorsToGroup(th.getUnauthorizedPerformerSubject(), Lists.newArrayList(actor), additionalGroup);
            assertTrue("Executor not added to group ", th.isExecutorInGroup(additionalActor, additionalGroup));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList(th.getFakeActor()), additionalGroup);
            assertTrue("Executor added to group ", false);
        } catch (ExecutorOutOfDateException e) {
            // this is supposed result
        }
    }

    public void testAddNullExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), Lists.newArrayList((Executor) null), additionalGroup);
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }

        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerSubject(), null, additionalGroup);
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorWithNullSubject() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(null, Lists.newArrayList(actor), additionalGroup);
            assertTrue("Executor added to group ", false);
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {

        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;

        additionalActor = null;
        additionalGroup = null;
        super.tearDown();
    }

}
