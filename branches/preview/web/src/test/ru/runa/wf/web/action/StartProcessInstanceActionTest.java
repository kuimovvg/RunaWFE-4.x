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
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class StartProcessInstanceActionTest extends StrutsTestCase {

    private static final String VALID_PROCESS_WITHOUT_START_FORM_NAME = "validProcessWithoutStartForm";

    public String getTestPrefix() {
        return StartProcessInstanceActionTest.class.getName();
    }

    private static final String FORWARD_FAILURE = "/manage_process_definitions.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definitions.do";

    private static final String FORWARD_SUCCESS_DISPLAY_START_FORM = "/submit_start_process_instance.do";

    private ActionMapping mapping;

    private StartProcessInstanceAction action;

    private IdForm form;

    protected void setUp() throws Exception {
        super.setUp();
        action = new StartProcessInstanceAction();
    }

    public void testStartProcessInstanceWithStartForm() throws Exception {
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(ru.runa.wf.web.Resources.FORWARD_SUCCESS_DISPLAY_START_FORM, FORWARD_SUCCESS_DISPLAY_START_FORM);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new FileForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        form.setId(testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME).getNativeId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("StartProcessInstanceActionTest returns null forward", forward);
        assertEquals("StartProcessInstanceActionTest returns wrong forward", FORWARD_SUCCESS_DISPLAY_START_FORM + "?id="
                + String.valueOf(form.getId()), forward.getPath());
        assertNull("StartProcessInstanceActionTest returns with errors", getGlobalErrors());

        List<ProcessInstanceStub> stubs = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("StartProcessInstanceActionTest returns wrong number processes", 0, stubs.size());

        testHelper.undeployValidProcessDefinition();
    }

    public void testStartProcessInstanceWithoutSTARTPermission() throws Exception {
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new FileForm();
        form.reset(mapping, request);
        testHelper.deployValidProcessDefinition(VALID_PROCESS_WITHOUT_START_FORM_NAME + ".par");

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, VALID_PROCESS_WITHOUT_START_FORM_NAME);

        form.setId(testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAuthorizedPerformerSubject(),
                VALID_PROCESS_WITHOUT_START_FORM_NAME).getNativeId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("StartProcessInstanceActionTest returns null forward", forward);
        assertEquals("StartProcessInstanceActionTest returns wrong forward", FORWARD_FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        List<ProcessInstanceStub> stubs = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("StartProcessInstanceActionTest returns wrong number processes", 0, stubs.size());

        testHelper.undeployValidProcessDefinition(VALID_PROCESS_WITHOUT_START_FORM_NAME);
    }

    public void testStartProcessInstanceWithoutStartForm() throws Exception {
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new FileForm();
        form.reset(mapping, request);
        testHelper.deployValidProcessDefinition("validProcessWithoutStartForm.par");

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, VALID_PROCESS_WITHOUT_START_FORM_NAME);

        form.setId(testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAuthorizedPerformerSubject(),
                VALID_PROCESS_WITHOUT_START_FORM_NAME).getNativeId());
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("StartProcessInstanceActionTest returns null forward", forward);
        assertEquals("StartProcessInstanceActionTest returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("StartProcessInstanceActionTest returns with errors", getGlobalErrors());

        List<ProcessInstanceStub> stubs = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("StartProcessInstanceActionTest returns wrong number processes", 1, stubs.size());

        testHelper.undeployValidProcessDefinition(VALID_PROCESS_WITHOUT_START_FORM_NAME);
    }

}
