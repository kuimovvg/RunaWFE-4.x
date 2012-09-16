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
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 * @author Vitaliy S    
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class ExecutionServiceDelegateCompleteTaskTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private TaskStub task;

    private Map<String, Object> legalVariables;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateCompleteTaskTest.class);
    }

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
        //task = executionDelegate.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation())[0];

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("amount.asked", new Double(200));
        legalVariables.put("amount.granted", new Double(150));
        legalVariables.put("approved", "true");

        super.setUp();
    }

    private void initTask() throws AuthorizationException, AuthenticationException {
        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation());
        assertNotNull(tasks);
        assertEquals(tasks.size() > 0, true);
        task = tasks.get(0);
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testCompleteTaskByAuthorizedSubject() throws Exception {
        initTask();

        assertEquals("state name differs from expected", "evaluating", task.getName());
        assertEquals("task <evaluating> is assigned before completeTask()", helper.getAuthorizedPerformerActor(), task.getTargetActor());

        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                legalVariables);
        List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(), helper.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Authorized Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());
        assertEquals("task <treating collegues on cake and pie> is not assigned after starting [requester]", helper.getAuthorizedPerformerActor(),
                task.getTargetActor());
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), tasks.get(0).getId(), tasks.get(0).getName(), tasks.get(0).getTargetActor()
                .getId(), legalVariables);

        tasks = executionService.getTasks(helper.getErpOperatorSubject(), helper.getTaskBatchPresentation());

        assertEquals("Tasks not returned for Erp Operator Subject", 1, tasks.size());
        assertEquals("state name differs from expected", "updating erp asynchronously", tasks.get(0).getName());
        assertEquals("task <updating erp asynchronously> is not assigned before competeTask()", helper.getAuthorizedPerformerActor(), task
                .getTargetActor());
    }

    public void testCompleteTaskBySubjectWhichIsNotInSwimlane() throws Exception {
        initTask();
        try {
            helper.removeExecutorFromGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                    legalVariables);
            fail("testCompleteTaskByNullSubject(), no Exception");
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByUnauthorizedSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getUnauthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(),
                    legalVariables);
            assertTrue("testCompleteTaskByNullSubject(), no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testCompleteTaskByNullSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(null, task.getId(), task.getName(), task.getTargetActor().getId(), legalVariables);
            assertTrue("testCompleteTaskByNullSubject(), no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCompleteTaskByFakeSubject() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getFakeSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), legalVariables);
            assertTrue("testCompleteTaskByFakeSubject(), no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithInvalidTaskId() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), -1l, task.getName(), task.getTargetActor().getId(), legalVariables);
            assertTrue("testCompleteTaskByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException", false);
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithInvalidTaskName() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName() + "fake", task.getTargetActor()
                    .getId(), legalVariables);
            assertTrue("testCompleteTaskByAuthorizedSubjectWithInvalidTaskId(), no TaskDoesNotExistException", false);
        } catch (TaskDoesNotExistException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithNullVariables() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), task.getTargetActor().getId(), null);
        } catch (IllegalArgumentException e) {
            assertTrue("testCompleteTaskByAuthorizedSubjectWithNullVariables(), IllegalArgumentException is thrown", false);
        } catch (Throwable e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithInvalidActorId() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), -1l, legalVariables);
            fail("not failed with invalid actor id");
        } catch (ExecutorOutOfDateException e) {
        }
    }

    public void testCompleteTaskByAuthorizedSubjectWithIncorrectActorId() throws Exception {
        initTask();
        try {
            executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), helper
                    .getUnauthorizedPerformerActor().getId(), legalVariables);
            fail("not failed with incorrect actor id");
        } catch (AuthorizationException e) {
        }
    }

}
