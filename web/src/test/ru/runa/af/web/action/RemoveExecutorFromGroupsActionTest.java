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
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;

import com.google.common.collect.Lists;

/**
 * Created on 19.10.2004
 * 
 */
public class RemoveExecutorFromGroupsActionTest extends StrutsTestCase {

    public String getTestPrefix() {
        return RemoveExecutorFromGroupsActionTest.class.getName();
    }

    private static final String FAILURE = "/manage_executor.do";

    private static final String SUCCESS = "/manage_executor.do";

    private static final String FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private RemoveExecutorFromGroupsAction action;

    private ActionMapping mapping;

    private IdsForm form;

    public static Test suite() {
        return new TestSuite(RemoveExecutorFromGroupsActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new RemoveExecutorFromGroupsAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);
        List<Permission> readRemovePermissions = Lists.newArrayList(Permission.READ);
        List<Permission> updatePermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);
        testHelper.setPermissionsToAuthorizedPerformer(readRemovePermissions, testHelper.getSubGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readRemovePermissions, testHelper.getSubGroup());
        testHelper.setPermissionsToAuthorizedPerformer(updatePermissions, testHelper.getBaseGroup());
    }

    public void testRemoveExecutorFromGroups() throws Exception {
        Long[] ids = { testHelper.getBaseGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorFromGroupAction returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("RemoveExecutorFromGroupAction returns with errors", getGlobalErrors());

        boolean baseGroupContainsSubGroup = testHelper.isExecutorInGroup(testHelper.getSubGroup(), testHelper.getBaseGroup());
        assertFalse("sub group actor was not removed from base group", baseGroupContainsSubGroup);
        boolean baseGroupContainsSubGroupActor = testHelper.isExecutorInGroup(testHelper.getSubGroupActor(), testHelper.getBaseGroup());
        assertFalse("sub group actor was not removed from base group", baseGroupContainsSubGroupActor);
    }

    public void testRemoveExecutorFromFromFakeGroup() throws Exception {
        Long[] ids = { testHelper.getFakeGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroupActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorFromGroupAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorFromGroupAction returns with errors", messages);
        assertEquals("RemoveExecutorFromGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_GROUP_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testRemoveExecutorsFromGroupFromEmptyGroupList() throws Exception {
        Long[] ids = {};
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorFromGroupAction returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("RemoveExecutorFromGroupAction returns without errors", getGlobalErrors());
    }

    public void testRemoveExecutorFromGroupsFromByUnauthorizedPerformer() throws Exception {
        Long[] ids = { testHelper.getSubGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroupActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorFromGroupAction returns wrong forward", FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorFromGroupAction returns with errors", messages);
        assertEquals("RemoveExecutorFromGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        boolean baseGroupContainsSubGroup = testHelper.isExecutorInGroup(testHelper.getSubGroup(), testHelper.getBaseGroup());
        assertTrue("sub group actor removed from base group", baseGroupContainsSubGroup);
        boolean baseGroupContainsSubGroupActor = testHelper.isExecutorInGroup(testHelper.getSubGroupActor(), testHelper.getBaseGroup());
        assertTrue("sub group actor removed from base group", baseGroupContainsSubGroupActor);
    }
}
