package ru.runa.wfe.extension.assign;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.extension.Assignable;
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
        removeIfTemporaryGroup(oldExecutor);
    }

    public void removeIfTemporaryGroup(Executor oldExecutor) {
        if (oldExecutor instanceof TemporaryGroup) {
            executorLogic.remove(oldExecutor);
        }
    }

    public void assignSwimlane(ExecutionContext executionContext, Assignable assignable, Collection<? extends Executor> executors) {
        try {
            if (executors.size() == 0) {
                log.warn("Assigning null executor in " + assignable + ", check swimlane initializer");
                // TODO display in ProcessErrors
                assignable.assignExecutor(executionContext, null, true);
                return;
            }
            if (executors.size() == 1) {
                Executor aloneExecutor = (executors.iterator().next());
                assignable.assignExecutor(executionContext, aloneExecutor, true);
                return;
            }
            String swimlaneName;
            if (assignable instanceof Swimlane) {
                swimlaneName = ((Swimlane) assignable).getName();
            } else if (executionContext.getTask().getSwimlane() == null) {
                swimlaneName = executionContext.getTask().getSwimlane().getName();
            } else {
                log.debug("Unable to get swimlane name; assignment postponed in " + assignable);
                return;
            }
            Group tmpGroup = TemporaryGroup.create(executionContext.getProcess().getId(), swimlaneName);
            executorLogic.saveTemporaryGroup(tmpGroup, executors);
            assignable.assignExecutor(executionContext, tmpGroup, true);
        } catch (Exception e) {
            log.warn("Unable to assign " + assignable + " in " + executionContext.getProcess(), e);
        }
    }

}
