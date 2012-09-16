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
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Permission;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.web.VariablesMultipartRequestHandler;
import ru.runa.wf.web.form.CommonProcessForm;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class SubmitStartProcessFormActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/submit_start_process_instance.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definitions.do";

    private static final String FORWARD_SUBMIT = "/submit_task.do";

    private static final String FORWARD_TASKKIST = "/manage_tasks.do";

    private ActionMapping mapping;

    private SubmitStartProcessFormAction action;

    private IdForm form;

    public String getTestPrefix() {
        return SubmitStartProcessFormActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new SubmitStartProcessFormAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(Resources.FORWARD_SUBMIT, FORWARD_SUBMIT);
        forwards.put(Resources.FORWARD_TASKKIST, FORWARD_TASKKIST);
        mapping = getActionMapping(forwards);
        form = new CommonProcessForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition("simpleProcess.par");

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, "simpleProcess");
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition("simpleProcess");
        super.tearDown();
    }

    public void testSubmitStartProcessForm() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        handler.addVariable("variableStartState", "start_reason");
        form.setMultipartRequestHandler(handler);

        form.getMultipartRequestHandler();

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitStartProcessForm returns null forward", forward);
        assertEquals("testSubmitStartProcessForm returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("testSubmitStartProcessForm returns with errors", getGlobalErrors());

        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Tasks count differs from expected", 1, tasks.size());
        assertEquals("Process instance state differs from expected", "state1", tasks.get(0).getName());

        Object varValue = testHelper.getExecutionService().getVariable(testHelper.getAuthorizedPerformerSubject(), tasks.get(0).getId(),
                "variableStartState");
        assertEquals("Variable value differs from expected", "start_reason", varValue);
    }

    public void testSubmitStartProcessFormInvalidProcessId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, definitions.size());

        form.setId(-77788L);
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        form.setMultipartRequestHandler(handler);

        form.getMultipartRequestHandler();

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitStartProcessForm returns null forward", forward);
        assertEquals("testSubmitStartProcessForm returns wrong forward", FORWARD_FAILURE + "?id=" + form.getId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Tasks count differs from expected", 0, tasks.size());
    }

    public void testSubmitStartProcessFormWithoutSTARTPermission() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, "simpleProcess");

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        form.setMultipartRequestHandler(handler);

        form.getMultipartRequestHandler();

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitStartProcessForm returns null forward", forward);
        assertEquals("testSubmitStartProcessForm returns wrong forward", FORWARD_FAILURE + "?id=" + form.getId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Tasks count differs from expected", 0, tasks.size());
    }

    public void testSubmitStartProcessFormWothoutStartVariables() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        VariablesMultipartRequestHandler handler = new VariablesMultipartRequestHandler();
        form.setMultipartRequestHandler(handler);

        form.getMultipartRequestHandler();

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSubmitStartProcessForm returns null forward", forward);
        assertEquals("testSubmitStartProcessForm returns wrong forward", FORWARD_FAILURE + "?id=" + form.getId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.MESSAGE_WEB_CLIENT_VALIDATION_ERROR, ((ActionMessage) messages.get().next()).getKey());

        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getTaskBatchPresentation());
        assertEquals("Tasks count differs from expected", 0, tasks.size());
    }
}
