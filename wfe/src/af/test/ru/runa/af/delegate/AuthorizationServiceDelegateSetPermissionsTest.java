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
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateSetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private Collection<Permission> p = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateSetPermissionsTest.class);
    }

    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateSetPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

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

    public void testSetPermissionsNullSubject() throws Exception {
        try {
            authorizationService.setPermissions(null, helper.getBaseGroupActor(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(helper.getFakeSubject(), helper.getBaseGroupActor(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    //	TODO think of what to do with ambiguous null
    /*
     * public void testSetPermissionsNullExecutor() throws Exception { try { delegate.setPermissions(helper.getAuthorizedPerformerSubject(), null, p, helper.getAASystem()); fail("AuthorizationDelegate.setPermissions() allows null executor"); } catch (IllegalArgumentException e) { } }
     */
    public void testSetPermissionsFakeExecutor() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getFakeActor(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null executor");
        } catch (ExecutorOutOfDateException e) {
        }
    }

    public void testSetPermissionsNullPermissions() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), null, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows null permissions");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), p, null);
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSetPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getFakeActor());
            fail("AuthorizationDelegate.setPermissions() allows null identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), p, helper.getBaseGroupActor());
        actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getAASystem());
        actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetNoPermission() throws Exception {
        p = GroupPermission.getNoPermissions();
        authorizationService.setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getBaseGroup());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroup(), p, helper.getBaseGroupActor());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroupActor(), p, helper.getAASystem());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
