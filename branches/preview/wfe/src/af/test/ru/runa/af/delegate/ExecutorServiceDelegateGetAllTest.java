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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetAllTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetAllTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateGetAllTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, group);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME));
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME));
        super.setUp();
    }

    final public void testGetAllByAuthorizedPerformer() throws Exception {
        List<Executor> executors = executorService.getAll(th.getAuthorizedPerformerSubject(), th.getExecutorBatchPresentation());
        LinkedList<Executor> realExecutors = new LinkedList<Executor>(executorsMap.values());
        Actor authorizedPerformerActor = th.getAuthorizedPerformerActor();
        realExecutors.add(authorizedPerformerActor);
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getAll() returns wrong executor set", realExecutors, executors);
    }

    public void testGetAllByUnauthorizedPerformer() throws Exception {
        List<Executor> executors = executorService.getAll(th.getUnauthorizedPerformerSubject(), th.getExecutorBatchPresentation());
        List<Actor> unauthorizedPerformerArray = Lists.newArrayList(th.getUnauthorizedPerformerActor());
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getAll() returns wrong executor set", unauthorizedPerformerArray, executors);
    }

    public void testGetAllwithNullSubject() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getAll(nullSubject, th.getExecutorBatchPresentation());
            assertTrue("buisnessDelegete.getAll() with null subject throws no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetAllwithoutFakeSubject() throws Exception {
        try {
            Subject fakeSubject = th.getFakeSubject();
            executorService.getAll(fakeSubject, th.getExecutorBatchPresentation());
            assertTrue("buisnessDelegete.getAll() with fake subject throws no AuthenticationException", false);
        } catch (AuthenticationException e) {
            //That's what we expect
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
