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

import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.junit.WebArrayAssert;

import com.google.common.collect.Lists;

/**
 */
public class UpdatePermissionsOnExecutorActionTest extends StrutsTestCase {
    private static String testPrefix = UpdatePermissionsOnExecutorActionTest.class.getName();

    private static final String FAILURE = "/manage_executor.do";

    private static final String SUCCESS = "/manage_executor.do";

    private static final String FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private UpdatePermissionsOnExecutorAction action;

    private ActionMapping mapping;

    private UpdatePermissionsOnIdentifiableForm form;

    public static Test suite() {
        return new TestSuite(UpdatePermissionsOnExecutorActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new UpdatePermissionsOnExecutorAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);

        form = new UpdatePermissionsOnIdentifiableForm();
        form.reset(mapping, request);

        List<Permission> updateP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformer(updateP, testHelper.getBaseGroupActor());
        List<Permission> readP = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(readP, testHelper.getSubGroupActor());
    }

    public void testUpdatePermissionOnExecutorFakeExecutor() throws Exception {
        Long[] ids = { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getFakeActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());

    }

    public void testUpdatePermissionOnExecutorFakeExecutors() throws Exception {
        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getFakeActor().getId(), testHelper.getFakeGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());

        UpdatePermissionsOnIdentifiableForm.Permissions permissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        permissions.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        permissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissions);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testUpdatePermissionOnExecutor() throws Exception {
        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());

        UpdatePermissionsOnIdentifiableForm.Permissions authorizedPermissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        authorizedPermissions.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        authorizedPermissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), authorizedPermissions);

        UpdatePermissionsOnIdentifiableForm.Permissions actorPermission = new UpdatePermissionsOnIdentifiableForm.Permissions();
        actorPermission.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getSubGroupActor().getId()), actorPermission);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("UpdatePermissionOnExecutor returns with errors", getGlobalErrors());

        Collection<Permission> actual = testHelper.getAuthorizationService().getPermissions(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getSubGroupActor(), testHelper.getBaseGroupActor());
        List<Permission> expected = Lists.newArrayList(Permission.READ);
        WebArrayAssert.assertWeakEqualArrays("UpdatePermissionOnExecutor does not set permission", expected, actual);

        actual = testHelper.getAuthorizationService().getPermissions(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getBaseGroupActor(), testHelper.getBaseGroupActor());
        expected = new ArrayList<Permission>();
        WebArrayAssert.assertWeakEqualArrays("UpdatePermissionOnExecutor does not set permission", expected, actual);
    }

    public void testUpdatePermissionOnExecutorSingleFakeExecutor() throws Exception {
        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getSubGroupActor().getId(), testHelper.getFakeActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());

        UpdatePermissionsOnIdentifiableForm.Permissions authorizedPermissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        authorizedPermissions.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        authorizedPermissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), authorizedPermissions);

        UpdatePermissionsOnIdentifiableForm.Permissions actorPermission = new UpdatePermissionsOnIdentifiableForm.Permissions();
        actorPermission.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getSubGroupActor().getId()), actorPermission);

        UpdatePermissionsOnIdentifiableForm.Permissions fakeActorPermission = new UpdatePermissionsOnIdentifiableForm.Permissions();
        fakeActorPermission.setPermission(String.valueOf(Permission.READ.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getFakeActor().getId()), fakeActorPermission);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testUpdatePermissionOnExecutorUnauthorized() throws Exception {
        testHelper.setPermissionsToAuthorizedPerformer(new ArrayList<Permission>(), testHelper.getBaseGroupActor());

        Long[] ids = { testHelper.getAuthorizedPerformerActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());

        UpdatePermissionsOnIdentifiableForm.Permissions permissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        permissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissions);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testUpdatePermissionOnExecutorAdministrative() throws Exception {
        testHelper.setPermissionsToAuthorizedPerformer(new GroupPermission().getAllPermissions(), testHelper.getAdministratorsGroup());

        Long[] ids = { testHelper.getAdministratorsGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());

        UpdatePermissionsOnIdentifiableForm.Permissions permissions = new UpdatePermissionsOnIdentifiableForm.Permissions();
        permissions.setPermission(String.valueOf(Permission.UPDATE_PERMISSIONS.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissions);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdatePermissionOnExecutor returns null forward", forward);
        assertEquals("UpdatePermissionOnExecutor returns wrong forward", FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
    }

    public String getTestPrefix() {
        return testPrefix;
    }
}
