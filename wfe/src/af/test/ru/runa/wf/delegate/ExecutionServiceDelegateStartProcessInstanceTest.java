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

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateStartProcessInstanceTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateStartProcessInstanceTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testStartProcessInstanceByAuthorizedSubject() throws Exception {
        Long processInstanceId = executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        assertNotNull(processInstanceId);
        List<ProcessInstanceStub> processInstances = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), helper
                .getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());
        assertEquals(processInstanceId, processInstances.get(0).getId());
    }

    public void testStartProcessInstanceByUnauthorizedSubject() throws Exception {
        try {
            executionService.startProcessInstance(helper.getUnauthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testStartProcessInstanceByUnauthorizedSubject, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceByNullSubject() throws Exception {
        try {
            executionService.startProcessInstance(null, WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testStartProcessInstanceByNullSubject, no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testStartProcessInstanceByFakeSubject() throws Exception {
        try {
            executionService.startProcessInstance(helper.getFakeSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testStartProcessInstanceByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithoutSTARTPermission() throws Exception {
        Collection<Permission> noPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(noPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testStartProcessInstanceByAuthorizedSubjectWithoutSTARTPermission, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithoutREADPermission() throws Exception {
        Collection<Permission> startPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(startPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        List<ProcessInstanceStub> processInstances = executionService.getProcessInstanceStubs(helper.getAdminSubject(), helper
                .getProcessInstanceBatchPresentation());
        assertEquals(1, processInstances.size());
    }

    public void testStartProcessInstanceByAuthorizedSubjectWithInvalidProcessName() throws Exception {
        try {
            executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), "0_INVALID_PROCESS_NAME");
            fail("executionDelegate.startProcessInstance(subj, invalid name), no ProcessDefinitionDoesNotExistException");
        } catch (ProcessDefinitionDoesNotExistException e) {
        }
    }
}
