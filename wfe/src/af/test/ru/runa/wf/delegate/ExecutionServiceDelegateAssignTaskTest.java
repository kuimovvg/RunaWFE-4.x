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

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * This test class is to check concurrent work of 2 users concerning "Assign task" function.<br />
 * It does not take into account substitution logic.
 * 
 * @see ExecutionServiceDelegateSubstitutionAssignTaskTest
 */
public class ExecutionServiceDelegateAssignTaskTest extends ServletTestCase {

    private final static String PREFIX = ExecutionServiceDelegateAssignTaskTest.class.getName();

    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";

    private Actor actor1;
    private Actor actor2;
    private Group group;

    private Subject actor1Subject = null;
    private Subject actor2Subject = null;

    private WfServiceTestHelper testHelper;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateAssignTaskTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        testHelper = new WfServiceTestHelper(PREFIX);

        actor1 = testHelper.createActorIfNotExist(nameActor1, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), actor1, pwdActor1);
        actor2 = testHelper.createActorIfNotExist(nameActor2, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), actor2, pwdActor2);
        group = testHelper.createGroupIfNotExist(nameGroup, "description");
        testHelper.addExecutorToGroup(actor1, group);
        testHelper.addExecutorToGroup(actor2, group);

        {
            Collection<Permission> perm = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), group, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor1, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor2, perm, ASystem.SYSTEM);
        }

        actor1Subject = testHelper.getAuthenticationService().authenticate(nameActor1, pwdActor1);
        actor2Subject = testHelper.getAuthenticationService().authenticate(nameActor2, pwdActor2);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_NAME + ".par");
        testHelper.getDefinitionService().deployProcessDefinition(testHelper.getAdminSubject(), parBytes, Lists.newArrayList("testProcess"));
        ProcessDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinitionStub(testHelper.getAdminSubject(),
                PROCESS_NAME);
        Collection<Permission> definitionPermission = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS);
        testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor1, definitionPermission, definition);

        batchPresentation = testHelper.getTaskBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        testHelper.getDefinitionService().undeployProcessDefinition(testHelper.getAdminSubject(), PROCESS_NAME);
        testHelper.releaseResources();
        super.tearDown();
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 assigns a task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    // 1
    public void testAssignAssigned() throws Exception {
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        for (int i = 0; i < 3; ++i) {
            moveAssignAssigned();
        }
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 executes a task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testAssignMoved() throws Exception {
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        for (int i = 0; i < 3; ++i) {
            moveAssignMoved();
        }
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 assign a task</li>
     * <li>User 2 tries to move the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveAssigned() throws Exception {
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        for (int i = 0; i < 3; ++i) {
            moveMoveAssigned();
        }
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>User 1 executes a task</li>
     * <li>User 2 tries to execute the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveMoved() throws Exception {
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        moveExecuteExecuted();
    }

    private void moveAssignAssigned() throws Exception {
        TaskStub[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1Subject, 1);
            tasks2 = checkTaskList(actor2Subject, 1);
        }
        Actor actor = SubjectPrincipalsHelper.getActor(actor1Subject);
        testHelper.getExecutionService().assignTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(), actor.getId());
        {
            tasks1 = checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 0);
        }
        assertExceptionThrownOnAssign(actor2Subject, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().completeTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(),
                tasks1[0].getTargetActor().getId(), new HashMap<String, Object>());
    }

    private void moveAssignMoved() throws Exception {
        TaskStub[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1Subject, 1);
            tasks2 = checkTaskList(actor2Subject, 1);
        }
        testHelper.getExecutionService().completeTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(),
                tasks1[0].getTargetActor().getId(), new HashMap<String, Object>());
        assertExceptionThrownOnAssign(actor2Subject, tasks2[0]);
    }

    // ------------------------------------------------------------------------------------------------------------------------
    private void moveMoveAssigned() throws Exception {
        TaskStub[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1Subject, 1);
            tasks2 = checkTaskList(actor2Subject, 1);
        }
        Actor actor = SubjectPrincipalsHelper.getActor(actor1Subject);
        testHelper.getExecutionService().assignTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(), actor.getId());
        {
            tasks1 = checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 0);
        }
        assertExceptionThrownOnExecute(actor2Subject, tasks2[0]);
        {
            tasks1 = checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 0);
        }
        testHelper.getExecutionService().completeTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(),
                tasks1[0].getTargetActor().getId(), new HashMap<String, Object>());
    }

    private void moveExecuteExecuted() throws Exception {
        TaskStub[] tasks1, tasks2;

        {
            tasks1 = checkTaskList(actor1Subject, 1);
            tasks2 = checkTaskList(actor2Subject, 1);
        }
        testHelper.getExecutionService().completeTask(actor1Subject, tasks1[0].getId(), tasks1[0].getName(),
                tasks1[0].getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(actor1Subject, tasks1[0]);
            checkTaskList(actor2Subject, tasks2[0]);
        }
        assertExceptionThrownOnExecute(actor2Subject, tasks2[0]);
    }

    private void assertExceptionThrownOnExecute(Subject actorSub, TaskStub task) throws InternalApplicationException {
        try {
            testHelper.getExecutionService().completeTask(actorSub, task.getId(), task.getName(), task.getTargetActor().getId(),
                    new HashMap<String, Object>());
            throw new InternalApplicationException("Exception TaskDoesNotExistException not thrown");
        } catch (AuthenticationException e) {
        } catch (AuthorizationException e) {
        } catch (TaskDoesNotExistException e) {
        } catch (ExecutorOutOfDateException e) {
            throw new InternalApplicationException("ExecutorOutOfDateException exception thrown");
        } catch (VariablesValidationException e) {
            throw new InternalApplicationException("ValidationException exception thrown");
        }
    }

    // /rask:
    private void assertExceptionThrownOnAssign(Subject actorSub, TaskStub task) throws InternalApplicationException, ExecutorOutOfDateException {
        try {
            Actor actor = SubjectPrincipalsHelper.getActor(actor1Subject);
            testHelper.getExecutionService().assignTask(actorSub, task.getId(), task.getName(), actor.getId());
            throw new InternalApplicationException("Exception TaskAlreadyAcceptedException not thrown");
        } catch (TaskAlreadyAcceptedException e) {
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        }
    }

    private List<TaskStub> checkTaskList(Subject actor, TaskStub task) throws Exception {
        boolean result = false;
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(actor, batchPresentation);
        for (TaskStub taskStub : tasks) {
            if (taskStub.equals(task) && taskStub.getName().equals(task.getName())) {
                result = true;
                break;
            }
        }
        assertFalse("Executed task is still in the user's tasks list.", result);
        return tasks;
    }

    private TaskStub[] checkTaskList(Subject subject, int expectedLength) throws Exception {
        List<TaskStub> tasks = testHelper.getExecutionService().getTasks(subject, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        return tasks.toArray(new TaskStub[tasks.size()]);
    }
}
