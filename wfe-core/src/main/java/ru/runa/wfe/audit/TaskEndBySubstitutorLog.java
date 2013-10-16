package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * Logging task completion by substitution rules.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "S")
public class TaskEndBySubstitutorLog extends TaskEndLog {
    private static final long serialVersionUID = 1L;

    public TaskEndBySubstitutorLog() {
    }

    public TaskEndBySubstitutorLog(Task task, Executor executor) {
        super(task, executor);
    }

}
