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
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
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
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static TestSuite suite() {
        return new TestSuite(ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> readUpdateAddToGroupPermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE, GroupPermission.ADD_TO_GROUP);
        Collection<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, group);
        th.setPermissionsToAuthorizedPerformer(readUpdateAddToGroupPermissions, subGroup);
        super.setUp();
    }

    private Group getSubGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return executorService.getGroup(th.getAdminSubject(), subGroup.getId());
    }

    final public void testGetExecutorsByAuthorizedPerformer1() throws Exception {
        List<Executor> calculatedExecutors = executorService.getGroupChildren(th.getAuthorizedPerformerSubject(), getSubGroup(), th
                .getExecutorBatchPresentation(), true);
        List<Executor> realExecutors = Lists.newArrayList(group, th.getAuthorizedPerformerActor());
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getExecutorGroups() returns wrong group set", realExecutors, calculatedExecutors);
    }

    final public void testGetExecutorsByAuthorizedPerformer2() throws Exception {
        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getBaseGroupActor());
        List<Executor> calculatedExecutors = executorService.getGroupChildren(th.getAuthorizedPerformerSubject(), getSubGroup(), th
                .getExecutorBatchPresentation(), true);
        List<Executor> realExecutors = Lists.newArrayList(group, th.getAuthorizedPerformerActor(), th.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getExecutors ...() returns wrong group set", realExecutors, calculatedExecutors);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroupChildren(th.getUnauthorizedPerformerSubject(), getSubGroup(), th.getExecutorBatchPresentation(), true);
            assertTrue("buisnessDelegete.getExecutorsByUnauthorizedPerformer() no AuthorizationFailedException", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithNullSubject() throws Exception {
        try {
            executorService.getGroupChildren(null, getSubGroup(), th.getExecutorBatchPresentation(), true);
            assertTrue("GetExecutorswithNullSubject no Exception", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithoutPermission() throws Exception {
        try {
            Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
            th.setPermissionsToAuthorizedPerformer(readPermissions, getSubGroup());
            executorService.getGroupChildren(th.getAuthorizedPerformerSubject(), getSubGroup(), th.getExecutorBatchPresentation(), true);
            assertTrue("testGetExecutorswithoutPermission no Exception", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithFakeSubject() throws Exception {
        try {
            Subject fakeSubject = th.getFakeSubject();
            executorService.getGroupChildren(fakeSubject, getSubGroup(), th.getExecutorBatchPresentation(), true);
            assertTrue("testGetExecutorswithoutPermission no Exception", false);
        } catch (AuthenticationException e) {
            //That's what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        executorsMap = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
        super.tearDown();
    }
}
