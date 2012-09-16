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
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 02.05.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetSwimlaneExecutorMapTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private Long instanceId;

    private Long swimlaneId;

    private HashMap<String, Object> legalVariables;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateGetSwimlaneExecutorMapTest.class);
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

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
        instanceId = executionService.getProcessInstanceStubs(helper.getAdminSubject(), helper.getProcessInstanceBatchPresentation()).get(0).getId();
        swimlaneId = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId).get(0).getId();

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", new Double(200));
        legalVariables.put("amount.granted", new Double(150));
        legalVariables.put("approved", "true");

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetSwimlaneExecutorMapByUnauthorizedSubject() throws Exception {
        try {
            executionService.getSwimlaneExecutorMap(helper.getUnauthorizedPerformerSubject(), instanceId, swimlaneId);
            fail("testGetSwimlaneExecutorMapByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByFakeSubject() throws Exception {
        try {
            executionService.getSwimlaneExecutorMap(helper.getFakeSubject(), instanceId, swimlaneId);
            fail("testGetSwimlaneExecutorMapByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByNullSubject() throws Exception {
        try {
            executionService.getSwimlaneExecutorMap(null, instanceId, swimlaneId);
            fail("testGetSwimlaneExecutorMapByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getSwimlaneExecutorMap(helper.getAuthorizedPerformerSubject(), -1l, swimlaneId);
            fail("testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessInstanceDoesNotExistException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidSwimlaneId() throws Exception {
        Map<String, List<Executor>> map = executionService.getSwimlaneExecutorMap(helper.getAuthorizedPerformerSubject(), instanceId, -1l);
        assertEquals("getSwimlaneExecutorMap() returns non empty map for invalid swimlane", map.size(), 0);
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubject() throws Exception {
        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        helper.setPermissionsToAuthorizedPerformer(readPermissions, helper.getErpOperator());

        List<SwimlaneStub> swimlanes = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId);

        swimlanes = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId);
        for (SwimlaneStub swimlane : swimlanes) {
            Map<String, List<Executor>> executorsInSwimlane = executionService.getSwimlaneExecutorMap(helper.getAuthorizedPerformerSubject(), instanceId, swimlane.getId());
            for (String name : executorsInSwimlane.keySet()) {
                ArrayAssert.assertWeakEqualArrays("Executors in the swimlane differs from expected", getExpectedExecutors(swimlane),
                        executorsInSwimlane.get(name));
            }
        }

        TaskStub task = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation()).get(0);
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                legalVariables);

        swimlanes = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId);
        for (SwimlaneStub swimlane : swimlanes) {
            Map<String, List<Executor>> executorsInSwimlane = executionService.getSwimlaneExecutorMap(helper.getAuthorizedPerformerSubject(), instanceId, swimlane.getId());
            for (String name : executorsInSwimlane.keySet()) {
                ArrayAssert.assertWeakEqualArrays("Executors in the swimlane differs from expected", getExpectedExecutors(swimlane),
                        executorsInSwimlane.get(name));
            }
        }
    }

    public void testGetSwimlaneExecutorMapDeletedExecutor() throws Exception {
        TaskStub task = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation()).get(0);
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                legalVariables);
        List<SwimlaneStub> swimlanes = executionService.getSwimlanes(helper.getAuthorizedPerformerSubject(), instanceId);
        SwimlaneStub swimlaneStub = null;
        for (SwimlaneStub swimlane : swimlanes) {
            if ("erp operator".equals(swimlane.getName())) {
                swimlaneStub = swimlane;
                break;
            }
        }
        assert (swimlaneStub != null);
        helper.removeCreatedExecutor(helper.getErpOperator());
        helper.removeExecutorIfExists(helper.getErpOperator());
        try {
            executionService.getSwimlaneExecutorMap(helper.getAuthorizedPerformerSubject(), instanceId, swimlaneStub.getId());
            fail("executionDelegate.getSwimlaneExecutorMap() does not throw exception for getting swimlane for nonexisting executor");
        } catch (ExecutorOutOfDateException e) {
        }
    }

    private List<? extends Executor> getExpectedExecutors(SwimlaneStub swimlane) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        String name = swimlane.getName();
        if (name.equals("requester")) {
            return Lists.newArrayList(helper.getAuthorizedPerformerActor());
        } else if (name.equals("boss")) {
            return Lists.newArrayList(helper.getBossGroup());
        } else if (name.equals("erp operator")) {
            return Lists.newArrayList(helper.getErpOperator());
        } else {
            throw new RuntimeException("Executor for swimlane " + swimlane.getName() + " is unknown");
        }
    }
}
