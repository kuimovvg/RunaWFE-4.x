package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ExecutionServiceDelegateTimerTest extends ServletTestCase {

    private static final String STATE_KOCHAB = "Kochab";

    private static final String STATE_ANWAR = "Anwar";

    private static final String STATE_ALIFA = "Alifa";

    private ExecutionService executionService = null;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        helper.deployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME + ".par");
        Collection<Permission> permissions = Lists.newArrayList(DefinitionPermission.START_PROCESS,
                DefinitionPermission.READ_STARTED_PROCESS);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.TIMER_PROCESS_NAME);

        helper.addExecutorToGroup(helper.getAuthorizedPerformerActor(), helper.getBossGroup());

        executionService = helper.getExecutionService();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition(WfServiceTestHelper.TIMER_PROCESS_NAME);
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void test() throws InternalApplicationException {
        Long pid = prolog();
        executionService.completeTask(
                helper.getAuthorizedPerformerUser(),
                executionService
                        .getTasks(helper.getAuthorizedPerformerUser(),
                                BatchPresentationFactory.TASKS.createDefault()).get(0).getId(),
                new HashMap<String, Object>(), null);
        epilog(pid, STATE_KOCHAB, 1, 0);
    }

    public void testTimeout() throws InternalApplicationException {
        Long pid = prolog();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }

        // TODO fix me!!! epilog(pid, STATE_ANWAR, 0, 1);
    }

    private Long prolog() throws AuthorizationException, AuthenticationException, DefinitionDoesNotExistException,
            ValidationException {
        Long pid = executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.TIMER_PROCESS_NAME, null);
        assertEquals(
                STATE_ALIFA,
                executionService
                        .getTasks(helper.getAuthorizedPerformerUser(),
                                BatchPresentationFactory.TASKS.createDefault()).get(0).getName());
        checkTasksCount(helper.getAuthorizedPerformerUser(), 1);
        checkTasksCount(helper.getErpOperatorUser(), 0);
        return pid;
    }

    private void epilog(Long pid, String stateName, int reqTasksCount, int erpTasksCount) throws AuthenticationException, AuthorizationException {
        List<WfTask> list = executionService.getTasks(helper.getAuthorizedPerformerUser(),
                BatchPresentationFactory.TASKS.createDefault());
        for (WfTask inst : list) {
            assertEquals(stateName, inst.getName());
        }
        // assertEquals (stateName, executionService.getProcessInstanceTokens(
        // helper.getAuthorizedPerformerUser(), pid).get(1).getName());
        checkTasksCount(helper.getAuthorizedPerformerUser(), reqTasksCount);
        checkTasksCount(helper.getErpOperatorUser(), erpTasksCount);
    }

    private void checkTasksCount(User user, int expected) throws InternalApplicationException {
        assertEquals(expected, executionService.getTasks(user, helper.getTaskBatchPresentation()).size());
    }
}
