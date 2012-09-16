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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 16.02.2005
 */
public class AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest extends ServletTestCase {
    private ServiceTestHelper th;
    private AuthorizationService authorizationService;

    Collection<Permission> testPermission = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);

    private static String testPrefix = AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest.class.getName();

    private Group additionalGroup;
    private Actor additionalActor;

    private List<Executor> additionalActorGroupsMixed;
    private List<Long> executorIDs;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest.class);
    }

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(testPrefix);

        Collection<Permission> readUpdateSystemPermission = Lists.newArrayList(SystemPermission.READ, SystemPermission.UPDATE_PERMISSIONS);
        Collection<Permission> readUpdateExecutorPermission = Lists.newArrayList(ExecutorPermission.READ, ExecutorPermission.UPDATE_PERMISSIONS);

        th.setPermissionsToAuthorizedPerformerOnSystem(readUpdateSystemPermission);

        authorizationService = DelegateFactory.getInstance().getAuthorizationService();

        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("mixed", "Additional Mixed");
        executorIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorIDs.add(executor.getId());
            th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, executor);
        }

        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalActor);
        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalGroup);

        super.setUp();
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), executorIDs, testPermission, additionalActor);

        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, DelegateFactory.getInstance().getExecutorService().getExecutor(
                    th.getAuthorizedPerformerSubject(), executorIDs.get(i)));
        }

        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getOwnPermissions(th.getAuthorizedPerformerSubject(), additionalActorGroupsMixed.get(i),
                    additionalActor);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }

        authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), executorIDs, testPermission, additionalGroup);

        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, DelegateFactory.getInstance().getExecutorService().getExecutor(
                    th.getAuthorizedPerformerSubject(), executorIDs.get(i)));
        }

        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getOwnPermissions(th.getAuthorizedPerformerSubject(), additionalActorGroupsMixed.get(i),
                    additionalGroup);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }
    }

    public void testSetPermissionsNullSubject() throws Exception {
        try {
            authorizationService.setPermissions(null, executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(th.getFakeSubject(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), executorIDs, testPermission, th.getFakeActor());
            fail("AuthorizationDelegate.setPermissions() allows fake executor");
        } catch (InternalApplicationException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsNullPermissions() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), executorIDs, (Collection<Permission>) null, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows null permissions");
        } catch (IllegalArgumentException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), executorIDs, testPermission, null);
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsFakeExecutors() throws Exception {
        try {
            authorizationService.setPermissions(th.getAuthorizedPerformerSubject(), Lists.newArrayList(-1L, -2L, -3L), testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows Fake Executors");
        } catch (ExecutorOutOfDateException e) {
            //This is what we expect
        }
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerSubject(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            //This is what we expect
        }

        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerSubject(), executorIDs, testPermission, additionalGroup);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            //This is what we expect
        }

    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

}
