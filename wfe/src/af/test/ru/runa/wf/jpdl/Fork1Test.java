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
package ru.runa.wf.jpdl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 14.05.2005
 * 
 * @author Gritsenko_S
 */
public class Fork1Test extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    private HashMap<String, Object> startVariables;

    public static Test suite() {
        return new TestSuite(Fork1Test.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_1_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME);

        batchPresentation = helper.getTaskBatchPresentation();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testVariant1() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "false");
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<TaskStub> hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        List<TaskStub> erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        List<TaskStub> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        TaskStub task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        executionService.completeTask(helper.getErpOperatorSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        task = null;
        if (erpTasks.get(0).getName().equals("state_4")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_4")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        executionService.completeTask(helper.getErpOperatorSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant2() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "false");
        startVariables.put("def_variable2", "false");
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<TaskStub> hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        List<TaskStub> erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        List<TaskStub> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());
        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_5", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant3() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "false");
        startVariables.put("def_variable2", "true");
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<TaskStub> hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        List<TaskStub> erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        List<TaskStub> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());
        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        String[] expectedStateNames = { "state_3", "state_5" };
        String[] actualStateNames = { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(1).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        TaskStub task = null;
        if (hrTasks.get(0).getName().equals("state_5")) {
            task = hrTasks.get(0);
        } else {
            if (hrTasks.get(1).getName().equals("state_5")) {
                task = hrTasks.get(1);
            }
        }
        assert (task != null);

        executionService.completeTask(helper.getHrOperatorSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant4() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "true");
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<TaskStub> hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        List<TaskStub> erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        List<TaskStub> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        TaskStub task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        executionService.completeTask(helper.getErpOperatorSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        expectedStateNames = new String[] { "state_3", "state_3" };
        actualStateNames = new String[] { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());
        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(1).getId(), hrTasks.get(1).getName(), hrTasks.get(1).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant4async() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable1", "true");
        startVariables.put("def_variable2", "true");
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_JPDL_1_PROCESS_NAME, startVariables);

        List<TaskStub> hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        List<TaskStub> erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        List<TaskStub> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        TaskStub task = null;
        if (erpTasks.get(0).getName().equals("state_2")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_2")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        executionService.completeTask(helper.getErpOperatorSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, hrTasks.size());
        expectedStateNames = new String[] { "state_3", "state_3" };
        actualStateNames = new String[] { hrTasks.get(0).getName(), hrTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_4", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        TaskStub erpTask2;
        { // Ok, we want to complete tasks from different token. This tasks id must have id of task between them 
            long[] ids = new long[] { erpTasks.get(0).getId(), erpTasks.get(1).getId(), hrTasks.get(0).getId(), hrTasks.get(1).getId() };
            Arrays.sort(ids);
            int idx = Arrays.binarySearch(ids, hrTasks.get(0).getId());

            if (idx > 1) { // search at .get(0) and .get(1)
                if ((erpTasks.get(0).getId() == ids[0]) || (erpTasks.get(0).getId() == ids[1])) {
                    erpTask2 = erpTasks.get(0);
                } else {
                    erpTask2 = erpTasks.get(1);
                }
            } else { // search at [2] and [3]
                if ((erpTasks.get(0).getId() == ids[2]) || (erpTasks.get(0).getId() == ids[3])) {
                    erpTask2 = erpTasks.get(0);
                } else {
                    erpTask2 = erpTasks.get(1);
                }
            }
        }

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());
        executionService.completeTask(helper.getErpOperatorSubject(), erpTask2.getId(), erpTask2.getName(), erpTask2.getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getTargetActor());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorSubject(), hrTasks.get(0).getId(), hrTasks.get(0).getName(), hrTasks.get(0).getTargetActor().getId(),
                new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getTargetActor());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getErpOperatorSubject(), erpTasks.get(0).getId(), erpTasks.get(0).getName(), erpTasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, performerTasks.size());
        expectedStateNames = new String[] { "state_6", "state_6" };
        actualStateNames = new String[] { performerTasks.get(0).getName(), performerTasks.get(1).getName() };
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(1).getTargetActor());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), performerTasks.get(0).getId(), performerTasks.get(0).getName(),
                performerTasks.get(0).getTargetActor().getId(), new HashMap<String, Object>());

        hrTasks = executionService.getTasks(helper.getHrOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }
}
