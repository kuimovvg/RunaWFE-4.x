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
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateGetSwimlanesTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long instanceId;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetSwimlanesTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        //instanceId = 
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        batchPresentation = helper.getProcessInstanceBatchPresentation();
        instanceId = executionService.getProcessInstanceStubs(helper.getAdminSubject(), batchPresentation).get(0).getId();

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetSwimlanesByUnauthorizedSubject() throws Exception {
        try {
            executionService.getSwimlanes(helper.getUnauthorizedPerformerSubject(), instanceId);
            fail("testGetSwimlanesByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetSwimlanesByFakeSubject() throws Exception {
        try {
            executionService.getSwimlanes(helper.getFakeSubject(), instanceId);
            fail("testGetSwimlanesByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetSwimlanesByNullSubject() throws Exception {
        try {
            executionService.getSwimlanes(null, instanceId);
            fail("testGetSwimlanesByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetSwimlanesByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), -1l);
            fail("testGetSwimlanesByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessInstanceDoesNotExistException e) {
        }
    }

    public void testGetSwimlanesByAuthorizedSubject() throws Exception {
        List<SwimlaneStub> swimlanes = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId);
        List<String> expectedNames = Lists.newArrayList("boss", "requester", "erp operator");
        List<String> actualNames = Lists.newArrayList();
        for (SwimlaneStub swimlane : swimlanes) {
            actualNames.add(swimlane.getName());
            if (swimlane.getName().equals("requester")) {
                assertTrue("swimlane is not assigned", swimlane.isAssigned());
                assertEquals("Actor differs from Assigned", helper.getAuthorizedPerformerActor(), swimlane.getExecutor());
            }
        }
        ArrayAssert.assertWeakEqualArrays("swimlane names are not equal", expectedNames, actualNames);
    }
}
