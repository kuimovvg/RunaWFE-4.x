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
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class GrantReadPermissionOnProcessDefinitionActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_definition.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definition.do";

    private static final String FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST = "/manage_process_definitions.do";

    private ActionMapping mapping;

    private GrantReadPermissionOnProcessDefinitionAction action;

    private IdsForm form;

    public String getTestPrefix() {
        return GrantReadPermissionOnProcessDefinitionActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new GrantReadPermissionOnProcessDefinitionAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST, FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        Collection<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        testHelper.setPermissionsToAuthorizedPerformer(readUpdatePermissions, testHelper.getBaseGroupActor());
        testHelper.setPermissionsToAuthorizedPerformer(readUpdatePermissions, testHelper.getBaseGroup());
    }

    protected void tearDown() throws Exception {
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testGrantReadPermissionOnProcessDefinitionValidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()),
                forward.getPath());
        assertNull("testGrantReadPermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }

    public void testGrantReadPermissionOnProcessDefinitionUnauthorized() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());
        testHelper.setPermissionsToAuthorizedPerformerOnDefinition(Lists.newArrayList(ProcessDefinitionPermission.READ), definitions.get(0));

        Collection<Permission> permissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessDefinition returns wrong forward", FORWARD_FAILURE + "?id=" + String.valueOf(form.getId()),
                forward.getPath());

        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition set incorrect permission", permissions, newPermissions);
    }

    public void testGrantReadPermissionOnProcessDefinitionToBaseExecutors() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getBaseGroup().getId(), testHelper.getBaseGroupActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()),
                forward.getPath());
        assertNull("testGrantReadPermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getBaseGroup(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
        newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(), testHelper.getBaseGroupActor(),
                definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }

    public void testGrantReadPermissionOnProcessDefinitionValidIdByAdmin() throws Exception {
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAdminSubject(), session);

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        form.setId(definitions.get(0).getNativeId());
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + String.valueOf(form.getId()),
                forward.getPath());
        assertNull("testGrantReadPermissionOnProcessDefinition returns with errors", getGlobalErrors());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition doesn't set correct permission",
                Lists.newArrayList(Permission.READ), newPermissions);
    }

    public void testGrantReadPermissionOnProcessDefinitionInValidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("process definitions count differs from expected", 1, definitions.size());

        Collection<Permission> permissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));

        form.setId(-117L);
        form.setIds(new Long[] { testHelper.getAuthorizedPerformerActor().getId() });

        ActionForward forward = action.execute(mapping, form, request, response);

        assertNotNull("testGrantReadPermissionOnProcessDefinition returns null forward", forward);
        assertEquals("testGrantReadPermissionOnProcessDefinition returns wrong forward", FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testCancelProcessInstanceInValidId returns with errors", messages);
        assertEquals("testCancelProcessInstanceInValidId returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        Collection<Permission> newPermissions = testHelper.getAuthorizationService().getPermissions(testHelper.getAdminSubject(),
                testHelper.getAuthorizedPerformerActor(), definitions.get(0));
        WebArrayAssert.assertWeakEqualArrays("testGrantReadPermissionOnProcessDefinition set incorrect permission", permissions, newPermissions);
    }
}
