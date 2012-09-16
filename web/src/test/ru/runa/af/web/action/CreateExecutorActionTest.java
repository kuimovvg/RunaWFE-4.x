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
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.web.form.CreateExecutorForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;

import com.google.common.collect.Lists;

/**
 * Created on 19.10.2004
 * 
 */
public class CreateExecutorActionTest extends StrutsTestCase {

    public String getTestPrefix() {
        return CreateExecutorActionTest.class.getName();
    }

    private static final String FORWARD_FAILURE = "/create_executor.do";

    private static final String FORWARD_SUCCESS = "/manage_executors.do";

    private static final String GROUP_TYPE_PARAM = "?executorType=group";

    private Executor createdExecutor;

    private CreateExecutorAction action;

    private ActionMapping mapping;

    private CreateExecutorForm form;

    public static Test suite() {
        return new TestSuite(CreateExecutorActionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new CreateExecutorAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new CreateExecutorForm();
        form.reset(mapping, request);
        List<Permission> createPermissions = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR, Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(createPermissions);
    }

    protected void tearDown() throws Exception {
        testHelper.removeExecutorIfExists(createdExecutor);
        super.tearDown();
    }

    public void testCreateActor() throws Exception {
        form.setExecutorType(CreateExecutorForm.TYPE_ACTOR);
        String name = getTestPrefix() + "name";
        String fullName = getTestPrefix() + "fullName";
        String desciption = getTestPrefix() + "desc";
        form.setNewName(name);
        form.setDescription(desciption);
        form.setFullName(fullName);
        ActionForward forward = action.execute(mapping, form, request, response);
        Actor actor = (Actor) testHelper.getExecutor(name);
        createdExecutor = actor;
        assertNotNull("CreateExecutorActionTest returns null forward", forward);
        assertEquals("CreateExecutorActionTest returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("CreateExecutorActionTest returns with errors", getGlobalErrors());

        assertEquals("return actor description deffers with given", actor.getDescription(), desciption);
        assertEquals("return actor full name deffers with given", actor.getFullName(), fullName);

    }

    public void testCreateGroup() throws Exception {
        form.setExecutorType(CreateExecutorForm.TYPE_GROUP);
        String name = getTestPrefix() + "name";
        String desciption = getTestPrefix() + "desc";
        form.setNewName(name);
        form.setDescription(desciption);
        ActionForward forward = action.execute(mapping, form, request, response);
        Group group = (Group) testHelper.getExecutor(name);
        createdExecutor = group;
        assertNotNull("CreateExecutorActionTest returns null forward", forward);
        assertEquals("CreateExecutorActionTest returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("CreateExecutorActionTest returns with errors", getGlobalErrors());

        assertEquals("return actor description deffers with given", group.getDescription(), desciption);
    }

    public void testCreateAlreadyExistExecutor() throws Exception {
        form.setExecutorType(CreateExecutorForm.TYPE_GROUP);
        String name = testHelper.getBaseGroupActor().getName();
        String desciption = getTestPrefix() + "desc";
        form.setNewName(name);
        form.setDescription(desciption);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("CreateExecutorActionTest returns null forward", forward);
        assertEquals("CreateExecutorActionTest returns wrong forward", FORWARD_FAILURE + GROUP_TYPE_PARAM, forward.getPath());
        Actor actor = (Actor) testHelper.getExecutor(name);
        ActionMessages messages = getGlobalErrors();
        assertNotNull("CreateExecutorActionTest returns with errors", messages);
        assertEquals("CreateExecutorActionTest returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_EXISTS, ((ActionMessage) messages.get().next())
                .getKey());

        assertNotSame("return actor description the same as with given", desciption, actor.getDescription());
    }

    public void testCreateExecutorWithoutCREATE_EXECUTORPermission() throws Exception {
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(readPermissions);
        form.setExecutorType(CreateExecutorForm.TYPE_GROUP);
        String name = "test_name_0" + getTestPrefix();//testHelper.getBaseGroupActor().getName();
        String desciption = getTestPrefix() + "desc";
        form.setNewName(name);
        form.setDescription(desciption);
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("CreateExecutorActionTest returns null forward", forward);
        assertEquals("CreateExecutorActionTest returns wrong forward", FORWARD_FAILURE + GROUP_TYPE_PARAM, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("CreateExecutorActionTest returns with errors", messages);
        assertEquals("CreateExecutorActionTest returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());
        try {
            testHelper.getExecutor(name);
            fail("executor was created");
        } catch (ExecutorOutOfDateException e) {
        }
    }
}
