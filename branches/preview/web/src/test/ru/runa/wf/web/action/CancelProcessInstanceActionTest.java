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
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class CancelProcessInstanceActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_instance.do";

    private static final String FORWARD_SUCCESS = "/manage_process_instance.do";

    private static final String FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST = "/manage_process_instances.do";

    private ActionMapping mapping;

    private CancelProcessInstanceAction action;

    private IdForm form;

    public String getTestPrefix() {
        return CancelProcessInstanceActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new CancelProcessInstanceAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_INSTANCE_DOES_NOT_EXIST, FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        testHelper.getExecutionService().startProcessInstance(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME);
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testCancelProcessInstanceValidId() throws Exception {
        List<ProcessInstanceStub> processes = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());

        Collection<Permission> permissions = Lists.newArrayList(ProcessInstancePermission.READ, ProcessInstancePermission.CANCEL_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(permissions, processes.get(0));

        form.setId(processes.get(0).getId());

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testCancelProcessInstanceValidId returns null forward", forward);
        assertEquals("testCancelProcessInstanceValidId returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward
                .getPath());
        assertNull("testCancelProcessInstanceValidId returns with errors", getGlobalErrors());

        processes = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());
        assertTrue("Process instance doesn't cancelled", processes.get(0).isEnded());
    }

    public void testCancelProcessInstanceInValidId() throws Exception {
        List<ProcessInstanceStub> processes = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());

        Collection<Permission> permissions = Lists.newArrayList(ProcessInstancePermission.READ, ProcessInstancePermission.CANCEL_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(permissions, processes.get(0));

        form.setId(-1044L);

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testCancelProcessInstanceInValidId returns null forward", forward);
        assertEquals("testCancelProcessInstanceInValidId returns wrong forward", FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_INSTANCE_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        processes = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());
        assertFalse("Process instance was cancelled", processes.get(0).isEnded());
    }

    public void testCancelProcessInstanceWithoutCANCELPermission() throws Exception {
        List<ProcessInstanceStub> processes = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());

        Collection<Permission> permissions = Lists.newArrayList(ProcessInstancePermission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(permissions, processes.get(0));

        form.setId(processes.get(0).getId());

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testCancelProcessInstanceInValidId returns null forward", forward);
        assertEquals("testCancelProcessInstanceInValidId returns wrong forward", FORWARD_FAILURE + "?id=" + String.valueOf(form.getId()), forward
                .getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        processes = testHelper.getExecutionService().getProcessInstanceStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessInstanceBatchPresentation());
        assertEquals("Process instances count differs from expected", 1, processes.size());
        assertFalse("Process instance was cancelled", processes.get(0).isEnded());
    }
}
