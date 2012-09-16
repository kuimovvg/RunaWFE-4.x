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
package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetVariablesTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long taskId;
    private String taskName;

    private final String variableName = "var1";

    private final String variableValue = "var1Value";

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetVariablesTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        HashMap<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
        TaskStub taskStub = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation()).get(0);
        taskId = taskStub.getId();
        taskName = taskStub.getName();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetVariablesByUnauthorizedSubject() throws Exception {
        try {
            executionService.getVariables(helper.getUnauthorizedPerformerSubject(), taskId);
            assertTrue("testGetVariablesByUnauthorizedSubject(), no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testGetVariablesByFakeSubject() throws Exception {
        try {
            executionService.getVariables(helper.getFakeSubject(), taskId);
            assertTrue("testGetVariablesByFakeSubject(), no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetVariablesByNullSubject() throws Exception {
        try {
            executionService.getVariables(null, taskId);
            assertTrue("testGetVariablesByNullSubject(), no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetVariablesByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        try {
            executionService.getVariables(helper.getAuthorizedPerformerSubject(), -1l);
            fail("testGetVariablesByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetVariablesByAuthorizedSubject() throws Exception {
        Map<String, Object> variables = executionService.getVariables(helper.getAuthorizedPerformerSubject(), taskId);

        List<String> expectedNames = Lists.newArrayList("boss", "requester", variableName);
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, variables.keySet());

        HashMap<String, Object> variables2 = new HashMap<String, Object>();
        variables2.put("var2", "var2Value");
        variables2.put("var3", "var3Value");
        variables2.put("approved", "true");
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), taskId, taskName, helper.getAuthorizedPerformerActor().getId(),
                variables2);

        taskId = executionService.getTasks(helper.getErpOperatorSubject(), helper.getTaskBatchPresentation()).get(0).getId();

        variables = executionService.getVariables(helper.getErpOperatorSubject(), taskId);

        expectedNames = Lists.newArrayList("boss", "requester", "var2", "var3", "approved", variableName, "erp operator");
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, variables.keySet());

        assertEquals(" variable value: <var1> differs from expected", "var1Value", variables.get("var1"));
        assertEquals(" variable value: <var2> differs from expected", "var2Value", variables.get("var2"));
        assertEquals(" variable value: <var3> differs from expected", "var3Value", variables.get("var3"));
        assertEquals(" variable value: <approved> differs from expected", "true", variables.get("approved"));
    }
}
