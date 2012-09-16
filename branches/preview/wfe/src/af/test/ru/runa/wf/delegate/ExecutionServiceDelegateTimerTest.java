package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.cactus.ServletTestCase;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.delegate.impl.ExecutionServiceDelegateImpl;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

public class ExecutionServiceDelegateTimerTest extends ServletTestCase {

    private static final String STATE_KOCHAB = "Kochab";

    private static final String STATE_ANWAR = "Anwar";

    private static final String STATE_ALIFA = "Alifa";

    private ExecutionServiceDelegateImpl executionDelegate = null;

    private WfServiceTestHelper helper = null;

    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        helper.deployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME + ".par");
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.TIMER_PROCESS_NAME);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());

        executionDelegate = new ExecutionServiceDelegateImpl();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME);
        helper.releaseResources();
        executionDelegate = null;
        super.tearDown();
    }

    public void test() throws InternalApplicationException, AuthorizationException, AuthenticationException, ProcessDefinitionDoesNotExistException,
            VariablesValidationException, TaskDoesNotExistException, ExecutorOutOfDateException {
        Long pid = prolog();
        executionDelegate.completeTask(helper.getAuthorizedPerformerSubject(), executionDelegate.getTasks(helper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation()).get(0).getId(), STATE_ALIFA, helper
                .getAuthorizedPerformerActor().getId(), new HashMap<String, Object>());
        epilog(pid, STATE_KOCHAB, 1, 0);
    }

    public void testTimeout() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, VariablesValidationException {
        Long pid = prolog();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
        epilog(pid, STATE_ANWAR, 0, 1);
    }

    private Long prolog() throws AuthorizationException, AuthenticationException, ProcessDefinitionDoesNotExistException, VariablesValidationException {
        Long pid = executionDelegate.startProcessInstance(helper.getAuthorizedPerformerSubject(), WfServiceTestHelper.TIMER_PROCESS_NAME);
        assertEquals(STATE_ALIFA, executionDelegate.getTasks(helper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation()).get(0).getName());
        checkTasksCount(helper.getAuthorizedPerformerSubject(), 1);
        checkTasksCount(helper.getErpOperatorSubject(), 0);
        return pid;
    }

    private void epilog(Long pid, String stateName, int reqTasksCount, int erpTasksCount) throws AuthenticationException, AuthorizationException {
        List<TaskStub> list = executionDelegate.getTasks(helper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        for (TaskStub inst : list) {
            assertEquals(stateName, inst.getName());
        }
        //assertEquals (stateName, executionDelegate.getProcessInstanceTokens(
        //			helper.getAuthorizedPerformerSubject(), pid).get(1).getName());
        checkTasksCount(helper.getAuthorizedPerformerSubject(), reqTasksCount);
        checkTasksCount(helper.getErpOperatorSubject(), erpTasksCount);
    }

    private void checkTasksCount(Subject subject, int expected) throws InternalApplicationException, AuthorizationException, AuthenticationException {
        assertEquals(expected, executionDelegate.getTasks(subject, helper.getTaskBatchPresentation()).size());
    }
}
