package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.cache.CachingLogic;

public class AssignStalledTasks extends JobTask {
    private TaskAssigner taskAssigner;

    @Required
    public void setExecutor(TaskAssigner executor) {
        taskAssigner = executor;
    }

    @Override
    protected void execute() throws Exception {
        if (taskAssigner.areUnassignedTasksExist()) {
            try {
                taskAssigner.execute();
            } finally {
                CachingLogic.onTransactionComplete();
            }
        }
    }

}
