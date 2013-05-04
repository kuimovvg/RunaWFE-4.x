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
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetGroupChildrenTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupChildrenTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
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

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        group = executorService.getExecutor(th.getAdminUser(), group.getId());
        subGroup = executorService.getExecutor(th.getAdminUser(), subGroup.getId());

        super.setUp();
    }

    final public void testGetGroupChildrenByAuthorizedPerformer() throws Exception {
        List<Executor> calculatedGroupChildren = executorService.getGroupChildren(th.getAuthorizedPerformerUser(), group, th
                .getExecutorBatchPresentation(), false);
        List<Executor> realGroupChildren = Lists.newArrayList(th.getBaseGroupActor(), th.getSubGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroupChildren, calculatedGroupChildren);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroupChildren(th.getUnauthorizedPerformerUser(), group, th.getExecutorBatchPresentation(), false);
            assertTrue("businessDelegate.getGroupChildrenByUnauthorizedPerformer() no AuthorizationFailedException", false);
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
            executorService.getGroupChildren(th.getAuthorizedPerformerUser(), group, th.getExecutorBatchPresentation(), false);
            assertTrue("testGetGroupChildrenwithoutPermission no Exception", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getGroupChildren(fakeUser, group, th.getExecutorBatchPresentation(), false);
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
