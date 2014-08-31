package ru.runa.wfe.extension.assign;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.logic.ExecutorLogic;

public class AssignmentHelper {
    private static final Log log = LogFactory.getLog(AssignmentHelper.class);
    @Autowired
    private ExecutorLogic executorLogic;

    public void reassignTask(ExecutionContext executionContext, Task task, Executor newExecutor, boolean reassignSwimlane) {
        Executor oldExecutor = task.getExecutor();
        task.assignExecutor(executionContext, newExecutor, reassignSwimlane);
        removeIfTemporaryGroup(oldExecutor);
    }

    public void removeIfTemporaryGroup(Executor oldExecutor) {
        if (oldExecutor instanceof TemporaryGroup) {
            try {
                executorLogic.remove(oldExecutor);
            } catch (ExecutorParticipatesInProcessesException e) {
                // will be removed at process end
                log.debug(e);
            }
        }
    }

    public void assign(ExecutionContext executionContext, Assignable assignable, Collection<? extends Executor> executors) {
        try {
            if (executors == null || executors.size() == 0) {
                log.warn("Assigning null executor in " + executionContext + ": " + assignable + ", check swimlane initializer");
                assignable.assignExecutor(executionContext, null, true);
                ProcessExecutionException pee = new ProcessExecutionException(assignable.getErrorMessageKey(), assignable.getName());
                ProcessExecutionErrors.addProcessError(executionContext.getProcess().getId(), assignable.getName(), assignable.getName(), null, pee);
                return;
            }
            ProcessExecutionErrors.removeProcessError(executionContext.getProcess().getId(), assignable.getName());
            if (executors.size() == 1) {
                Executor aloneExecutor = (executors.iterator().next());
                assignable.assignExecutor(executionContext, aloneExecutor, true);
                return;
            }
            String swimlaneName = assignable.getSwimlaneName();
            Group tmpGroup = TemporaryGroup.create(executionContext.getProcess().getId(), swimlaneName);
            executorLogic.saveTemporaryGroup(tmpGroup, executors);
            assignable.assignExecutor(executionContext, tmpGroup, true);
            log.info("Cascaded assignment done in " + assignable);
        } catch (Exception e) {
            log.warn("Unable to assign " + assignable + " in " + executionContext.getProcess(), e);
        }
    }

}
