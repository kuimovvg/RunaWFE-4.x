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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
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
public class ExecutionServiceDelegateCancelProcessInstanceTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private ProcessInstanceStub processInstance = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateCancelProcessInstanceTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);

        batchPresentation = helper.getProcessInstanceBatchPresentation();

        processInstance = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation).get(0);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testCancelProcessInstanceByAuthorizedSubject() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE), processInstance);
        executionService.cancelProcessInstance(helper.getAuthorizedPerformerSubject(), processInstance.getId());

        List<ProcessInstanceStub> processInstances = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("Process not cancelled", 0, processInstances.size());
    }

    public void testCancelProcessInstanceByAuthorizedSubjectWithoutCANCELPermission() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(new ArrayList<Permission>(), processInstance);
        try {
            executionService.cancelProcessInstance(helper.getAuthorizedPerformerSubject(), processInstance.getId());
            assertFalse("testCancelProcessInstanceByAuthorizedSubjectWithoutCANCELPermission, no AuthorizationException", true);
        } catch (AuthorizationException e) {
        }
    }

    public void testCancelProcessInstanceByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(ProcessInstancePermission.CANCEL_INSTANCE), processInstance);
        try {
            executionService.cancelProcessInstance(helper.getAuthorizedPerformerSubject(), -1l);
            assertFalse("testCancelProcessInstanceByAuthorizedSubjectWithInvalidProcessId, no ProcessInstanceDoesNotExistException", true);
        } catch (ProcessInstanceDoesNotExistException e) {
        }
    }

    public void testCancelProcessInstanceByFakeSubject() throws Exception {
        try {
            executionService.cancelProcessInstance(helper.getFakeSubject(), processInstance.getId());
            assertFalse("executionDelegate.cancelProcessInstance(helper.getFakeSubject(), ..), no AuthenticationException", true);
        } catch (AuthenticationException e) {
        }
    }

    public void testCancelProcessInstanceByNullSubject() throws Exception {
        try {
            executionService.cancelProcessInstance(null, processInstance.getId());
            assertFalse("testCancelProcessInstanceByNullSubject, no IllegalArgumentException", true);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCancelProcessInstanceByUnauthorizedSubject() throws Exception {
        try {
            executionService.cancelProcessInstance(helper.getUnauthorizedPerformerSubject(), processInstance.getId());
            List<ProcessInstanceStub> processInstances = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(),
                    batchPresentation);
            assertEquals("Process was cancelled by unauthorized subject", 1, processInstances.size());
        } catch (AuthorizationException e) {
        }
    }
}
