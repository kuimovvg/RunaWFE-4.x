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

import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
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
public class ExecutionServiceDelegateGetVariableNamesTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private TaskStub taskStub;

    private final String variableName = "var1";

    private final String variableValue = "var1Value";

    private long taskId;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetVariableNamesTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        HashMap<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, variablesMap);
        super.setUp();
    }

    private void initTask() throws AuthorizationException, AuthenticationException {
        List<TaskStub> tasks = executionService.getTasks(helper.getAdminSubject(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        taskStub = tasks.get(0);
        taskId = taskStub.getId();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        taskStub = null;
        super.tearDown();
    }

    public void testGetVariableNamesByUnauthorizedSubject() throws Exception {
        initTask();
        try {
            executionService.getVariableNames(helper.getUnauthorizedPerformerSubject(), taskId);
            fail("testGetVariableNamesByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetVariableNamesByFakeSubject() throws Exception {
        initTask();
        try {
            executionService.getVariableNames(helper.getFakeSubject(), taskId);
            fail("testGetVariableNamesByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetVariableNamesByNullSubject() throws Exception {
        initTask();
        try {
            executionService.getVariableNames(null, taskId);
            fail("testGetVariableNamesByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetVariableNamesByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        try {
            executionService.getVariableNames(helper.getUnauthorizedPerformerSubject(), -1l);
            fail("testGetVariableNamesByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException");
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testGetVariableNamesByAuthorizedSubject() throws Exception {
        initTask();
        List<String> actualNames = executionService.getVariableNames(helper.getAuthorizedPerformerSubject(), taskId);
        List<String> expectedNames = Lists.newArrayList("boss", "requester", variableName);

        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, actualNames);
    }
}
