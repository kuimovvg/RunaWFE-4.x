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
import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
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
public class UpdatePermissionOnProcessInstanceActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_instance.do";

    private static final String FORWARD_SUCCESS = "/manage_process_instance.do";

    private static final String FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST = "/manage_process_instances.do";

    private ActionMapping mapping;

    private UpdatePermissionOnProcessInstanceAction action;

    private UpdatePermissionsOnIdentifiableForm form;

    public String getTestPrefix() {
        return UpdatePermissionOnProcessInstanceActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new UpdatePermissionOnProcessInstanceAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_INSTANCE_DOES_NOT_EXIST, FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new UpdatePermissionsOnIdentifiableForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> ProcessDefinitionPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(ProcessDefinitionPermissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        testHelper.getExecutionService().startProcessInstance(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME);

        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());
        assertEquals("process instances count differs from expected", 1, instances.size());
        Collection<Permission> processInstancePermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.READ,
                ProcessInstancePermission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnProcessInstance(processInstancePermissions, instances.get(0));

        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getBaseGroup());
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testUpdatePermissionOnProcessInstanceValidId() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.UPDATE_PERMISSIONS);

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });
        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessInstanceValidId returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceValidId doesn't updated permissions", newPermissions,
                actualPermissions);
    }

    public void testUpdatePermissionOnProcessInstanceValidIdToBaseActorAndGroup() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.UPDATE_PERMISSIONS);
        Collection<Permission> newPermissions2 = Lists.newArrayList(ProcessInstancePermission.READ);

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() });

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getBaseGroupActor().getId()), permissionsToPut);

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut2 = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions2) {
            permissionsToPut2.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getBaseGroup().getId()), permissionsToPut2);

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessInstanceValidId returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getBaseGroupActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceValidId doesn't updated permissions", newPermissions,
                actualPermissions);
        actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(), testHelper.getBaseGroup(),
                instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceValidId doesn't updated permissions", newPermissions2,
                actualPermissions);
    }

    public void testUpdatePermissionOnProcessInstanceInValidId() throws Exception {
        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());

        Collection<Permission> permissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.UPDATE_PERMISSIONS);

        form.setId(-777L);
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });
        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessInstanceInValidId returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessInstanceInValidId returns wrong forward", FORWARD_FAILURE_INSTANCE_DOES_NOT_EXIST, forward
                .getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_INSTANCE_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert
                .assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceInValidId updated permissions. ERROR", permissions, actualPermissions);
    }

    public void testUpdatePermissionOnProcessInstanceValidIdByAdmin() throws Exception {
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAdminSubject(), session);

        List<ProcessInstanceStub> instances = testHelper.getExecutionService().getProcessInstanceStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessInstanceBatchPresentation());

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE, ProcessInstancePermission.UPDATE_PERMISSIONS);

        form.setId(instances.get(0).getId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getProcessDefinitionAdministratorsGroup().getId() });

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        Collection<Permission> adminPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getProcessDefinitionAdministratorsGroup(), instances.get(0));

        UpdatePermissionsOnIdentifiableForm.Permissions adminPermissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : adminPermissions) {
            adminPermissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getProcessDefinitionAdministratorsGroup().getId()), adminPermissionsToPut);

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessInstanceValidId returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessInstanceValidId returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessInstanceValidId returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceValidId doesn't updated permissions", newPermissions,
                actualPermissions);
        Collection<Permission> actualAdminPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getProcessDefinitionAdministratorsGroup(), instances.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessInstanceValidId doesn't updated permissions", adminPermissions,
                actualAdminPermissions);
    }
}
