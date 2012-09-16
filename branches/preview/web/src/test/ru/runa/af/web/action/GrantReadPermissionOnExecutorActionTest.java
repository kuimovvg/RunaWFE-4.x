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
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.junit.WebArrayAssert;

import com.google.common.collect.Lists;

/**
 * Created on 19.10.2004
 * 
 */
public class GrantReadPermissionOnExecutorActionTest extends StrutsTestCase {

    public String getTestPrefix() {
        return GrantReadPermissionOnExecutorActionTest.class.getName();
    }

    private static final String FORWARD_FAILURE = "/manage_executor.do";

    private static final String FORWARD_SUCCESS = "/manage_executor.do";

    private static final String FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private GrantReadPermissionOnExecutorAction action;

    private ActionMapping mapping;

    private IdsForm form;

    public static Test suite() {
        return new TestSuite(GrantReadPermissionOnExecutorActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new GrantReadPermissionOnExecutorAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);
        List<Permission> updatePermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformer(updatePermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getSubGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getSubGroup());
    }

    public void testGrantReadPermissionOnExecutor() throws Exception {
        Long[] ids = { testHelper.getSubGroup().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("GrantReadPermissionOnExecutorAction returns null forward", forward);
        assertEquals("GrantReadPermissionOnExecutorAction returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward
                .getPath());
        assertNull("GrantReadPermissionOnExecutorAction returns with errors", getGlobalErrors());

        Collection<Permission> permissions = testHelper.getOwnPermissions(testHelper.getSubGroupActor(), testHelper.getBaseGroupActor());
        assertEquals("SubGroupActor does not have READ permission on SubGroupActor", Permission.READ, WebArrayAssert.getFirst(permissions));
        permissions = testHelper.getOwnPermissions(testHelper.getSubGroup(), testHelper.getBaseGroupActor());
        assertEquals("SubGroup does not have READ permission on SubGroupActor", Permission.READ, WebArrayAssert.getFirst(permissions));
    }

    public void testGrantReadPermissionOnFakeExecutor() throws Exception {
        Long[] ids = { testHelper.getSubGroup().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getFakeActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("GrantReadPermissionOnExecutorAction returns null forward", forward);
        assertEquals("GrantReadPermissionOnExecutorAction returns wrong forward", FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("GrantReadPermissionOnExecutorAction returns with errors", messages);
        assertEquals("GrantReadPermissionOnExecutorAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testGrantReadPermissionOnExecutorToEmptyExecutorList() throws Exception {
        Long[] ids = {};
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("GrantReadPermissionOnExecutorAction returns null forward", forward);
        assertEquals("GrantReadPermissionOnExecutorAction returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward
                .getPath());
        assertNull("GrantReadPermissionOnExecutorAction returns with errors", getGlobalErrors());
    }

    public void testRemoveExecutorFromGroupsFromByUnauthorizedPerformer() throws Exception {
        Long[] ids = { testHelper.getSubGroup().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroupActor().getId());
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getBaseGroupActor());

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("GrantReadPermissionOnExecutorAction returns null forward", forward);
        assertEquals("GrantReadPermissionOnExecutorAction returns wrong forward", FORWARD_FAILURE + "?id=" + String.valueOf(form.getId()), forward
                .getPath());

        ActionMessages messages = getGlobalErrors();
        assertNotNull("GrantReadPermissionOnExecutorAction returns with errors", messages);
        assertEquals("GrantReadPermissionOnExecutorAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        Collection<Permission> permissions = testHelper.getOwnPermissions(testHelper.getSubGroupActor(), testHelper.getBaseGroupActor());
        assertEquals("SubGroupActor does not have READ permission on SubGroupActor", 0, permissions.size());
        permissions = testHelper.getOwnPermissions(testHelper.getSubGroup(), testHelper.getBaseGroupActor());
        assertEquals("SubGroup does not have READ permission on SubGroupActor", 0, permissions.size());
    }
}
