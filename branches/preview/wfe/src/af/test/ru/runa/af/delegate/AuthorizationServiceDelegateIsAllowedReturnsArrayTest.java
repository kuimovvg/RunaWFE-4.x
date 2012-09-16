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
import ru.runa.af.AuthenticationException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

public class AuthorizationServiceDelegateIsAllowedReturnsArrayTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateIsAllowedTest.class);
    }

    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemP = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        helper = null;
        authorizationService = null;
        super.tearDown();
    }

    public void testIsAllowedNullSubject() throws Exception {
        try {
            authorizationService.isAllowed(null, Permission.READ, Lists.newArrayList(helper.getAASystem()));
            fail("AuthorizationDelegate.isAllowed() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getFakeSubject(), Permission.READ, Lists.newArrayList(helper.getAASystem()));
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testIsAllowedPermissionSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), null, Lists.newArrayList(helper.getAASystem()));
            fail("AuthorizationDelegate.isAllowed() allows null permission");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedNullIdentifiable() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, (List<Identifiable>) null);
            fail("AuthorizationDelegate.isAllowed() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedNullIdentifiables() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, Lists.newArrayList((Identifiable) null, null));
            fail("AuthorizationDelegate.isAllowed() allows null identifiables");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeIdentifiable() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, Lists.newArrayList(helper.getFakeActor()));
            fail("AuthorizationDelegate.isAllowed() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }

        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, Lists.newArrayList(helper.getBaseGroupActor(),
                    helper.getFakeActor()));
            fail("AuthorizationDelegate.isAllowed() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testIsAllowedAASystem() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), SystemPermission.CREATE_EXECUTOR,
                Lists.newArrayList(helper.getAASystem()));
        boolean[] expected = { true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, Lists.newArrayList(helper.getAASystem()));
        expected = new boolean[] { false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutor() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, Lists.newArrayList(
                helper.getBaseGroupActor(), helper.getBaseGroup()));
        boolean[] expected = { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.UPDATE_PERMISSIONS, Lists.newArrayList(
                helper.getBaseGroupActor(), helper.getBaseGroup()));
        expected = new boolean[] { false, false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), ExecutorPermission.UPDATE, Lists.newArrayList(
                helper.getBaseGroupActor(), helper.getBaseGroup()));
        expected = new boolean[] { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutorUnauthorized() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getUnauthorizedPerformerSubject(), Permission.READ, Lists.newArrayList(
                helper.getAASystem(), helper.getBaseGroupActor(), helper.getBaseGroup()));
        boolean[] expected = { false, false, false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }
}
