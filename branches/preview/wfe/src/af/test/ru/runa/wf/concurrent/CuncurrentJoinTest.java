package ru.runa.wf.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Permission;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Check if system behavior is incorrect :).
 */
public class CuncurrentJoinTest extends ServletTestCase {

    /**
     * Main process (with fork/join and subprocess start) par file.
     */
    private static String MainProcess = "Concurrent_Main.par";

    /**
     * Subprocess par file.
     */
    private static String SubProcess = "Concurrent_Sub.par";

    /**
     * Delegate to execute processes.
     */
    private ExecutionService executionService;

    /**
     * Test helper.
     */
    private WfServiceTestHelper helper = null;

    /**
     * Awaiting on this semaphore task completition.
     */
    private final Semaphore semaphore = new Semaphore(1);

    public static Test suite() {
        return new TestSuite(CuncurrentJoinTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        executionService = DelegateFactory.getInstance().getExecutionService();
        helper = new WfServiceTestHelper(getClass().getName());
        helper.deployValidProcessDefinition(MainProcess);
        helper.deployValidProcessDefinition(SubProcess);
        Collection<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                ProcessDefinitionPermission.READ_STARTED_INSTANCE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, "Main");
    }

    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition("Main");
        helper.undeployValidProcessDefinition("Sub");
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testConcurrentTaskExecution() throws Exception {
        executionService.startProcessInstance(helper.getAuthorizedPerformerSubject(), "Main");
        final List<TaskStub> tasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(2, tasks.size());
        new Thread(new Runnable() {
            public void run() {
                try {
                    semaphore.acquire();
                    TaskStub task = tasks.get(0).getName().contains("1") ? tasks.get(0) : tasks.get(1);
                    executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), helper
                            .getAuthorizedPerformerActor().getId(), new HashMap<String, Object>());
                } catch (Exception e) {
                }
                semaphore.release();
            }
        }).start();
        TaskStub task = tasks.get(0).getName().contains("1") ? tasks.get(1) : tasks.get(0);
        executionService.completeTask(helper.getAuthorizedPerformerSubject(), task.getId(), task.getName(), helper.getAuthorizedPerformerActor()
                .getId(), new HashMap<String, Object>());
        semaphore.acquire();
        List<TaskStub> newTasks = executionService.getTasks(helper.getAuthorizedPerformerSubject(),
                WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(1, newTasks.size());
        assertTrue(newTasks.get(0).getName().contains("1"));
        assertEquals("Main", newTasks.get(0).getProcessDefinitionName());
    }
}
