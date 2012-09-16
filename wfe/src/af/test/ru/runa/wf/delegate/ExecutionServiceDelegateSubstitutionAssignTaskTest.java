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
import ru.runa.af.ActorPermission;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
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
 * This test class is to check substitution logic concerning "Assign task" function.<br />
 * It does not take into account concurrent work of the several members of the same group.
 * 
 * @see ExecutionServiceDelegateAssignTaskTest
 */
public class ExecutionServiceDelegateSubstitutionAssignTaskTest extends ServletTestCase {

    private final static String PREFIX = ExecutionServiceDelegateSubstitutionAssignTaskTest.class.getName();

    private static final String PROCESS_NAME = WfServiceTestHelper.SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME;

    private final static String nameActor1 = "actor1";
    private final static String nameActor2 = "actor2";
    private final static String nameGroup = "testGroup";
    private final static String nameSubstitute = "substitute";

    private final static String pwdActor1 = "123";
    private final static String pwdActor2 = "123";
    private final static String pwdSubstitute = "123";

    private Actor actor1;
    private Actor actor2;
    private Group group;
    private Actor substitute;

    private Subject actor1Subject = null;
    private Subject actor2Subject = null;
    private Subject substituteSubject = null;

    private SubstitutionCriteria substitutionCriteria_always;

    private WfServiceTestHelper testHelper;

    private BatchPresentation batchPresentation;

