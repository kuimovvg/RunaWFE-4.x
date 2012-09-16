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

import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created on 14.05.2005
 * 
 * @author Gritsenko_S
 */
public class ForkFaultTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ForkFaultTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);

        batchPresentation = helper.getTaskBatchPresentation();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void test1() throws Exception {
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);

        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        List<String> expectedStateNames = Lists.newArrayList("state_2", "state_3");
        List<String> actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());

        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        TaskStub task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        HashMap<String, Object> state2Variables = Maps.newHashMap();
        state2Variables.put("def_variable", "false");
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                state2Variables);

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_3", tasks.get(0).getName());
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }

    public void testFault1() throws Exception {
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.FORK_FAULT_JPDL_PROCESS_NAME);

        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_1", tasks.get(0).getName());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        List<String> expectedStateNames = Lists.newArrayList("state_2", "state_3");
        List<String> actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());

        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        TaskStub task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        HashMap<String, Object> state2Variables = Maps.newHashMap();
        state2Variables.put("def_variable", "true");
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                state2Variables);

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());
        expectedStateNames = Lists.newArrayList("state_1", "state_3");
        actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        task = null;
        if (tasks.get(0).getName().equals("state_1")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_1")) {
                task = tasks.get(1);
            }
        }
        assert (task != null);

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 3, tasks.size());
        expectedStateNames = Lists.newArrayList("state_2", "state_3", "state_3");
        actualStateNames = Lists.newArrayList(tasks.get(0).getName(), tasks.get(1).getName(), tasks.get(2).getName());
        ArrayAssert.assertWeakEqualArrays("state names differs from expected", expectedStateNames, actualStateNames);
        task = null;
        if (tasks.get(0).getName().equals("state_2")) {
            task = tasks.get(0);
        } else {
            if (tasks.get(1).getName().equals("state_2")) {
                task = tasks.get(1);
            } else {
                task = tasks.get(2);
            }
        }

        state2Variables = new HashMap<String, Object>();
        state2Variables.put("def_variable", "false");
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                state2Variables);

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 2, tasks.size());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(1).getId(), tasks.get(1).getName(), tasks.get(1).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "state_4", tasks.get(0).getName());
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), new HashMap<String, Object>());

        tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), batchPresentation);
        assertEquals("tasks length differs from expected", 0, tasks.size());
    }
}
