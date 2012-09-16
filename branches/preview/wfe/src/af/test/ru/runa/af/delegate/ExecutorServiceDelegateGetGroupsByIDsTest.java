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

import javax.security.auth.Subject;

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
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 16.02.2005
 */
public class ExecutorServiceDelegateGetGroupsByIDsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateAddManyExecutorsToGroupsTest.class.getName();

    private List<Group> additionalGroups;
    private List<Long> additionalGroupsIDs;

    private final List<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    public static TestSuite suite() {
        return new TestSuite(ExecutorServiceDelegateGetGroupsByIDsTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        additionalGroups = th.createGroupArray("additionalG", "Additional Group");
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalGroups);

        additionalGroupsIDs = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIDs.add(group.getId());
        }
        super.setUp();
    }

    public void testGetGroupsByAuthorizedPerformer() throws Exception {
        List<Group> returnedGroups = executorService.getGroups(th.getAuthorizedPerformerSubject(), additionalGroupsIDs);
        ArrayAssert.assertWeakEqualArrays("Groups retuned by buisnessDelegete differes with expected", returnedGroups, additionalGroups);
    }

    public void testGetGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroups(th.getUnauthorizedPerformerSubject(), additionalGroupsIDs);
            assertTrue("buisnessDelegete allow to getGroups() with UnauthorizedPerformerSubject", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedGroupByAuthorizedPerformer() throws Exception {
        additionalGroupsIDs = Lists.newArrayList(-1L, -2L, -3L);
        try {
            executorService.getGroups(th.getAuthorizedPerformerSubject(), additionalGroupsIDs);
            assertTrue("buisnessDelegete does not throw Exception to getGroups() for unexisting groups", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    public void testGetGroupByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getGroups(nullSubject, additionalGroupsIDs);
            assertTrue("buisnessDelegete allow to getGroups() to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect 
        }
    }

    public void testGetActorsInsteadOfGroups() throws Exception {
        List<Executor> additional = th.createMixedActorsGroupsArray("mixed", "Additional mixed");
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additional);

        additionalGroupsIDs = Lists.newArrayList();
        for (Executor executor : additional) {
            additionalGroupsIDs.add(executor.getId());
        }
        try {
            executorService.getGroups(th.getAuthorizedPerformerSubject(), additionalGroupsIDs);
            assertTrue("buisnessDelegete allow to getGroup() where the actor really is returned.", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect 
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalGroupsIDs = null;
        additionalGroups = null;
        super.tearDown();
    }
}
