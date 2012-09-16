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

import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;

import com.google.common.collect.Lists;

/**
 */
public class RemoveExecutorsActionTest extends StrutsTestCase {
    private static String testPrefix = RemoveExecutorsActionTest.class.getName();

    private static final String FAILURE = "/manage_executors.do";

    private static final String SUCCESS = "/manage_executors.do";

    private RemoveExecutorsAction action;

    private ActionMapping mapping;

    private IdsForm form;

    public static Test suite() {
        return new TestSuite(RemoveExecutorsActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new RemoveExecutorsAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FAILURE);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);
        List<Permission> updateP = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        testHelper.setPermissionsToAuthorizedPerformer(updateP, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(updateP, testHelper.getBaseGroup());
    }

    public void testRemoveExecutors() throws Exception {
        testHelper.removeCreatedExecutor(testHelper.getBaseGroupActor());
        testHelper.removeCreatedExecutor(testHelper.getBaseGroup());

        Long[] ids = { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorAction returns null forward", forward);
        assertEquals("RemoveExecutorAction returns wrong forward", SUCCESS, forward.getPath());
        assertNull("RemoveExecutorAction returns with errors", getGlobalErrors());
        try {
            testHelper.getBaseGroup();
            fail("RemoveExecutorAction does not remove executor");
        } catch (ExecutorOutOfDateException e) {
        }
        try {
            testHelper.getBaseGroupActor();
            fail("RemoveExecutorAction does not remove executor");
        } catch (ExecutorOutOfDateException e) {
        }
    }

    public String getTestPrefix() {
        return testPrefix;
    }

    public void testRemoveFakeExecutors() throws Exception {
        Long[] ids = { testHelper.getFakeActor().getId(), testHelper.getFakeGroup().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorAction returns null forward", forward);
        assertEquals("RemoveExecutorAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorAction returns with errors", messages);
        assertEquals("RemoveExecutorAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());
    }

    public void testRemoveEmptyExecutors() throws Exception {
        Long[] ids = {};
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorAction returns null forward", forward);
        assertEquals("RemoveExecutorAction returns wrong forward", SUCCESS, forward.getPath());
        assertNull("RemoveExecutorAction returns without errors", getGlobalErrors());
    }

    public void testRemoveExecutorsUnauthorized() throws Exception {
        Long[] ids = { testHelper.getBaseGroupActor().getId(), testHelper.getSubGroupActor().getId() };
        form.setIds(ids);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("RemoveExecutorAction returns null forward", forward);
        assertEquals("RemoveExecutorAction returns wrong forward", FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("RemoveExecutorAction returns with errors", messages);
        assertEquals("RemoveExecutorAction returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        try {
            testHelper.getSubGroupActor();
        } catch (ExecutorOutOfDateException e) {
            fail("RemoveExecutorAction removed executor");
        }
        try {
            testHelper.getBaseGroupActor();
        } catch (ExecutorOutOfDateException e) {
            fail("RemoveExecutorAction removed executor");
        }
    }
}
