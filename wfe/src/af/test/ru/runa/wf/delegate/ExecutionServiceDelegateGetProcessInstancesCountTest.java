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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessInstancesCountTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetProcessInstancesCountTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetProcessInstanceCountByAuthorizedSubject() throws Exception {
        int processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountByUnauthorizedSubject() throws Exception {
        int processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getUnauthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
    }

    public void testGetProcessInstanceCountByFakeSubject() throws Exception {
        try {
            executionService.getAllProcessInstanceStubsCount(helper.getFakeSubject(), batchPresentation);
            assertFalse("testGetAllProcessInstanceStubsByFakeSubject, no AuthenticationException", true);
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceCountByNullSubject() throws Exception {
        try {
            executionService.getAllProcessInstanceStubsCount(null, batchPresentation);
            assertFalse("testGetAllProcessInstanceStubsByNullSubject, no IllegalArgumentException", true);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetProcessInstanceCountByAuthorizedSubjectWithoutREADPermission() throws Exception {
        int processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 4;
        startInstances(expectedCount);
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        List<ProcessInstanceStub> processInstanceStubs = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(),
                batchPresentation);

        Collection<Permission> nullPermissions = Permission.getNoPermissions();
        int withoutPermCount = processInstanceStubs.size() / 2;
        for (int i = 0; i < withoutPermCount; i++) {
            helper.setPermissionsToAuthorizedPerformerOnProcessInstance(nullPermissions, processInstanceStubs.get(i));
        }

        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount - withoutPermCount, processesCount);
    }

    public void testGetProcessInstanceCountWithSorting() throws Exception {
        int processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToSort(new int[] { 0, 1, 2, 3 }, new boolean[] { true, false, true, false });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    public void testGetProcessInstanceCountWithGrouping() throws Exception {
        int processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", 0, processesCount);
        int expectedCount = 20;
        startInstances(expectedCount);
        batchPresentation.setFieldsToGroup(new int[] { 0 });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1 });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);

        batchPresentation.setFieldsToGroup(new int[] { 0, 1, 2, 3 });
        processesCount = executionService.getAllProcessInstanceStubsCount(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Incorrect processes array", expectedCount, processesCount);
    }

    private void startInstances(int instanceCount) throws ProcessDefinitionDoesNotExistException, AuthorizationException, AuthenticationException,
            InternalApplicationException, VariablesValidationException {
        for (int i = 0; i < instanceCount; i++) {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        }
    }

}
