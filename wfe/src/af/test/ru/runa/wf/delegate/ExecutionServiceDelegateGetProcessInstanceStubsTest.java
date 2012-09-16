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
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.ClassPresentation;
import ru.runa.af.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S    
 */
public class ExecutionServiceDelegateGetProcessInstanceStubsTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetProcessInstanceStubsTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetProcessInstanceStubsByVariableFilterByAuthorizedSubject() throws Exception {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        for (int i = 0; i < batchPresentation.getAllFields().length; ++i) {
            if (batchPresentation.getAllFields()[i].dbSources[0].equals(VariableInstance.class)
                    && batchPresentation.getAllFields()[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                batchPresentation.addDynamicField(i, name);
            }
        }
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFieldsMap();
        filters.put(new Integer(0), new AnywhereStringFilterCriteria(new String[] { value }));
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        for (ProcessInstanceStub processInstanceStub : processes) {
            Map<String, Object> instanceVariablesMap = executionService.getInstanceVariables(helper.getAuthorizedPerformerSubject(),
                    processInstanceStub.getId());
            int instancesFound = 0;
            for (Map.Entry<String, Object> variable : instanceVariablesMap.entrySet()) {
                if ((name.equals(variable.getKey())) && (value.equals(variable.getValue()))) {
                    instancesFound++;
                }
            }
            assertEquals(instancesFound, 3);
        }
    }

    public void testGetProcessInstanceStubsByVariableFilterWithWrongMatcherByAuthorizedSubject() throws Exception {
        String name = "reason";
        String value = "intention";
        Map<String, Object> variablesMap = WfServiceTestHelper.createVariablesMap(name, value);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, variablesMap);
        for (int i = 0; i < batchPresentation.getAllFields().length; ++i) {
            if (batchPresentation.getAllFields()[i].dbSources[0].equals(VariableInstance.class)
                    && batchPresentation.getAllFields()[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                batchPresentation.addDynamicField(i, name);
            }
        }
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFieldsMap();
        filters.put(new Integer(0), new AnywhereStringFilterCriteria(new String[] { "bad matcher" }));
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        for (ProcessInstanceStub processInstanceStub : processes) {
            Map<String, Object> instanceVariablesMap = executionService.getInstanceVariables(helper.getAuthorizedPerformerSubject(),
                    processInstanceStub.getId());
            int instancesFound = 0;
            for (Map.Entry<String, Object> variable : instanceVariablesMap.entrySet()) {
                if ((name.equals(variable.getKey())) && (value.equals(variable.getValue()))) {
                    instancesFound++;
                }
            }
            assertEquals(instancesFound, 0);
        }
    }

    public void testGetProcessInstanceStubsByAuthorizedSubject() throws Exception {
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
    }

    public void testGetProcessInstanceStubsByUnauthorizedSubject() throws Exception {
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getUnauthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        processes = executionService.getProcessInstanceStubs(helper.getUnauthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsByFakeSubject() throws Exception {
        try {
            executionService.getProcessInstanceStubs(helper.getFakeSubject(), batchPresentation);
            assertFalse("testGetAllProcessInstanceStubsByFakeSubject, no AuthenticationException", true);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceStubsByNullSubject() throws Exception {
        try {
            executionService.getProcessInstanceStubs(null, batchPresentation);
            assertFalse("testGetAllProcessInstanceStubsByNullSubject, no IllegalArgumentException", true);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetProcessInstanceStubsByAuthorizedSubjectWithoutREADPermission() throws Exception {
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        processes = executionService.getProcessInstanceStubs(helper.getAdminSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 1, processes.size());
        Collection<Permission> nullPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(nullPermissions, processes.get(0));
        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());
    }

    public void testGetProcessInstanceStubsPagingByAuthorizedSubject() throws Exception {
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        int expectedCount = 14;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        }
        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, processes.size());

        batchPresentation.setPageNumber(2);

        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - rangeSize, processes.size());

        rangeSize = 50;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);

        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processes.size());
    }

    public void testGetProcessInstanceStubsUnexistentPageByAuthorizedSubject() throws Exception {
        List<ProcessInstanceStub> processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processes.size());

        int rangeSize = 10;
        batchPresentation.setRangeSize(rangeSize);
        batchPresentation.setPageNumber(1);
        batchPresentation.setFieldsToSort(new int[] { 3 }, new boolean[] { true });

        int expectedCount = 17;
        for (int i = 0; i < expectedCount; i++) {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        }
        List<ProcessInstanceStub> firstTenProcesses = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", rangeSize, firstTenProcesses.size());

        batchPresentation.setPageNumber(2);
        processes = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - 10, processes.size());

        //the wrong page is replaced by last page in case it contains 0 objects
        batchPresentation.setPageNumber(3);

        List<ProcessInstanceStub> wrongPageProcesses = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(),
                batchPresentation);
        // due to ru.runa.af.presentation.BatchPresentation.setFilteredFieldsMap(Map<Integer, FilterCriteria>) in hibernate.update
        ArrayAssert.assertEqualArrays("Incorrect returned", firstTenProcesses, wrongPageProcesses);
    }

}
