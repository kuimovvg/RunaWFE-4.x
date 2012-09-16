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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetActorTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetActorTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateGetActorTest.class);
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
        super.setUp();
    }

    public void testGetActorByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = executorService.getActor(th.getAuthorizedPerformerSubject(), testPrefix
                + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", actor, returnedBaseGroupActor);
        Actor returnedSubGroupActor = executorService.getActor(th.getAuthorizedPerformerSubject(), testPrefix
                + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        Actor subGroupActor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", subGroupActor, returnedSubGroupActor);
    }

    public void testGetActorByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getActor(th.getUnauthorizedPerformerSubject(), testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            assertTrue("buisnessDelegete allow to getActor() to performer without Permission.READ.", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getActor(th.getUnauthorizedPerformerSubject(), testPrefix + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
            assertTrue("buisnessDelegete allow to getActor() to performer without Permission.READ.", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedActorByAuthorizedPerformer() throws Exception {
        try {
            executorService.getActor(th.getAuthorizedPerformerSubject(), testPrefix + "unexistent actor name");
            assertTrue("buisnessDelegete does not throw Exception to getActor() to performer without Permission.READ.", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    public void testGetNullActorByAuthorizedPerformer() throws Exception {
        try {
            String nullActorName = null;
            executorService.getActor(th.getAuthorizedPerformerSubject(), nullActorName);
            assertTrue("buisnessDelegete allow to getActor()with null actor.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetActorByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getActor(nullSubject, testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            assertTrue("buisnessDelegete allow to getActor() to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetActorInsteadOfGroup() throws Exception {
        try {
            executorService.getActor(th.getAuthorizedPerformerSubject(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            assertTrue("buisnessDelegete allow to getActor() where the group really is returned.", false);
        } catch (ExecutorOutOfDateException e) {
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
