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

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.service.WebServiceTestHelper;
import ru.runa.af.web.form.UpdatePasswordForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;

import com.google.common.collect.Lists;

/**
 */
public class SetPasswordActionTest extends StrutsTestCase {
    private static String testPrefix = SetPasswordActionTest.class.getName();

    private static final String FAILURE = "/manage_executor.do";

    private static final String SUCCESS = "/manage_executor.do";

    private static final String FAILURE_EXECUTOR_DOES_NOT_EXIST = "/manage_executors.do";

    private static final String PASSWORD = testPrefix + "passwd";

    private UpdatePasswordAction action;

    private ActionMapping mapping;

    private UpdatePasswordForm form;

    private Actor actor;

    public static Test suite() {
        return new TestSuite(SetPasswordActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new UpdatePasswordAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        forwards.put(ru.runa.af.web.Resources.FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST, FAILURE_EXECUTOR_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);

        form = new UpdatePasswordForm();
        form.reset(mapping, request);

        Map<String, Executor> executorsMap = testHelper.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(WebServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        List<Permission> updateP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        testHelper.setPermissionsToAuthorizedPerformer(updateP, actor);
    }

    protected void tearDown() throws Exception {
        actor = null;
        super.tearDown();
    }

    public void testSetPasswordActionFakeExecutor() throws Exception {
        form.setId(testHelper.getFakeActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("SetPasswordAction returns null forward", forward);
        assertEquals("SetPasswordAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("SetPasswordAction returns with errors", messages);
        assertEquals("SetPasswordAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_ACTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testSetPasswordGroup() throws Exception {
        form.setId(testHelper.getBaseGroup().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("SetPasswordAction returns null forward", forward);
        assertEquals("SetPasswordAction returns wrong forward", FAILURE_EXECUTOR_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("SetPasswordAction returns with errors", messages);
        assertEquals("SetPasswordAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_ACTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testSetPasswordActionAuthorizationError() throws Exception {
        form.setId(testHelper.getBaseGroupActor().getId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdateExecutor returns null forward", forward);
        assertEquals("UpdateExecutor returns wrong forward", FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("UpdatePermissionOnExecutor returns with errors", messages);
        assertEquals("UpdatePermissionOnExecutor returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
    }

    public void testSetPasswordAction() throws Exception {
        form.setId(actor.getId());
        form.setPassword(PASSWORD);
        form.setPasswordConfirm(PASSWORD);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("UpdateExecutor returns null forward", forward);
        assertEquals("UpdateExecutor returns wrong forward", SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("UpdateExecutor returns with errors", getGlobalErrors());
    }

    public String getTestPrefix() {
        return testPrefix;
    }
}
