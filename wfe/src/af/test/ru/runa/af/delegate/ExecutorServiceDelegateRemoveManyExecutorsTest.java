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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateRemoveManyExecutorsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsTest.class.getName();

    private List<Executor> additionalActorsGroupsMixed;

    private final List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateRemoveManyExecutorsTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        additionalActorsGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");
        th.setPermissionsToAuthorizedPerformerOnExecutors(readUpdatePermissions, additionalActorsGroupsMixed);
    }

    public void testRemoveExecutorsByAuthorizedPerformer() throws Exception {
        executorService.remove(th.getAuthorizedPerformerSubject(), th.toIds(additionalActorsGroupsMixed));
        for (Executor executor : additionalActorsGroupsMixed) {
            assertFalse("Executor was not deleted.", th.isExecutorExist(executor));
            //th.removeCreatedExecutor(executor);
        }
    }

    public void testRemoveExecutorsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.remove(th.getUnauthorizedPerformerSubject(), th.toIds(additionalActorsGroupsMixed));
            fail("Executors were deleted by unauthorize performer.");
        } catch (AuthorizationException e) {
            // that's what we expect to see
        }
        for (Executor executor : additionalActorsGroupsMixed) {
            assertTrue("Executor was deleted.", th.isExecutorExist(executor));
        }
    }

    public void testRemoveNullExecutors() throws Exception {
        List<Long> ids = Lists.newArrayList((Long) null, null, null);
        try {
            executorService.remove(th.getAuthorizedPerformerSubject(), ids);
            fail("IllegalArgumentException was not thrown on RemoveNullExecutors.");
        } catch (IllegalArgumentException e) {
            //that's what we expect to see
        }
    }

    public void testRemoveFakeExecutors() throws Exception {
        try {
            for (Executor executor : th.getFakeExecutors()) {
                executorService.remove(th.getAuthorizedPerformerSubject(), executor);
            }
            assertTrue("ExecutorOutOfDateException was not thrown on remove method call with fakeExecutors argument.", false);
        } catch (ExecutorOutOfDateException e) {
            //that's what we expect to see
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalActorsGroupsMixed = null;
        super.tearDown();
    }
}
