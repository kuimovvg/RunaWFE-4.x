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

import java.util.Collection;
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateHierarchyPermissionsTest extends ServletTestCase {

    private static final String ACTOR_PWD = "ActorPWD";

    private ServiceTestHelper th;

    private ExecutorService executorService;

    private AuthorizationService authorizationService;

    private static String testPrefix = ExecutorServiceDelegateHierarchyPermissionsTest.class.getName();

    private Actor actor;

    private Group group;

    public static Test suite() {
        return new TestSuite(ExecutorServiceDelegateHierarchyPermissionsTest.class);
    }

    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();
        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> updatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        Collection<Permission> loginPermissions = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        Collection<Permission> createExecutorPermissions = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);
        Collection<Permission> addToGroupPermissions = Lists.newArrayList(Permission.READ, GroupPermission.ADD_TO_GROUP);
        Collection<Permission> grantAASystem = Lists.newArrayList(Permission.UPDATE_PERMISSIONS);

        Map<String, Executor> executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(updatePermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, group);

        th.setPermissionsToAuthorizedPerformerOnSystem(grantAASystem);

        actor = executorService.getActor(th.getAdminSubject(), actor.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), actor, loginPermissions, ASystem.SYSTEM);
        group = executorService.getGroup(th.getAdminSubject(), group.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), group, createExecutorPermissions, ASystem.SYSTEM);

        actor = executorService.getActor(th.getAdminSubject(), actor.getId());
        executorService.setPassword(th.getAuthorizedPerformerSubject(), actor, ACTOR_PWD);

        super.setUp();
    }

    public void testPermissionsInheritance() throws Exception {
        Subject additionalUserSubject = DelegateFactory.getInstance().getAuthenticationService().authenticate(actor.getName(), ACTOR_PWD);

        if (!authorizationService.isAllowed(additionalUserSubject, SystemPermission.CREATE_EXECUTOR, ASystem.SYSTEM)) {
            assertTrue("unproper createExecutor permission ", false);
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        authorizationService = null;
        actor = null;
        group = null;

        super.tearDown();
    }

}
