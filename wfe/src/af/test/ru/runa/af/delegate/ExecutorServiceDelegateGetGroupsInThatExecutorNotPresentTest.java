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
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetGroupsInThatExecutorNotPresentTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupsInThatExecutorNotPresentTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static TestSuite suite() {
        return new TestSuite(ExecutorServiceDelegateGetGroupsInThatExecutorNotPresentTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, subGroup);

        actor = executorService.getActor(th.getAdminSubject(), actor.getId());
        group = executorService.getGroup(th.getAdminSubject(), group.getId());
        subGroup = executorService.getGroup(th.getAdminSubject(), subGroup.getId());

        super.setUp();
    }

    final public void testGetGroupsInThatExecutorNotPresentByAuthorizedPerformer1() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerSubject(), actor, th.getExecutorBatchPresentation(),
                true);
        List<Group> realGroups = Lists.newArrayList(group);
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getGroupsInThatExecutorNotPresent() returns wrong group set", realGroups,
                calculatedGroups);
    }

    final public void testGetGroupsInThatExecutorNotPresentByAuthorizedPerformer2() throws Exception {
        List<Group> calculatedGroups = executorService.getExecutorGroups(th.getAuthorizedPerformerSubject(), group, th.getExecutorBatchPresentation(),
                true);
        List<Group> realGroups = Lists.newArrayList(subGroup);
        ArrayAssert.assertWeakEqualArrays("buisnessDelegete.getGroupsInThatExecutorNotPresent() returns wrong group set", realGroups,
                calculatedGroups);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorGroups(th.getUnauthorizedPerformerSubject(), actor, th.getExecutorBatchPresentation(), true);
            assertTrue("buisnessDelegete.getGroupsInThatExecutorNotPresent() no AuthorizationFailedException", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithNullSubject() throws Exception {
        try {
            executorService.getExecutorGroups(null, actor, th.getExecutorBatchPresentation(), true);
            assertTrue("GetGroupsInThatExecutorNotPresentwithNullSubject no Exception", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithoutPermission() throws Exception {
        try {
            List<Permission> noPermissions = Lists.newArrayList();
            th.setPermissionsToAuthorizedPerformer(noPermissions, actor);
            actor = executorService.getActor(th.getAdminSubject(), actor.getId());
            executorService.getExecutorGroups(th.getAuthorizedPerformerSubject(), actor, th.getExecutorBatchPresentation(), true);
            assertTrue("testGetGroupsInThatExecutorNotPresentwithoutPermission no Exception", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupswithFakeSubject() throws Exception {
        try {
            Subject fakeSubject = th.getFakeSubject();
            executorService.getExecutorGroups(fakeSubject, actor, th.getExecutorBatchPresentation(), true);
            assertTrue("testGetGroupsInThatExecutorNotPresentwithoutPermission no Exception", false);
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
