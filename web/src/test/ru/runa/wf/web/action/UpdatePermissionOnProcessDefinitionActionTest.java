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
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class UpdatePermissionOnProcessDefinitionActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_definition.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definition.do";

    private static final String FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST = "/manage_process_definitions.do";

    private ActionMapping mapping;

    private UpdatePermissionOnProcessDefinitionAction action;

    private UpdatePermissionsOnIdentifiableForm form;

    public String getTestPrefix() {
        return UpdatePermissionOnProcessDefinitionActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new UpdatePermissionOnProcessDefinitionAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST, FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new UpdatePermissionsOnIdentifiableForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readPermissions, testHelper.getBaseGroup());
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testUpdatePermissionOnProcessDefinitionValidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessDefinitionPermission.REDEPLOY_DEFINITION);

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", newPermissions, actualPermissions);
    }

    public void testUpdatePermissionOnProcessDefinitionValidIdByAdmin() throws Exception {
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAdminSubject(), session);

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        Collection<Permission> newPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessDefinitionPermission.REDEPLOY_DEFINITION);

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        Collection<Permission> adminPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getProcessDefinitionAdministratorsGroup(), definitions.get(0));

        UpdatePermissionsOnIdentifiableForm.Permissions adminPermissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : adminPermissions) {
            adminPermissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getProcessDefinitionAdministratorsGroup().getId()), adminPermissionsToPut);

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId(), testHelper.getProcessDefinitionAdministratorsGroup().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", newPermissions, actualPermissions);
        actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getProcessDefinitionAdministratorsGroup(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", adminPermissions, actualPermissions);

    }

    public void testUpdatePermissionOnProcessDefinitionInValidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        Collection<Permission> oldPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));

        List<Permission> newPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessDefinitionPermission.REDEPLOY_DEFINITION);

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getAuthorizedPerformerActor().getId()), permissionsToPut);

        form.setId(-111L);
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessDefinitionInValidId returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessDefinitionInValidId returns wrong forward", FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST, forward
                .getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", oldPermissions, actualPermissions);
    }

    public void testUpdatePermissionOnProcessDefinitionValidIdToBaseExecutors() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        List<Permission> newPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessDefinitionPermission.REDEPLOY_DEFINITION);

        UpdatePermissionsOnIdentifiableForm.Permissions permissionsToPut = new UpdatePermissionsOnIdentifiableForm.Permissions();
        for (Permission permission : newPermissions) {
            permissionsToPut.setPermission(String.valueOf(permission.getMask()), UpdatePermissionsOnIdentifiableForm.ON_VALUE);
        }
        form.setExecutor(String.valueOf(testHelper.getBaseGroup().getId()), permissionsToPut);
        form.setExecutor(String.valueOf(testHelper.getBaseGroupActor().getId()), permissionsToPut);

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getBaseGroupActor().getId(), testHelper.getBaseGroup().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testUpdatePermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testUpdatePermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + form.getId(), forward.getPath());
        assertNull("testUpdatePermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(),
                testHelper.getBaseGroupActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", newPermissions, actualPermissions);
        actualPermissions = testHelper.getAuthorizationService().getOwnPermissions(testHelper.getAdminSubject(), testHelper.getBaseGroup(),
                definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testUpdatePermissionOnProcessDefinition updated permissions. ERROR", newPermissions, actualPermissions);
    }
}
