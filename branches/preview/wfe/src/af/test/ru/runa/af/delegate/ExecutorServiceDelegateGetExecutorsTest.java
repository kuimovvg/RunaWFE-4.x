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
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 16.05.2005
 */
public class ExecutorServiceDelegateGetExecutorsTest extends ServletTestCase {
    private ServiceTestHelper th;
    private ExecutorService executorService;
    private static String testPrefix = ExecutorServiceDelegateGetExecutorsTest.class.getName();

    private List<Executor> additionalActorGroupsMixed;
    private final List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
    private List<Long> executorsIDs;

    public static TestSuite suite() {
        return new TestSuite(ExecutorServiceDelegateGetExecutorsTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActorGroupsMixed);

        executorsIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorsIDs.add(executor.getId());
        }

        super.setUp();
    }

    public void testGetExecutorsByAuthorizedPerformer() throws Exception {
        List<Executor> returnedExecutors = executorService.getExecutors(th.getAuthorizedPerformerSubject(), executorsIDs);
        ArrayAssert
                .assertWeakEqualArrays("buisnessDelegete.getExecutors() returns wrong executor set", additionalActorGroupsMixed, returnedExecutors);
    }

    public void testGetExecutorsByUnauthorizedPerformer() throws Exception {
        Subject unauthorizedPerformerSubject = th.getUnauthorizedPerformerSubject();
        try {
            executorService.getExecutors(unauthorizedPerformerSubject, executorsIDs);
            assertTrue("buisnessDelegete allow to getExecutor() to performer without Permission.READ.", false);
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedExecutorByAuthorizedPerformer() throws Exception {
        executorsIDs = Lists.newArrayList(-1L, -2L, -3L);
        try {
            executorService.getExecutors(th.getAuthorizedPerformerSubject(), executorsIDs);
            assertTrue("buisnessDelegete does not throw Exception to getExecutor() for UnexistedExecutor", false);
        } catch (ExecutorOutOfDateException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorsByNullPerformer() throws Exception {
        try {
            Subject nullSubject = null;
            executorService.getExecutors(nullSubject, executorsIDs);
            assertTrue("buisnessDelegete allow to getExecutors() to performer with null subject.", false);
        } catch (IllegalArgumentException e) {
            //That's what we expect 
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        executorsIDs = null;
        additionalActorGroupsMixed = null;
        super.tearDown();
    }
}
