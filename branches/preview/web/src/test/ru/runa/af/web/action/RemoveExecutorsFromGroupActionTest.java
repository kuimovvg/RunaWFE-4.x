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

import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;

import com.google.common.collect.Lists;

/**
 */
public class RemoveExecutorsFromGroupActionTest extends StrutsTestCase {
    private static String testPrefix = RemoveExecutorsFromGroupActionTest.class.getName();

    private static final String FAILURE = "/manage_executor.do";

    private static final String SUCCESS = "/manage_executor.do";

    private static final String FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private RemoveExecutorsFromGroupAction action;

    private ActionMapping mapping;

    private IdsForm form;

    public static Test suite() {
        return new TestSuite(RemoveExecutorsFromGroupActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new RemoveExecutorsFromGroupAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);
        List<Permission> updateP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        testHelper.setPermissionsToAuthorizedPerformer(updateP, testHelper.getSubGroupActor());

        List<Permission> removeFromGroupP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE, GroupPermission.REMOVE_FROM_GROUP);
        testHelper.setPermissionsToAuthorizedPerformer(removeFromGroupP, testHelper.getSubGroup());
    }

    public void testRemoveExecutorsFromGroupActionFakeGroup() throws Exception {
        form.setId(testHelper.getFakeActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorsFromGroupAction returns with errors", messages);
        assertEquals("RemoveExecutorsFromGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_GROUP_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testRemoveExecutorsFromGroupActionFakeExecutors() throws Exception {
        Long[] ids = { testHelper.getFakeActor().getId(), testHelper.getFakeGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorsFromGroupAction returns with errors", messages);
        assertEquals("RemoveExecutorsFromGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testRemoveExecutorsFromGroupActionAuthorizationError() throws Exception {
        Long[] ids = { testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getBaseGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorsFromGroupAction returns with errors", messages);
        assertEquals("RemoveExecutorsFromGroupAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
        assertTrue("RemoveExecutorsFromGroupAction removed executor from group", testHelper.getExecutorService().isExecutorInGroup(
                testHelper.getAdminSubject(), testHelper.getSubGroupActor(), testHelper.getBaseGroup()));
    }

    public void testRemoveExecutorsFromGroupActionEmptyExecutors() throws Exception {
        Long[] ids = {};
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("RemoveExecutorsFromGroupAction returns with errors", getGlobalErrors());
    }

    public void testRemoveExecutorsFromGroupAction() throws Exception {
        Long[] ids = { testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("RemoveExecutorsFromGroupAction returns with errors", getGlobalErrors());
        assertFalse("RemoveExecutorsFromGroupAction does not remove executor from group", testHelper.getExecutorService().isExecutorInGroup(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getSubGroupActor(), testHelper.getSubGroup()));
    }

    public void testRemoveExecutorsFromGroupActionAdministrators() throws Exception {
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAdminSubject(), request.getSession());
        Executor[] executors = { testHelper.getAdministratorsGroup() };
        testHelper.getExecutorService().addExecutorsToGroup(testHelper.getAdminSubject(), Lists.newArrayList(executors), testHelper.getSubGroup());
        Long[] ids = { testHelper.getAdministratorsGroup().getId() };
        form.setIds(ids);
        form.setId(testHelper.getSubGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorsFromGroupAction returns null forward", forward);
        assertEquals("RemoveExecutorsFromGroupAction returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("RemoveExecutorsFromGroupAction returns with errors", getGlobalErrors());
        assertFalse("RemoveExecutorsFromGroupAction does not remove executor from group", testHelper.getExecutorService().isExecutorInGroup(
                testHelper.getAdminSubject(), testHelper.getAdministratorsGroup(), testHelper.getSubGroup()));
    }

    public String getTestPrefix() {
        return testPrefix;
    }
}
