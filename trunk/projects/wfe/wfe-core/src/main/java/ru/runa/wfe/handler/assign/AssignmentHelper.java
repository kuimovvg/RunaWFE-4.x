package ru.runa.wfe.handler.assign;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
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
        if (oldExecutor instanceof TemporaryGroup) {
            executorLogic.remove(oldExecutor);
        }
    }

    public void assignSwimlane(ExecutionContext executionContext, Assignable assignable, Collection<? extends Executor> executors) {
        try {
            // TODO backwardCompatibilitySwimlanes
            if (executors.size() == 1) {
                Executor aloneExecutor = (executors.iterator().next());
                assignable.assignExecutor(executionContext, aloneExecutor, true);
                return;
            }
            if (executionContext.getTask().getSwimlane() == null) {
                return;
            }
            Long processId = executionContext.getProcess().getId();
            String swimlaneName = executionContext.getTask().getSwimlane().getName();
            Group tmpGroup = TemporaryGroup.create(processId + "_" + swimlaneName);
            executorLogic.saveTemporaryGroup(tmpGroup, executors);
            assignable.assignExecutor(executionContext, tmpGroup, true);
        } catch (Exception e) {
            log.warn("Unable to assign in process id = " + executionContext.getProcess().getId(), e);
        }
    }

}
