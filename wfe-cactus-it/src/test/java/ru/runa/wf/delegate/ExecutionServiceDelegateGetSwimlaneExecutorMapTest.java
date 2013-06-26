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

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

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

    private String swimlaneName;

    private HashMap<String, Object> legalVariables;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ,
                DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        // instanceId =
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
        instanceId = executionService.getProcesses(helper.getAdminUser(), helper.getProcessInstanceBatchPresentation()).get(0).getId();
        swimlaneName = executionService.getSwimlanes(helper.getAuthorizedPerformerUser(), instanceId).get(0).getDefinition().getName();

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", new Double(200));
        legalVariables.put("amount.granted", new Double(150));
        legalVariables.put("approved", "true");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetSwimlaneExecutorMapByUnauthorizedSubject() throws Exception {
        try {
            executionService.getProcessTasks(helper.getUnauthorizedPerformerUser(), instanceId);
            fail("testGetSwimlaneExecutorMapByUnauthorizedSubject(), no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByFakeSubject() throws Exception {
        try {
            executionService.getProcessTasks(helper.getFakeUser(), instanceId);
            fail("testGetSwimlaneExecutorMapByFakeSubject(), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByNullSubject() throws Exception {
        try {
            executionService.getProcessTasks(null, instanceId);
            fail("testGetSwimlaneExecutorMapByNullSubject(), no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        try {
            executionService.getProcessTasks(helper.getAuthorizedPerformerUser(), -1l);
            fail("testGetSwimlaneExecutorMapByAuthorizedSubjectWithInvalidProcessId(), no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
        }
    }

    //
    // public void testGetSwimlaneExecutorMapByAuthorizedSubject() throws
    // Exception {
    // Collection<Permission> readPermissions =
    // Lists.newArrayList(Permission.READ);
    // helper.setPermissionsToAuthorizedPerformer(readPermissions,
    // helper.getErpOperator());
    //
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    //
    // WfTask task =
    // executionService.getTasks(helper.getAuthorizedPerformerUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // executionService.completeTask(helper.getAuthorizedPerformerUser(),
    // task.getId(), legalVariables);
    //
    // swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // for (Swimlane swimlane : swimlanes) {
    // Map<String, Executor> executorsInSwimlane =
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId,
    // swimlane.getDefinition().getName());
    // for (String name : executorsInSwimlane.keySet()) {
    // Assert.assertEquals("Executor in the swimlane differs from expected",
    // getExpectedExecutor(swimlane), executorsInSwimlane.get(name));
    // }
    // }
    // }
    //
    // public void testGetSwimlaneExecutorMapDeletedExecutor() throws Exception
    // {
    // WfTask task =
    // executionService.getTasks(helper.getAuthorizedPerformerUser(),
    // helper.getTaskBatchPresentation()).get(0);
    // executionService.completeTask(helper.getAuthorizedPerformerUser(),
    // task.getId(), legalVariables);
    // List<Swimlane> swimlanes =
    // executionService.getSwimlanes(helper.getAuthorizedPerformerUser(),
    // instanceId);
    // Swimlane swimlane = null;
    // for (Swimlane existing : swimlanes) {
    // if ("erp operator".equals(existing.getDefinition().getName())) {
    // swimlane = existing;
    // break;
    // }
    // }
    // assert (swimlane != null);
    // helper.removeCreatedExecutor(helper.getErpOperator());
    // helper.removeExecutorIfExists(helper.getErpOperator());
    // try {
    // executionService.getActiveTasks(helper.getAuthorizedPerformerUser(),
    // instanceId, swimlane.getDefinition().getName());
    // fail("executionDelegate.getSwimlaneExecutorMap() does not throw exception for getting swimlane for nonexisting executor");
    // } catch (ExecutorDoesNotExistException e) {
    // }
    // }

    private Executor getExpectedExecutor(WfSwimlane WfSwimlane) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        String name = WfSwimlane.getDefinition().getName();
        if (name.equals("requester")) {
            return helper.getAuthorizedPerformerActor();
        } else if (name.equals("boss")) {
            return helper.getBossGroup();
        } else if (name.equals("erp operator")) {
            return helper.getErpOperator();
        } else {
            throw new RuntimeException("Executor for swimlane " + WfSwimlane.getDefinition().getName() + " is unknown");
        }
    }
}
