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
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 20.05.2005
 * 
 */
public class RedeployProcessDefinitionActionTest extends StrutsTestCase {

    private static final String FORWARD_FAILURE = "/manage_process_definition.do";

    private static final String FORWARD_SUCCESS = "/manage_process_definition.do";

    private static final String FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST = "/manage_process_definitions.do";

    private ActionMapping mapping;

    private RedeployProcessDefinitionAction action;

    private FileForm form;

    private HttpServletRequestWrapper requestParamWrapper;

    @Override
    public String getTestPrefix() {
        return RedeployProcessDefinitionActionTest.class.getName();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        action = new RedeployProcessDefinitionAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(Resources.FORWARD_SUCCESS, FORWARD_SUCCESS);
        forwards.put(Resources.FORWARD_FAILURE, FORWARD_FAILURE);
        forwards.put(ru.runa.wf.web.Resources.FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST, FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST);
        mapping = getActionMapping(forwards);
        form = new FileForm();
        form.reset(mapping, request);

        testHelper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.REDEPLOY_DEFINITION);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);
        requestParamWrapper = new HttpServletRequestWrapper(request) {
            @Override
            public java.lang.String getParameter(java.lang.String name) {
                if ("type".equals(name)) {
                    return "script";
                } else {
                    return super.getParameter(name);
                }
            }
        };
    }

    @Override
    protected void tearDown() throws Exception {
        mapping = null;
        action = null;
        form = null;
        testHelper.undeployValidProcessDefinition();
        super.tearDown();
    }

    public void testRedeployProcessDefinition() throws Exception {
        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        long version = definitions.get(0).getVersion();

        form.setId(definitions.get(0).getNativeId());

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());

        assertNotNull("testRedeployProcessDefinition returns null forward", forward);
        assertEquals("testRedeployProcessDefinition returns wrong forward", FORWARD_SUCCESS + "?id=" + definitions.get(0).getNativeId(),
                forward.getPath());
        assertNull("testRedeployProcessDefinition returns with errors", getGlobalErrors());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        long version2 = definitions.get(0).getVersion();
        assertEquals("Process definition version doesn't grow", version + 1, version2);
    }

    public void testRedeployProcessDefinitionInvalidDefinitionId() throws Exception {
        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        form.setId(-1L);

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);

        assertNotNull("testRedeployProcessDefinition returns null forward", forward);
        assertEquals("testRedeployProcessDefinition returns wrong forward", FORWARD_FAILURE_DEFINITION_DOES_NOT_EXIST, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testRedeployProcessDefinition returns with errors", messages);
        assertEquals("testRedeployProcessDefinition returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST,
                ((ActionMessage) messages.get().next()).getKey());
    }

    public void testRedeployProcessDefinitionWithoutREDEPLOYPermission() throws Exception {
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ);
        testHelper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WebWfServiceTestHelper.VALID_PROCESS_NAME);

        form.setFile(getFile(WebWfServiceTestHelper.VALID_FILE_NAME));

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        ProcessDefinition expectedDefinition = definitions.get(0);

        form.setId(expectedDefinition.getNativeId());

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);

        assertNotNull("testRedeployProcessDefinition returns null forward", forward);
        assertEquals("testRedeployProcessDefinition returns wrong forward", FORWARD_FAILURE + "?id=" + expectedDefinition.getNativeId(),
                forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testRedeployProcessDefinition returns with errors", messages);
        assertEquals("testRedeployProcessDefinition returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION, ((ActionMessage) messages.get().next()).getKey());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());

        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        assertEquals("Process definition was redeployed", expectedDefinition, definitions.get(0));
    }

    public void testRedeployProcessDefinitionWithDifferentContent() throws Exception {
        form.setFile(getFile(WebWfServiceTestHelper.DECISION_JPDL_PROCESS_FILE_NAME));

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        ProcessDefinition expectedDefinition = definitions.get(0);

        form.setId(expectedDefinition.getNativeId());

        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);

        assertNotNull("testRedeployProcessDefinitionWithDifferentContent returns null forward", forward);
        assertEquals("testRedeployProcessDefinitionWithDifferentContent returns wrong forward",
                FORWARD_FAILURE + "?id=" + expectedDefinition.getNativeId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testRedeployProcessDefinitionWithDifferentContent returns with errors", messages);
        assertEquals("testRedeployProcessDefinitionWithDifferentContent returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", Messages.ERROR_WEB_CLIENT_DEFINITION_NAME_MISMATCH,
                ((ActionMessage) messages.get().next()).getKey());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        assertEquals("Process definition version was grow", expectedDefinition, definitions.get(0));
    }

    public void testRedeployProcessDefinitionWithInvalidContent() throws Exception {
        testRedeploymentFailure("invalidProcess.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testRedeploymentFailure("inValidProcess-no-processdefinition.xml.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testRedeploymentFailure("inValidProcess-no-forms.xml.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testRedeploymentFailure("inValidProcess-forms.xml-error.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testRedeploymentFailure("inValidProcess-not-an-archive.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        testRedeploymentFailure("inValidProcess-no-form-file.par", Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
    }

    private void testRedeploymentFailure(String definitionFileName, String errorMessageKey) throws Exception {
        form.setFile(getFile(definitionFileName));

        List<ProcessDefinition> definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(
                testHelper.getAuthorizedPerformerSubject(), testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        ProcessDefinition expectedDefinition = definitions.get(0);

        form.setId(expectedDefinition.getNativeId());

        clearGlobalErrors();
        ActionForward forward = action.execute(mapping, form, requestParamWrapper, response);

        assertNotNull("testRedeployProcessDefinitionWithDifferentContent returns null forward", forward);
        assertEquals("testRedeployProcessDefinitionWithDifferentContent returns wrong forward",
                FORWARD_FAILURE + "?id=" + expectedDefinition.getNativeId(), forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testRedeployProcessDefinitionWithDifferentContent returns with errors", messages);
        assertEquals("testRedeployProcessDefinitionWithDifferentContent returns with incorrect size errors", 1, messages.size());
        assertEquals(" error differs from expected", errorMessageKey, ((ActionMessage) messages.get().next()).getKey());

        definitions = testHelper.getDefinitionService().getLatestProcessDefinitionStubs(testHelper.getAuthorizedPerformerSubject(),
                testHelper.getProcessDefinitionBatchPresentation());
        assertEquals("Process definitions length differs from expected", 1, definitions.size());
        assertEquals("Process definition version was grow", expectedDefinition, definitions.get(0));
    }
}
