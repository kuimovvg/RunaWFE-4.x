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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateUpdateGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateUpdateGroupTest.class.getName();

    private Group group;

    private Group newGroup;

    private Group returnedBaseGroup;

    private Map<String, Executor> executorsMap;

    private final List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateUpdateGroupTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        executorsMap = th.getDefaultExecutorsMap();

        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, group);

        newGroup = new Group("additionalGroup", "Additional Group");

        group = executorService.getGroup(th.getAdminSubject(), group.getId());

        super.setUp();
    }

    public void testUpdateGroupByAuthorizedPerformer() throws Exception {
        returnedBaseGroup = executorService.update(th.getAuthorizedPerformerSubject(), group, newGroup);
        assertEquals("group Name retuned by buisnessDelegete differes with expected", newGroup.getName(), returnedBaseGroup.getName());
        assertEquals("group Discription retuned by buisnessDelegete differes with expected", newGroup.getDescription(), returnedBaseGroup
                .getDescription());
    }

    public void testUpdateGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.update(th.getUnauthorizedPerformerSubject(), group, newGroup);
            assertTrue("No exception - UpdateGroupByUnAuthorizedPerformer", false);
        } catch (AuthorizationException e) {
            // This is supposed result of operation
        }
    }

    public void testUpdateNullGroupByAuthorizedPerformer() throws Exception {
        Group nullGroup = null;
        try {
            executorService.update(th.getAuthorizedPerformerSubject(), group, nullGroup);
            assertTrue("No exception - UpdateNullGroupByAuthorizedPerformer", false);
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    public void testUpdateGroupWithNullSubject() throws Exception {
        try {
            executorService.update(null, group, newGroup);
            assertTrue("No exception - UpdateGroupWithNullSubject", false);
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        group = null;
        newGroup = null;
        returnedBaseGroup = null;
        executorsMap = null;
        super.tearDown();
    }

}
