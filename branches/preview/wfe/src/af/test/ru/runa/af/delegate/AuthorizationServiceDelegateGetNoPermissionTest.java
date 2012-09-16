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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.ActorPermission;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateGetNoPermissionTest extends ServletTestCase {
    private ServiceTestHelper helper;
    private AuthorizationService authorizationService;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateGetNoPermissionTest.class);
    }

    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetNoPermissionTest.class.getName());
        helper.createDefaultExecutorsMap();

        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetNoPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.getNoPermission(null);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetAllPermissions() throws Exception {
        Permission expected;
        Permission actual;

        expected = new ActorPermission();
        actual = authorizationService.getNoPermission(helper.getBaseGroupActor());
        assertEquals("AuthorizationDelegate.getNoPermission() returns wrong permissions", expected, actual);

        expected = new GroupPermission();
        actual = authorizationService.getNoPermission(helper.getBaseGroup());
        assertEquals("AuthorizationDelegate.getNoPermission() returns wrong permissions", expected, actual);

        expected = new SystemPermission();
        actual = authorizationService.getNoPermission(helper.getAASystem());
        assertEquals("AuthorizationDelegate.getNoPermission() returns wrong permissions", expected, actual);
    }

}
