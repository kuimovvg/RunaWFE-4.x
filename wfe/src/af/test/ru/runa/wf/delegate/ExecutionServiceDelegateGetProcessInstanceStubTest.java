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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetProcessInstanceStubTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long processId;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetProcessInstanceStubTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition();

        //processId = 
        executionService.startProcessInstance(helper.getAdminSubject(), WfServiceTestHelper.VALID_PROCESS_NAME);
        processId = executionService.getProcessInstanceStubs(helper.getAuthorizedPerformerSubject(), helper.getProcessInstanceBatchPresentation()).get(0)
                .getId();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetProcessInstanceStubByAuthorizedSubject() throws Exception {
        ProcessInstanceStub processInstance = executionService.getProcessInstanceStub(helper.getAuthorizedPerformerSubject(), processId);
        assertEquals("id of running process differs from requested", processId, processInstance.getId());
        assertEquals("name of running process differs from definition", WfServiceTestHelper.VALID_PROCESS_NAME, processInstance.getName());
    }

    public void testGetProcessInstanceStubByUnauthorizedSubject() throws Exception {
        try {
            executionService.getProcessInstanceStub(helper.getUnauthorizedPerformerSubject(), processId);
            fail("testGetProcessInstanceStubByUnauthorizedSubject, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetProcessInstanceStubByFakeSubject() throws Exception {
        try {
            executionService.getProcessInstanceStub(helper.getFakeSubject(), processId);
            fail("testGetProcessInstanceStubByFakeSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetProcessInstanceStubByNullSubject() throws Exception {
        try {
            executionService.getProcessInstanceStub(null, processId);
            fail("testGetProcessInstanceStubByNullSubject, no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetProcessInstanceStubByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getProcessInstanceStub(helper.getAuthorizedPerformerSubject(), -1l);
            fail("testGetProcessInstanceStubByAuthorizedSubjectWithInvalidProcessId, no ProcessInstanceDoesNotExistException");
        } catch (ProcessInstanceDoesNotExistException e) {
        }
    }
}
