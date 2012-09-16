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
package ru.runa.af.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Permission;
import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.junit.WebArrayAssert;

import com.google.common.collect.Lists;

/**
 */
public class UpdatePermissionOnSystemActionTest extends StrutsTestCase {
    private static String testPrefix = UpdatePermissionOnSystemActionTest.class.getName();

    private static final String FAILURE = "/manage_system.do";

    private static final String SUCCESS = "/manage_system.do";

    private UpdatePermissionOnSystemAction action;

    private ActionMapping mapping;

    private UpdatePermissionsOnIdentifiableForm form;

    public static Test suite() {
        return new TestSuite(UpdatePermissionOnSystemActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new UpdatePermissionOnSystemAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        mapping = getActionMapping(forwards);
        form = new UpdatePermissionsOnIdentifiableForm();
        form.reset(mapping, request);
        List<Permission> updateP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformer(updateP, testHelper.getBaseGroupActor());
        List<Permission> readP = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(readP, testHelper.getSubGroupActor());
        List<Permission> systemP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(systemP);
    }

    public void testUpdatePermissionOnSystemFakeExecutors() throws Exception {
        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getFakeActor().getId(), testHelper.getFakeGroup().getId() };
        form.setIds(ids);

        UpdatePermissionsOnIdentifiableForm.Permissions permissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        permissions.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        permissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissions);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnSystemAction returns null forward", forward);
        assertEquals("UpdatePermissionOnSystemAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnSystemAction returns with errors", messages);
        assertEquals("UpdatePermissionOnSystemAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testUpdatePermissionOnSystem() throws Exception {
        Long[] names = { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(names);

        UpdatePermissionsOnIdentifiableForm.Permissions authorizedPermissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        authorizedPermissions.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        authorizedPermissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), authorizedPermissions);

        UpdatePermissionsOnIdentifiableForm.Permissions actorPermission = new UpdatePermissionsOnIdentifiableForm.Permissions();
        actorPermission.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getSubGroupActor().getId()), actorPermission);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnSystemAction returns null forward", forward);
        assertEquals("UpdatePermissionOnSystemAction returns wrong forward", SUCCESS, forward.getPath());
        assertNull("UpdatePermissionOnSystemAction returns with errors", getGlobalErrors());

        Collection<Permission> actual = testHelper.getAuthorizationService().getPermissions(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getSubGroupActor(), testHelper.getAASystem());
        List<Permission> expected = Lists.newArrayList(Permission.READ);
        WebArrayAssert.assertWeakEqualArrays("UpdatePermissionOnSystemAction does not set permission", expected, actual);

        Collection<Permission> actual2 = testHelper.getAuthorizationService().getPermissions(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getAuthorizedPerformerActor(), testHelper.getAASystem());
        List<Permission> expected2 = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        WebArrayAssert.assertWeakEqualArrays("UpdatePermissionOnSystemAction does not set permission", expected2, actual2);
    }

    public void testUpdatePermissionOnSystemUnauthorizedAction() throws Exception {
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(new ArrayList<Permission>());
        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId() };
        form.setIds(ids);

        UpdatePermissionsOnIdentifiableForm.Permissions permission = new UpdatePermissionsOnIdentifiableForm.Permissions();
        permission.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permission);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnSystemAction returns null forward", forward);
        assertEquals("UpdatePermissionOnSystemAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnSystemAction returns with errors", messages);
        assertEquals("UpdatePermissionOnSystemAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        Collection<Permission> actual2 = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), testHelper.getAASystem());
        List<Permission> expected2 = Lists.newArrayList();
        WebArrayAssert.assertWeakEqualArrays("UpdatePermissionOnSystemAction does not set permission", expected2, actual2);
    }

    public String getTestPrefix() {
        return testPrefix;
    }
}
