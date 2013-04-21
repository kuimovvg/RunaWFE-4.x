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
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.List;
import java.util.Map;

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
        executorService = Delegates.getExecutorService();
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

    public void testgetExecutorByNameByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix
                + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", actor, returnedBaseGroupActor);
        Actor returnedSubGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix
                + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        Actor subGroupActor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by buisnessDelegete differes with expected", subGroupActor, returnedSubGroupActor);
    }

    public void testgetExecutorByNameByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("buisnessDelegete allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
            fail("buisnessDelegete allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedActorByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + "unexistent actor name");
            fail("buisnessDelegete does not throw Exception to getExecutorByName() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            //That's what we expect
        }
    }

    public void testGetNullActorByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), null);
            fail("buisnessDelegete allow to getExecutorByName()with null actor.");
        } catch (NullPointerException e) {
            //That's what we expect
        }
    }

    public void testgetExecutorByNameByNullPerformer() throws Exception {
        try {
            executorService.getExecutorByName(null, testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("buisnessDelegete allow to getExecutorByName() to performer with null subject.");
        } catch (NullPointerException e) {
            //That's what we expect
        }
    }

    public void testgetExecutorByNameInsteadOfGroup() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("buisnessDelegete allow to getExecutorByName() where the group really is returned.");
        } catch (ExecutorDoesNotExistException e) {
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
