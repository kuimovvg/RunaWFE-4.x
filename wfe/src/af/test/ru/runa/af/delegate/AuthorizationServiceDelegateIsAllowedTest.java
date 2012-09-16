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

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateIsAllowedTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateIsAllowedTest.class);
    }

    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        helper.createDefaultExecutorsMap();

        List<Permission> systemP = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

        List<Permission> executorP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
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

    public void testIsAllowedNullSubject() throws Exception {
        try {
            authorizationService.isAllowed(null, Permission.READ, helper.getAASystem());
            fail("AuthorizationDelegate.isAllowed() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getFakeSubject(), Permission.READ, helper.getAASystem());
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testIsAllowedPermissionSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), null, helper.getAASystem());
            fail("AuthorizationDelegate.isAllowed() allows null permission");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedNullIdentifiable() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, (Identifiable) null);
            fail("AuthorizationDelegate.isAllowed() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsAllowedFakeIdentifiable() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(), Permission.READ, helper.getFakeActor());
            fail("AuthorizationDelegate.isAllowed() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testIsAllowedAASystem() throws Exception {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                SystemPermission.CREATE_EXECUTOR, helper.getAASystem()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                Permission.READ, helper.getAASystem()));
    }

    public void testIsAllowedExecutor() throws Exception {
        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                Permission.READ, helper.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                ExecutorPermission.UPDATE, helper.getBaseGroupActor()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                Permission.UPDATE_PERMISSIONS, helper.getBaseGroupActor()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                Permission.READ, helper.getBaseGroup()));

        assertTrue("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                ExecutorPermission.UPDATE, helper.getBaseGroup()));

        assertFalse("AuthorizationDelegate.isAllowed() returns wrong info", authorizationService.isAllowed(helper.getAuthorizedPerformerSubject(),
                Permission.UPDATE_PERMISSIONS, helper.getBaseGroup()));
    }

    public void testIsAllowedExecutorUnauthorized() throws Exception {
        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerSubject(), Permission.READ, helper.getAASystem()));

        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerSubject(), Permission.READ, helper.getBaseGroupActor()));

        assertFalse(authorizationService.isAllowed(helper.getUnauthorizedPerformerSubject(), Permission.READ, helper.getBaseGroup()));
    }

}
