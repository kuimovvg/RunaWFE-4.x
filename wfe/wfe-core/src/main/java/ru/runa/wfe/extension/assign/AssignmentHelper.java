package ru.runa.wfe.extension.assign;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;

public class AssignmentHelper {
    private static final Log log = LogFactory.getLog(AssignmentHelper.class);

    public static void reassignTask(ExecutionContext executionContext, Task task, Executor newExecutor, boolean reassignSwimlane) {
        Executor oldExecutor = task.getExecutor();
        task.assignExecutor(executionContext, newExecutor, reassignSwimlane);
        removeIfTemporaryGroup(oldExecutor);
    }

    public static void removeIfTemporaryGroup(Executor oldExecutor) {
        if (oldExecutor instanceof TemporaryGroup) {
            ApplicationContextFactory.getExecutorLogic().remove(oldExecutor);
        }
    }

    public static void assign(ExecutionContext executionContext, Assignable assignable, Collection<? extends Executor> executors) {
        try {
            if (executors == null || executors.size() == 0) {
                log.warn("Assigning null executor in " + executionContext + ": " + assignable + ", check swimlane initializer");
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
            } else if (executionContext.getTask().getSwimlane() != null) {
                swimlaneName = executionContext.getTask().getSwimlane().getName();
            } else {
                log.warn("Unable to get swimlane name; assignment postponed in " + assignable);
                return;
            }
            Group tmpGroup = TemporaryGroup.create(executionContext.getProcess().getId(), swimlaneName);
            ApplicationContextFactory.getExecutorLogic().saveTemporaryGroup(tmpGroup, executors);
            assignable.assignExecutor(executionContext, tmpGroup, true);
        } catch (Exception e) {
            log.warn("Unable to assign " + assignable + " in " + executionContext.getProcess(), e);
        }
    }
    
    public static void assignSwimlane(ExecutionContext executionContext, String swimlaneName, String swimlaneInitializer) {
        List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(swimlaneInitializer, executionContext.getVariableProvider());
        SwimlaneDefinition swimlaneDefinition = executionContext.getProcessDefinition().getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = executionContext.getProcess().getSwimlaneNotNull(swimlaneDefinition);
        assign(executionContext, swimlane, executors);
    }


}
