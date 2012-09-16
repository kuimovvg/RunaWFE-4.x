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
import java.util.Map;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.Permission;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class DeployProcessDefinitionActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/deploy_process_definition.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definitions.do";

    private ActionMapping mapping;

    private DeployProcessDefinitionAction action;

    private FileForm form;

    private HttpServletRequestWrapper requestParamWrapper;

    public String getTestPrefix() {
        return DeployProcessDefinitionActionTest.class.getName();
    }

    protected void setUp() throws Exception {
        super.setUp();
        action = new DeployProcessDefinitionAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        mapping = getActionMapping(forwards);
        form = new FileForm();
        form.reset(mapping, request);

        Collection<Permission> permissions = Lists.newArrayList(Permission.READ, WorkflowSystemPermission.DEPLOY_DEFINITION);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(permissions);

        requestParamWrapper = new HttpServletRequestWrapper(request) {
            public java.lang.String getParameter(java.lang.String name) {
                if ("type".equals(name)) {
                    return "script";
                } else {
                    return super.getParameter(name);
                }
            }
        };
    }

    protected void tearDown() throws Exception {
        mapping = null;
        action = null;
        form = null;
        super.tearDown();
    }

    public void testDeployProcessDefinitionValidProcess() throws Exception {
        try {
            testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                    WebWfServiceTestHelper.VALID_PROCESS_NAME);
            assertFalse("testDeployProcessDefinitionValidProcess, definition exists", true);
        } catch (ProcessDefinitionDoesNotExistException e) {
        }

        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);
        assertNotNull("DeployProcessDefinitionAction returns null forward", forward);
        assertEquals("DeployProcessDefinitionAction returns wrong forward", FORWARD_SUCCESS, forward.getPath());
        assertNull("DeployProcessDefinitionAction returns with errors", getGlobalErrors());

        ProcessDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME);
        assertNotNull("DeployProcessDefinitionAction, definition after action is null", definition);

        testHelper.undeployValidProcessDefinition();

    }

    public void testDeployProcessDefinitionValidProcessWhichAlreadyExists() throws Exception {
        testHelper.deployValidProcessDefinition();

        ProcessDefinition expectedDefinition = testHelper.getDefinitionService().getLatestProcessDefinitionStub(
                testHelper.getAdminSubject(), WebWfServiceTestHelper.VALID_PROCESS_NAME);
        assertNotNull("DeployProcessDefinitionAction, definition after action is null", expectedDefinition);

        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);
        assertNotNull("testDeployProcessDefinitionValidProcessWhichAlreadyExists returns null forward", forward);
        assertEquals("testDeployProcessDefinitionValidProcessWhichAlreadyExists returns wrong forward", FORWARD_FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWhichAlreadyExists returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWhichAlreadyExists returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", "definition.already.exists.error", ((ActionMessage) messages.get().next()).getKey());

        ProcessDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                WebWfServiceTestHelper.VALID_PROCESS_NAME);
        assertEquals("testDeployProcessDefinitionValidProcessWhichAlreadyExists, definition was redeployed", expectedDefinition, definition);

        testHelper.undeployValidProcessDefinition();
    }

    public void testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission() throws Exception {
        try {
            testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                    WebWfServiceTestHelper.VALID_PROCESS_NAME);
            assertFalse("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission, definition exists", true);
        } catch (ProcessDefinitionDoesNotExistException e) {
            // expected
        }

        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnSystem(permissions);

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns null forward", forward);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns wrong forward", FORWARD_FAILURE, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with errors", messages);
        assertEquals("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        try {
            testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                    WebWfServiceTestHelper.VALID_PROCESS_NAME);
            assertFalse("testDeployProcessDefinitionValidProcessWithoutDEPLOYPermission, definition was deployed without permission", true);
        } catch (ProcessDefinitionDoesNotExistException e) {
            // expected
        }
    }

    public void testDeployProcessDefinitionInValidProcess() throws Exception {
        testDeploymentFailure(WebWfServiceTestHelper.INVALID_FILE_NAME, Messages.DEFINITION_FILE_FORMAT_ERROR);
        testDeploymentFailure("inValidProcess-no-processdefinition.xml.par", Messages.DEFINITION_FILE_FORMAT_ERROR);
        testDeploymentFailure("inValidProcess-no-forms.xml.par", Messages.DEFINITION_FILE_DOES_NOT_EXIST_ERROR);
        testDeploymentFailure("inValidProcess-forms.xml-error.par", Messages.DEFINITION_FILE_FORMAT_ERROR);
        testDeploymentFailure("inValidProcess-not-an-archive.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testDeploymentFailure("inValidProcess-no-form-file.par", Messages.DEFINITION_FILE_DOES_NOT_EXIST_ERROR);
    }

    private void testDeploymentFailure(String definitionFileName, String errorMessageKey) throws Exception {
        try {
            testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                    WebWfServiceTestHelper.INVALID_PROCESS_NAME);
            fail("testDeployProcessDefinitionInValidProcess, definition exists");
        } catch (ProcessDefinitionDoesNotExistException e) {
            // expected
        }

        form.setFile(getFile(definitionFileName));

        clearGlobalErrors();

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);
        assertNotNull("testDeployProcessDefinitionInValidProcess returns null forward", forward);
        assertEquals("testDeployProcessDefinitionInValidProcess returns wrong forward", FORWARD_FAILURE, forward.getPath());

        ActionMessages messages = getGlobalErrors();
        assertNotNull("testDeployProcessDefinitionInValidProcess returns with errors", messages);
        assertEquals("testDeployProcessDefinitionInValidProcess returns with incorrect size errors", 1, messages.size());
        String actuel = ((ActionMessage) messages.get().next()).getKey();
        assertEquals(" error differs from expected", errorMessageKey, actuel);
        try {
            testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                    WebWfServiceTestHelper.INVALID_PROCESS_NAME);
            assertFalse("testDeployProcessDefinitionInValidProcess, definition exists", true);
        } catch (ProcessDefinitionDoesNotExistException e) {
            // expected
        }
    }
}
