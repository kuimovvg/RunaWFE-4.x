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

import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupTest.class.getName();

    private Group group;

    private Map<String, Executor> executorsMap;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateGetGroupTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getSubGroup());
        super.setUp();
    }

    public void testGetGroupByAuthorizedPerformer() throws Exception {
        Group returnedBaseGroup = executorService.getGroup(th.getAuthorizedPerformerSubject(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", group, returnedBaseGroup);
        Group returnedSubGroup = executorService.getGroup(th.getAuthorizedPerformerSubject(), testPrefix + ServiceTestHelper.SUB_GROUP_NAME);
        Group subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", subGroup, returnedSubGroup);
    }

    public void testGetGroupByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroup(th.getUnauthorizedPerformerSubject(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            assertTrue("buisnessDelegete allow to getGroup()", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getGroup(th.getUnauthorizedPerformerSubject(), testPrefix + ServiceTestHelper.SUB_GROUP_NAME);
            assertTrue("buisnessDelegete allow to getGroup()", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedGroupByAuthorizedPerformer() throws Exception {
        try {
            executorService.getGroup(th.getAuthorizedPerformerSubject(), testPrefix + "unexistent group name");
            assertTrue("buisnessDelegete does not throw Exception to getGroup() in testGetUnexistedGroupByAuthorizedPerformer", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    public void testGetNullGroupByAuthorizedPerformer() throws Exception {
        try {
            String nullGroupName = null;
            executorService.getGroup(th.getAuthorizedPerformerSubject(), nullGroupName);
            assertTrue("buisnessDelegete allow to getGroup()with null group.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetGroupByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getGroup(nullSubject, testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            assertTrue("buisnessDelegete allow to getGroup() to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetActorInsteadOfGroup() throws Exception {
        try {
            executorService.getGroup(th.getAuthorizedPerformerSubject(), testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            assertTrue("buisnessDelegete allow to getGroup() where the actor really is returned.", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        group = null;
        executorsMap = null;
        super.tearDown();
    }
}