    public static Test suite() {
        return new TestSuite(ExecutionServiceDelegateSubstitutionAssignTaskTest.class);
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
        substitute = testHelper.createActorIfNotExist(nameSubstitute, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminSubject(), substitute, pwdSubstitute);

        {
            Collection<Permission> perm = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), group, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor1, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor2, perm, ASystem.SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitute, perm, ASystem.SYSTEM);
        }
        {
            Collection<Permission> perm = Lists.newArrayList(ActorPermission.READ);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), actor1, perm, substitute);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminSubject(), substitute, perm, actor1);
        }
        actor1Subject = testHelper.getAuthenticationService().authenticate(nameActor1, pwdActor1);
        actor2Subject = testHelper.getAuthenticationService().authenticate(nameActor2, pwdActor2);
        substituteSubject = testHelper.getAuthenticationService().authenticate(nameSubstitute, pwdSubstitute);

        substitutionCriteria_always = null;
        testHelper.createSubstitutionCriteria(substitutionCriteria_always);

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
        testHelper.removeSubstitutionCriteria(substitutionCriteria_always);
        super.tearDown();
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    // rask:
    public void testAssignAssigned() throws Exception {
        TaskStub[] actor1Tasks;
        TaskStub[] actor2Tasks;
        TaskStub[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1Subject, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1Subject, 0);
            actor2Tasks = checkTaskList(actor2Subject, 0);
            substituteTasks = checkTaskList(substituteSubject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        {
            checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 1);
            checkTaskList(substituteSubject, 0);
        }
        testHelper.setActorStatus(actor1.getId(), false);
        testHelper.setActorStatus(actor2.getId(), false);
        {
            actor1Tasks = checkTaskList(actor1Subject, 1);
            actor2Tasks = checkTaskList(actor2Subject, 1);
            substituteTasks = checkTaskList(substituteSubject, 1);
        }
        Actor actor = SubjectPrincipalsHelper.getActor(substituteSubject);
        testHelper.getExecutionService().assignTask(substituteSubject, substituteTasks[0].getId(), substituteTasks[0].getName(),
                actor.getId());
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
            substituteTasks = checkTaskList(substituteSubject, 1);
        }
        assertExceptionThrownOnAssign(actor1Subject, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2Subject, actor2Tasks[0]);
        testHelper.getExecutionService().completeTask(substituteSubject, substituteTasks[0].getId(), substituteTasks[0].getName(),
                substituteTasks[0].getTargetActor().getId(), new HashMap<String, Object>());
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute executes a task</li>
     * <li>User 1 tries to assign the task</li>
     * <li>User 2 tries to assign the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testAssignMoved() throws Exception {
        TaskStub[] actor1Tasks;
        TaskStub[] actor2Tasks;
        TaskStub[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1Subject, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1Subject, 0);
            actor2Tasks = checkTaskList(actor2Subject, 0);
            substituteTasks = checkTaskList(substituteSubject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        {
            checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 1);
            checkTaskList(substituteSubject, 0);
        }
        testHelper.setActorStatus(actor1.getId(), false);
        testHelper.setActorStatus(actor2.getId(), false);
        {
            actor1Tasks = checkTaskList(actor1Subject, 1);
            actor2Tasks = checkTaskList(actor2Subject, 1);
            substituteTasks = checkTaskList(substituteSubject, 1);
        }
        testHelper.getExecutionService().completeTask(substituteSubject, substituteTasks[0].getId(), substituteTasks[0].getName(),
                substituteTasks[0].getTargetActor().getId(), new HashMap<String, Object>());
        {
            checkTaskList(actor1Subject, actor1Tasks[0]);
            checkTaskList(actor2Subject, actor2Tasks[0]);
            checkTaskList(substituteSubject, substituteTasks[0]);
        }
        assertExceptionThrownOnAssign(actor1Subject, actor1Tasks[0]);
        assertExceptionThrownOnAssign(actor2Subject, actor2Tasks[0]);
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    /**
     * This method is to check the following test case:
     * <ul>
     * <li>Substitute assigns a task</li>
     * <li>User 1 tries to execute the task</li>
     * <li>User 2 tries to execute the task</li>
     * </ul>
     * 
     * @throws Exception
     */
    public void testMoveAssigned() throws Exception {
        TaskStub[] actor1Tasks;
        TaskStub[] actor2Tasks;
        TaskStub[] substituteTasks;

        Substitution substitution1 = testHelper.createActorSubstitutor(actor1Subject, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitute + ")", substitutionCriteria_always, true);
        {
            actor1Tasks = checkTaskList(actor1Subject, 0);
            actor2Tasks = checkTaskList(actor2Subject, 0);
            substituteTasks = checkTaskList(substituteSubject, 0);
        }
        testHelper.getExecutionService().startProcessInstance(actor1Subject, PROCESS_NAME);
        {
            checkTaskList(actor1Subject, 1);
            checkTaskList(actor2Subject, 1);
            checkTaskList(substituteSubject, 0);
        }
        testHelper.setActorStatus(actor1.getId(), false);
        testHelper.setActorStatus(actor2.getId(), false);
        {
            actor1Tasks = checkTaskList(actor1Subject, 1);
            actor2Tasks = checkTaskList(actor2Subject, 1);
            substituteTasks = checkTaskList(substituteSubject, 1);
        }
        Actor actor = SubjectPrincipalsHelper.getActor(substituteSubject);
        testHelper.getExecutionService().assignTask(substituteSubject, substituteTasks[0].getId(), substituteTasks[0].getName(),
                actor.getId());
        {
            checkTaskList(actor1Subject, 0);
            checkTaskList(actor2Subject, 0);
            substituteTasks = checkTaskList(substituteSubject, 1);
        }
        assertExceptionThrownOnExecute(actor1Subject, actor1Tasks[0]);
        assertExceptionThrownOnExecute(actor2Subject, actor2Tasks[0]);
        testHelper.removeCriteriaFromSubstitution(substitution1);
    }

    private void assertExceptionThrownOnExecute(Subject actorSub, TaskStub task) throws InternalApplicationException {
        try {
            testHelper.getExecutionService().completeTask(actorSub, task.getId(), task.getName(), task.getTargetActor().getId(),
                    new HashMap<String, Object>());
            throw new InternalApplicationException("Exception not thrown. Actor shouldn't see assigned/executed task by another user...");
        } catch (AuthenticationException e) {
            throw new InternalApplicationException("Auth exception thrown");
        } catch (AuthorizationException e) {
            // task was already assigned/executed by another user
        } catch (TaskDoesNotExistException e) {
        } catch (ExecutorOutOfDateException e) {
            throw new InternalApplicationException("ExecutorOutOfDateException exception thrown");
        } catch (VariablesValidationException e) {
            throw new InternalApplicationException("ValidationException exception thrown");
        }
    }

    private void assertExceptionThrownOnAssign(Subject actorSub, TaskStub task) throws InternalApplicationException, ExecutorOutOfDateException {
        try {
            Actor actor = SubjectPrincipalsHelper.getActor(actorSub);
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
