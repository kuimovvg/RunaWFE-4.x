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
public class AddChildrenToGroupActionTest extends StrutsTestCase {

    public String getTestPrefix() {
        return AddChildrenToGroupActionTest.class.getName();
    }

    private static final String FAILURE = "/manage_executor.do";

    private static final String SUCCESS = "/manage_executor.do";

    private static final String FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private String forwardSuffix;

    private AddMembersToGroupAction action;

    private ActionMapping mapping;

    private IdsForm form;

    public static Test suite() {
        return new TestSuite(AddChildrenToGroupActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new AddMembersToGroupAction();

        form = new IdsForm();
        form.reset(mapping, request);
        form.setId(testHelper.getSubGroup().getId());

        forwardSuffix = "?id=" + String.valueOf(form.getId());

        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);

        List<Permission> readRemovePermissions = Lists.newArrayList(Permission.READ);
        List<Permission> updatePermissions = Lists.newArrayList(Permission.READ, GroupPermission.ADD_TO_GROUP);
        testHelper.setPermissionsToAuthorizedPerformer(readRemovePermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readRemovePermissions, testHelper.getBaseGroup());
        testHelper.setPermissionsToAuthorizedPerformer(updatePermissions, testHelper.getSubGroup());
    }

    protected void tearDown() throws Exception {
        forwardSuffix = null;
        mapping = null;
        form = null;
        action = null;
        super.tearDown();
    }

    public void testAddEmptyExecutorsChildrenToGroup() throws Exception {
        Long[] ids = {};
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("AddChildrenToGroupAction returns null forward", forward);
        assertEquals("AddChildrenToGroupAction returns wrong forward", SUCCESS + forwardSuffix, forward.getPath());
        assertNull("AddChildrenToGroupAction returns without errors", getGlobalErrors());
    }

    public void testAddExecutorsByUnauthorized() throws Exception {
        List<Permission> noUpdatePermissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(noUpdatePermissions, testHelper.getSubGroup());

        Long[] ids = { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("AddChildrenToGroupAction returns null forward", forward);
        assertEquals("AddChildrenToGroupAction returns wrong forward", FAILURE + forwardSuffix, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("AddChildrenToGroupAction returns with errors", messages);
        assertEquals("AddChildrenToGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        boolean baseGroupActorWasAddedToSubGroup = testHelper.isExecutorInGroup(testHelper.getBaseGroupActor(), testHelper.getSubGroup());
        assertFalse("base group actor was added to sub group", baseGroupActorWasAddedToSubGroup);
        boolean baseGroupWasAddedToSubGroup = testHelper.isExecutorInGroup(testHelper.getBaseGroup(), testHelper.getSubGroup());
        assertFalse("base group actor was added to  sub group", baseGroupWasAddedToSubGroup);
    }

    public void testAddFakeChildrenToGroup() throws Exception {
        Long[] ids = { testHelper.getFakeActor().getId(), testHelper.getFakeGroup().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("AddChildrenToGroupAction returns null forward", forward);
        assertEquals("AddChildrenToGroupAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("AddChildrenToGroupAction returns with errors", messages);
        assertEquals("AddChildrenToGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testAddChildrenToGroup() throws Exception {
        Long[] ids = { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("AddChildrenToGroupAction returns null forward", forward);
        assertEquals("AddChildrenToGroupAction returns wrong forward", SUCCESS + forwardSuffix, forward.getPath());
        assertNull("AddChildrenToGroupAction returns with errors", getGlobalErrors());

        boolean baseGroupActorWasAddedToSubGroup = testHelper.isExecutorInGroup(testHelper.getBaseGroupActor(), testHelper.getSubGroup());
        assertTrue("base group actor was not added to sub group", baseGroupActorWasAddedToSubGroup);
        boolean baseGroupWasAddedToSubGroup = testHelper.isExecutorInGroup(testHelper.getBaseGroup(), testHelper.getSubGroup());
        assertTrue("base group actor was not added to  sub group", baseGroupWasAddedToSubGroup);
    }
}
