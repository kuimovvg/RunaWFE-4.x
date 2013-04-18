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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 16.05.2005
 * 
 * @author Gritsenko_S
 */
public class Fork2Test extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    private HashMap<String, Object> startVariables;

    public static Test suite() {
        return new TestSuite(Fork2Test.class);
    }

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS, DefinitionPermission.READ,
                DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);

        batchPresentation = helper.getTaskBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testVariant1() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable", "false");
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_5", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_7", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);
        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(1).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant2() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable", "true");
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask task = null;
        if (erpTasks.get(0).getName().equals("state_4")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_4")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);
        executionService.completeTask(helper.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        task = null;
        if (erpTasks.get(0).getName().equals("state_7")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_7")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        executionService.completeTask(helper.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_4", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }

    public void testVariant3() throws Exception {
        startVariables = new HashMap<String, Object>();
        startVariables.put("def_variable", "true");
        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.FORK_JPDL_2_PROCESS_NAME, startVariables);

        List<WfTask> hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_1", hrTasks.get(0).getName());
        assertEquals("task is assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        List<WfTask> erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        List<WfTask> performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        String[] expectedStateNames = { "state_2", "state_4" };
        String[] actualStateNames = { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_2", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        WfTask task = null;
        if (erpTasks.get(0).getName().equals("state_4")) {
            task = erpTasks.get(0);
        } else {
            if (erpTasks.get(1).getName().equals("state_4")) {
                task = erpTasks.get(1);
            }
        }
        assert (task != null);

        executionService.completeTask(helper.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_2", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, hrTasks.size());
        assertEquals("task name differs from expected", "state_3", hrTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getHrOperator(), hrTasks.get(0).getOwner());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, erpTasks.size());
        expectedStateNames = new String[] { "state_7", "state_4" };
        actualStateNames = new String[] { erpTasks.get(0).getName(), erpTasks.get(1).getName() };
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(1).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
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

        executionService.completeTask(helper.getHrOperatorUser(), hrTasks.get(0).getId(), new HashMap<String, Object>(), null);
        executionService.completeTask(helper.getErpOperatorUser(), task.getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, erpTasks.size());
        assertEquals("task name differs from expected", "state_7", erpTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getErpOperator(), erpTasks.get(0).getOwner());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());

        executionService.completeTask(helper.getErpOperatorUser(), erpTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_6", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, performerTasks.size());
        assertEquals("task name differs from expected", "state_8", performerTasks.get(0).getName());
        assertEquals("task is not assigned", helper.getAuthorizedPerformerActor(), performerTasks.get(0).getOwner());

        executionService.completeTask(helper.getAuthorizedPerformerUser(), performerTasks.get(0).getId(), new HashMap<String, Object>(), null);

        hrTasks = executionService.getTasks(helper.getHrOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, hrTasks.size());

        erpTasks = executionService.getTasks(helper.getErpOperatorUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, erpTasks.size());

        performerTasks = executionService.getTasks(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, performerTasks.size());
    }
}
