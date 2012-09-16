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
import ru.runa.common.web.form.IdsForm;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class UndeployProcessDefinitionActionTest extends StrutsTestCase {
    public String getTestPrefix() {
        return UndeployProcessDefinitionActionTest.class.getName();
    }

    private static final String FORWARD_FAILURE = "/manage_process_definitions.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definitions.do";

    private ActionMapping mapping;

    private UndeployProcessDefinitionAction action;

    private IdsForm form;

    protected void setUp() throws Exception {
        super.setUp();
        action = new UndeployProcessDefinitionAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new IdsForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.UNDEPLOY_DEFINITION);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testUndeployProcessDefinitionInvalidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());

        form.setIds(new Long[] { -1L });
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testUndeployProcessDefinitionInvalidId returns null forward", forward);
        assertEquals("testUndeployProcessDefinitionInvalidId returns wrong forward", FORWARD_FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST, ((ActionMessage) messages.get().next())
                .getKey());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());

        testHelper.undeployValidProcessDefinition();
    }

    public void testUndeployProcessDefinitionValidId() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());

        form.setIds(new Long[] { testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME).getNativeId() });
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testUndeployProcessDefinition returns null forward", forward);
        assertEquals("testUndeployProcessDefinition returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("testUndeployProcessDefinition returns with errors", getGlobalErrors());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 0, definitions.size());
    }

    public void testUndeployProcessDefinitionWithoutUNDEPLOYPermission() throws Exception {
        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        form.setIds(new Long[] { testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAuthorizedPerformerSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME).getNativeId() });
        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testUndeployProcessDefinitionInvalidId returns null forward", forward);
        assertEquals("testUndeployProcessDefinitionInvalidId returns wrong forward", FORWARD_FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());

        testHelper.undeployValidProcessDefinition();
    }
}
