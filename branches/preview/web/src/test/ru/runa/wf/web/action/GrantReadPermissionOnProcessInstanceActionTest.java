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
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.junit.WebArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class GrantReadPermissionOnProcessInstanceActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_instance.do";

    private static final String FORWARD_SUCCESS = "/manage_process_instance.do";

    private static final String FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST = "/manage_process_instances.do";

    private ActionMapping mapping;

    private GrantReadPermissionOnProcessInstanceAction action;

    private IdsForm form;

    public String getTestPrefix() {
        return GrantReadPermissionOnProcessInstanceActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new GrantReadPermissionOnProcessInstanceAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_INSTANCE_DOES_NOT_EXIST, FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        testHelper.getExecutionService().startProcessInstance(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME);

        Collection<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformer(readUpdatePermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readUpdatePermissions, testHelper.getBaseGroup());

        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        Collection<Permission> instancePermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.READ,
                ProcessInstancePermission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(instancePermissions, instances.get(0));
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testGrantReadPermissionOnProcessInstanceValidId() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessInstanceValidId returns wrong forward",
                FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("testGrantReadPermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }

    public void testGrantReadPermissionOnProcessInstanceUnauthorized() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(ProcessInstancePermission.READ), instances.get(0));

        Collection<Permission> permissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessInstanceValidId returns wrong forward",
                FORWARD_FAILURE + "?id=" + String.valueOf(form.getId()), forward.getPath());

        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId set incorrect permission", permissions, newPermissions);
    }

    public void testGrantReadPermissionOnProcessInstanceInValidId() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());

        Collection<Permission> permissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));

        form.setId(-154L);
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessInstanceInValidId returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessInstanceInValidId returns wrong forward", FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST, forward
                .getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_INSTANCE_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId set incorrect permission", permissions, newPermissions);
    }

    public void testGrantReadPermissionOnProcessInstanceToBaseExecutors() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessInstanceValidId returns wrong forward",
                FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("testGrantReadPermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getBaseGroup(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
        newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(), testHelper.getBaseGroupActor(),
                instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }

    public void testGrantReadPermissionOnProcessInstanceValidIdByAdmin() throws Exception {
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAdminSubject(), session);

        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessInstanceValidId returns wrong forward",
                FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()), forward.getPath());
        assertNull("testGrantReadPermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessInstanceValidId doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }
}
