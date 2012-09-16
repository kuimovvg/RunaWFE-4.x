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
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetExecutorTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateGetExecutorTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        super.setUp();
    }

    public void testGetActorByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = (Actor) executorService.getExecutor(th.getAuthorizedPerformerSubject(), testPrefix
                + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", actor, returnedBaseGroupActor);
        Group returnedBaseGroup = (Group) executorService.getExecutor(th.getAuthorizedPerformerSubject(), testPrefix
                + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", group, returnedBaseGroup);
    }

    public void testGetExecutorByUnauthorizedPerformer() throws Exception {
        Subject unauthorizedPerformerSubject = th.getUnauthorizedPerformerSubject();
        try {
            executorService.getExecutor(unauthorizedPerformerSubject, testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            assertTrue("buisnessDelegete allow to getExecutor() to performer without Permission.READ.", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getExecutor(unauthorizedPerformerSubject, testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            assertTrue("buisnessDelegete allow to getExecutor() to performer without Permission.READ.", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistentExecutorByAuthorizedPerformer() throws Exception {
        Subject authorizedPerformerSubject = th.getAuthorizedPerformerSubject();
        try {
            executorService.getExecutor(authorizedPerformerSubject, testPrefix + "unexistent actor name");
            assertTrue("buisnessDelegete does not throw Exception to getExecutor() to performer without Permission.READ.", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    public void testGetNullExecutorByAuthorizedPerformer() throws Exception {
        Subject authorizedPerformerSubject = th.getAuthorizedPerformerSubject();
        try {
            String nullActorName = null;
            executorService.getExecutor(authorizedPerformerSubject, nullActorName);
            assertTrue("buisnessDelegete allow to getExecutor()with null actor.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorActorByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getExecutor(nullSubject, actor.getName());
            assertTrue("buisnessDelegete allow to getExecutor() (Actor really) to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getExecutor(nullSubject, group.getName());
            assertTrue("buisnessDelegete allow to getExecutor() (Group really) to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
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
