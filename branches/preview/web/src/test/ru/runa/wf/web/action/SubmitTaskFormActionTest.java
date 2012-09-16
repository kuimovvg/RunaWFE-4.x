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
package ru.runa.wf.web.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Permission;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.web.VariablesMultipartRequestHandler;
import ru.runa.wf.web.form.ProcessForm;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class SubmitTaskFormActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_tasks.do";

    private static final String FORWARD_SUCCESS = "/manage_tasks.do";

    private static final String FORWARD_SUBMIT = "/submit_task.do";

    private static final String FORWARD_TASKKIST = "/manage_tasks.do";

    private ActionMapping mapping;

    private SubmitTaskFormAction action;

    private ProcessForm form;

    @Override
    public String getTestPrefix() {
        return SubmitTaskFormActionTest.class.getName();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        action = new SubmitTaskFormAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(Resources.FORWARD_SUBMIT, FORWARD_SUBMIT);
        forwards.put(Resources.FORWARD_TASKKIST, FORWARD_TASKKIST);
        mapping = getActionMapping(forwards);
        form = new ProcessForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition("simpleProcess.par");

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, "simpleProcess");

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("variableStartState", "true");
        testHelper.getExecutionService().startProcessInstance(testHelper.getAuthorizedPerformerSubject(), "simpleProcess", variables);
    }

    @Override
    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition("simpleProcess");
        super.tearDown();
    }

    public void testSubmitTaskForm() throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());

        form.setId(tasks.get(0).getId());
        form.setTaskName(tasks.get(0).getName());
        form.setActorId(tasks.get(0).getTargetActor().getId());

        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        handler.addVariable("variableState1", "_none_value_");
        form.setMultipartRequestHandler(handler);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitTaskForm returns null forward", forward);
        assertEquals("testSubmitTaskForm returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("testSubmitTaskForm returns with errors", getGlobalErrors());

        tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(), testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state2", tasks.get(0).getName());
    }

    public void testSubmitTaskFormInvalidTaskId() throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
        form.setId(-1L);
        form.setActorId(tasks.get(0).getTargetActor().getId());
        form.setTaskName(tasks.get(0).getName());
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        handler.addVariable("variableState1", "_none_value_");
        form.setMultipartRequestHandler(handler);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitTaskForm returns null forward", forward);
        assertEquals("testSubmitTaskForm returns wrong forward", FORWARD_SUCCESS, forward.getPath());

        ActionMessages messages = getGlobalErrors();
        assertNotNull("testSubmitTaskFormInvalidTaskId returns with errors", messages);
        assertEquals("testSubmitTaskFormInvalidTaskId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_TASK_DOES_NOT_EXIST, ((ActionMessage) messages.get().next()).getKey());

        tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(), testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
    }

    public void testSubmitTaskFormInvalidTaskName() throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
        form.setId(tasks.get(0).getId());
        form.setActorId(tasks.get(0).getTargetActor().getId());
        form.setTaskName(tasks.get(0).getName() + "fake");
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        handler.addVariable("variableState1", "_none_value_");
        form.setMultipartRequestHandler(handler);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitTaskForm returns null forward", forward);
        assertEquals("testSubmitTaskForm returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testSubmitTaskFormInvalidTaskName returns with errors", messages);
        assertEquals("testSubmitTaskFormInvalidTaskName returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_TASK_DOES_NOT_EXIST, ((ActionMessage) messages.get().next()).getKey());

        tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(), testHelper.getTaskBatchPresentation());
        assertEquals("Process tasks count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
    }

    public void testSubmitTaskFormInvalidActorId() throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());

        form.setId(tasks.get(0).getId());
        form.setActorId(-1L);
        form.setTaskName(tasks.get(0).getName());

        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        handler.addVariable("variableState1", "_none_value_");
        form.setMultipartRequestHandler(handler);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitTaskForm returns null forward", forward);
        checkPath(form.getActorId(), form.getTaskName(), form.getId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_ACTOR_DOES_NOT_EXISTS, ((ActionMessage) messages.get().next()).getKey());

        tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(), testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
    }

    public void testSubmitTaskFormWithoutAllNeededVariables() throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());

        form.setId(tasks.get(0).getId());
        form.setActorId(tasks.get(0).getTargetActor().getId());
        form.setTaskName(tasks.get(0).getName());

        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        form.setMultipartRequestHandler(handler);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitTaskForm returns null forward", forward);
        checkPath(form.getActorId(), form.getTaskName(), form.getId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.MESSAGE_WEB_CLIENT_VALIDATION_ERROR, ((ActionMessage) messages.get().next()).getKey());

        tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(), testHelper.getTaskBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());
    }

    private void checkPath(Long actorId, String taskName, Long id, String forwardPath) {
        Set<String> expected = new HashSet<String>();
        expected.add(FORWARD_FAILURE);
        expected.add("actorId=" + actorId);
        expected.add("taskName=" + taskName);
        expected.add("id=" + id);
        // example: /manage_tasks.do?id=42&actorId=40&taskName=state1
        String[] params = forwardPath.split("[&?]");
        assertEquals(4, params.length);
        for (int i = 0; i < params.length; ++i) {
            assertTrue("testSubmitTaskForm returns wrong forward: " + params[i], expected.contains(params[i]));
        }
    }
}
