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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateCreateGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateCreateGroupTest.class.getName();

    private final List<Permission> createPermissions = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);

    private Group group;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateCreateGroupTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.setPermissionsToAuthorizedPerformerOnSystem(createPermissions);
        super.setUp();
    }

    public void testCreateGroupByAuthorizedPerformer() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = executorService.create(th.getAuthorizedPerformerSubject(), group);
        assertTrue("Executor (group) does not exists ", th.isExecutorExist(group));
        Group returnedGroup = executorService.getGroup(th.getAuthorizedPerformerSubject(), group.getId());
        assertEquals("Returned group differes with created one", group, returnedGroup);
    }

    public void testCreateExecutorByUnAuthorizedPerformer() throws Exception {

        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(th.getUnauthorizedPerformerSubject(), group);
            fail("ExecutorServiceDelegate.create(group) creates executor without permissions");
        } catch (AuthorizationException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor exists ", th.isExecutorExist(group));
    }

    public void testCreateAlreadyExistedGroup() throws Exception {
        Group group2 = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = executorService.create(th.getAuthorizedPerformerSubject(), group2);
        assertTrue("Executor does not exists ", th.isExecutorExist(group));
        try {
            executorService.create(th.getAuthorizedPerformerSubject(), group2);
            fail("ExecutorServiceDelegate.create(group) creates already existed group");
        } catch (ExecutorAlreadyExistsException e) {
            // This is supposed result of operation
        }
        Group returnedGroup = executorService.getGroup(th.getAuthorizedPerformerSubject(), group.getId());
        assertEquals("Returned actor differes with created one", group, returnedGroup);
    }

    public void testCreateAlreadyExistedActor() throws Exception {
        Actor actor = th.createActorIfNotExist("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = new Group(actor.getName(), actor.getDescription());
        try {
            executorService.create(th.getAuthorizedPerformerSubject(), group);
            fail("ExecutorServiceDelegate.create(group) creates already existed actor");
        } catch (ExecutorAlreadyExistsException e) {
            // This is supposed result of operation
        }
    }

    public void testCreateNullExecutorByAuthorizedPerformer() throws Exception {
        Group group2 = null;
        try {
            group = executorService.create(th.getAuthorizedPerformerSubject(), group2);
            fail("null executor created");
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    public void testCreateExecutorWithNullSubject() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(null, group);
            fail("executor with null subject created");
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor does not exists ", th.isExecutorExist(group));
    }

    public void testCreateExecutorWithFakeSubject() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(th.getFakeSubject(), group);
            fail("executor with fake subject created");
        } catch (AuthenticationException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor does not exists ", th.isExecutorExist(group));
    }

    protected void tearDown() throws Exception {
        th.removeExecutorIfExists(group);
        group = null;

        th.releaseResources();
        executorService = null;
        group = null;
        super.tearDown();
    }

}
