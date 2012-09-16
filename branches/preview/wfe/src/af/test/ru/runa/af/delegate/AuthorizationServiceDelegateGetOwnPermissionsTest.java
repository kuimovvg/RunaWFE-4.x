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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateGetOwnPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;
    private AuthorizationService authorizationService;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateGetOwnPermissionsTest.class);
    }

    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetOwnPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> p = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformerOnSystem(p);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetOwnPermissionsNullSubject() throws Exception {
        try {
            authorizationService.getOwnPermissions(null, helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getFakeSubject(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetOwnPermissionsNullExecutor() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), null, helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows null executor");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeExecutor() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getFakeActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows fake executor");
        } catch (ExecutorOutOfDateException e) {
        }
    }

    public void testGetOwnPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), null);
            fail("AuthorizationDelegate.getOwnPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getFakeActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testGetOwnPermissions() throws Exception {
        Collection<Permission> noPermission = Lists.newArrayList();
        Collection<Permission> expected = Lists.newArrayList(Permission.READ);

        Collection<Permission> actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getOwnPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), expected, helper.getAASystem());
        actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getOwnPermissions() returns wrong permissions", expected, actual);

        actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getOwnPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), expected, helper.getBaseGroupActor());
        actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getOwnPermissions() returns wrong permissions", expected, actual);
    }

    public void testGetOwnPermissionsGroupPermissions() throws Exception {
        Collection<Permission> noPermission = Lists.newArrayList();
        Collection<Permission> expected = Lists.newArrayList(Permission.READ);

        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), expected, helper.getAASystem());
        Collection<Permission> actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getOwnPermissions() returns wrong permissions", noPermission, actual);

    }

    public void testGetOwnPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.getOwnPermissions(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getAASystem());
            fail("AuthorizationDelegate.getOwnPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.getOwnPermissions(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getOwnPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }
}
