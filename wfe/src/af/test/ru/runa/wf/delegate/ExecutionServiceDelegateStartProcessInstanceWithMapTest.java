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

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateStartProcessInstanceWithMapTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Map<String, Object> startVariables;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateStartProcessInstanceWithMapTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        helper.createDefaultExecutorsMap();
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.READ, ProcessDefinitionPermission.UPDATE_PERMISSIONS,
                ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        Collection<Permission> executorPermission = Lists.newArrayList(ExecutorPermission.READ);
        helper.setPermissionsToAuthorizedPerformer(executorPermission, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorPermission, helper.getSubGroupActor());

        startVariables = new HashMap<String, Object>();
        startVariables.put("var1start", "var1Value");

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testStartProcessInstanceWithMapByUnauthorizedSubject() throws Exception {
        try {
            executionService.startProcessInstance(helper.getUnauthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);
            assertTrue("testStartProcessInstanceWithMapByUnauthorizedSubject(), no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceWithMapByFakeSubject() throws Exception {
        try {
            executionService.startProcessInstance(helper.getFakeSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);
            assertTrue("testStartProcessInstanceWithMapByFakeSubject(), no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testStartProcessInstanceWithMapByNullSubject() throws Exception {
        try {
            executionService.startProcessInstance(null, WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);
            assertTrue("testStartProcessInstanceWithMapByNullSubject(), no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubjectWithInvalidProcessDefinitionName() throws Exception {
        try {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), "INVALID_PROCESS_NAME", startVariables);
            assertTrue(
                    "testStartProcessInstanceWithMapByAuthorizedSubjectWithInvalidProcessDefinitionName(), no ProcessDefinitionDoesNotExistException",
                    false);
        } catch (ProcessDefinitionDoesNotExistException e) {
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubjectWithNullVariables() throws Exception {
        try {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            assertTrue("testStartProcessInstanceWithMapByAuthorizedSubjectWithNullVariables(), no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedSubject() throws Exception {
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);

        List<ProcessInstanceStub> processInstances = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), helper
                .getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());

        Long taskId = executionService.getTasks(helper.getAdminSubject(), helper.getTaskBatchPresentation()).get(0).getId();
        Map<String, Object> actualVariables = executionService.getVariables(helper.getAuthorizedPerformerSubject(), taskId);

        for (Map.Entry<String, Object> entry : startVariables.entrySet()) {
            assertEquals("No predefined variable", actualVariables.get(entry.getKey()), entry.getValue());
        }
        assertEquals("No swimlane variable", String.valueOf(helper.getAuthorizedPerformerActor().getCode()), actualVariables.get("requester"));
    }

    public void testStartProcessInstanceWithMapInstancePermissions() throws Exception {
        ProcessDefinition defintiion = helper.getDefinitionService().getLatestProcessDefinitionStub(
                helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.getAuthorizationService().setPermissions(helper.getAuthorizedPerformerSubject(), helper.getBaseGroupActor(), permissions,
                defintiion);
        permissions = Lists.newArrayList(ProcessDefinitionPermission.READ_STARTED_INSTANCE, ProcessDefinitionPermission.CANCEL_STARTED_INSTANCE);
        helper.getAuthorizationService().setPermissions(helper.getAuthorizedPerformerSubject(), helper.getSubGroupActor(), permissions,
                defintiion);

        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);

        helper.getExecutionService().getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(),
                helper.getProcessInstanceBatchPresentation());

        ProcessInstanceStub instance = getInstance(WfServiceTestHelper.VALID_PROCESS_NAME);
        Collection<Permission> actual = helper.getAuthorizationService().getOwnPermissions(helper.getAuthorizedPerformerSubject(),
                helper.getBaseGroupActor(), instance);
        Collection<Permission> expected = Lists.newArrayList(ProcessInstancePermission.READ);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance", expected, actual);
        actual = helper.getAuthorizationService().getOwnPermissions(helper.getAuthorizedPerformerSubject(), helper.getSubGroupActor(),
                instance);
        expected = Lists.newArrayList(ProcessInstancePermission.READ, ProcessInstancePermission.CANCEL_INSTANCE);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance", expected, actual);
    }

    private ProcessInstanceStub getInstance(String definitionName) throws ProcessInstanceDoesNotExistException, InternalApplicationException,
            AuthorizationException, AuthenticationException {
        List<ProcessInstanceStub> stubs = helper.getExecutionService().getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(),
                helper.getProcessInstanceBatchPresentation());
        for (ProcessInstanceStub processInstance : stubs) {
            if (definitionName.equals(processInstance.getName())) {
                return processInstance;
            }
        }
        throw new ProcessInstanceDoesNotExistException(definitionName);
    }
}
