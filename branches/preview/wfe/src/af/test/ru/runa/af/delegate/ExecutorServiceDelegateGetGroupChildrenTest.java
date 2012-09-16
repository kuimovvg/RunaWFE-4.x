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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetGroupChildrenTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupChildrenTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateGetGroupChildrenTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> readUpdateListPermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE, GroupPermission.LIST_GROUP);
        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdateListPermissions, group);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, subGroup);

        actor = executorService.getActor(th.getAdminSubject(), actor.getId());
        group = executorService.getGroup(th.getAdminSubject(), group.getId());
        subGroup = executorService.getGroup(th.getAdminSubject(), subGroup.getId());

        super.setUp();
    }

    final public void testGetGroupChildrenByAuthorizedPerformer() throws Exception {
        List<Executor> calculatedGroupChildren = executorService.getGroupChildren(th.getAuthorizedPerformerSubject(), group, th
                .getExecutorBatchPresentation(), false);
        List<Executor> realGroupChildren = Lists.newArrayList(th.getBaseGroupActor(), th.getSubGroup());
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getExecutorGroups() returns wrong group set", realGroupChildren, calculatedGroupChildren);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroupChildren(th.getUnauthorizedPerformerSubject(), group, th.getExecutorBatchPresentation(), false);
            assertTrue("buisnessDelegete.getGroupChildrenByUnauthorizedPerformer() no AuthorizationFailedException", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithNullSubject() throws Exception {
        try {
            executorService.getGroupChildren(null, group, th.getExecutorBatchPresentation(), false);
            assertTrue("GetGroupChildrenwithNullSubject no Exception", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithoutPermission() throws Exception {
        try {
            Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
            th.setPermissionsToAuthorizedPerformer(readPermissions, group);
            executorService.getGroupChildren(th.getAuthorizedPerformerSubject(), group, th.getExecutorBatchPresentation(), false);
            assertTrue("testGetGroupChildrenwithoutPermission no Exception", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithFakeSubject() throws Exception {
        try {
            Subject fakeSubject = th.getFakeSubject();
            executorService.getGroupChildren(fakeSubject, group, th.getExecutorBatchPresentation(), false);
            assertTrue("testGetGroupChildrenwithoutPermission no Exception", false);
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
