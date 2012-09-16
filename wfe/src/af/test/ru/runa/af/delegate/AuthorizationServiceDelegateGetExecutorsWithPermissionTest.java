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
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateGetExecutorsWithPermissionTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(AuthorizationServiceDelegateGetExecutorsWithPermissionTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetExecutorsWithPermissionTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemP = Lists.newArrayList(Permission.READ, SystemPermission.CREATE_EXECUTOR);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = DelegateFactory.getInstance().getAuthorizationService();
        batchPresentation = helper.getExecutorBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetExecutorsWithPermissionNullSubject() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(null, helper.getAASystem(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetExecutorsWithPermissionFakeSubject() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getFakeSubject(), helper.getAASystem(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetExecutorsWithPermissionNullIdentifiable() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerSubject(), null, batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetExecutorsWithPermissionFakeIdentifiable() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerSubject(), helper.getFakeActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testGetExecutorsWithPermission() throws Exception {
        List<Actor> expected = Lists.newArrayList(helper.getAuthorizedPerformerActor(), helper.getBaseGroupActor());
        List<Executor> actual = authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(),
                batchPresentation, true);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getExecutorsWithPermission() returns wrong executors", expected, actual);
    }

    public void testGetExecutorsWithPermissionUnauthorized() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getUnauthorizedPerformerSubject(), helper.getBaseGroupActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
