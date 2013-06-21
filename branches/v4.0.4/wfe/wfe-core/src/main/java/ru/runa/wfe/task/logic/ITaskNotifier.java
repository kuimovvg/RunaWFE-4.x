package ru.runa.wfe.task.logic;

import ru.runa.wfe.execution.ExecutionContext;


/**
 * Interface for new tasks notification through third-party application.
 * 
 * @author Dofs
 */
public interface ITaskNotifier {

    public void onNewTask(ExecutionContext executionContext) throws Exception;

}
