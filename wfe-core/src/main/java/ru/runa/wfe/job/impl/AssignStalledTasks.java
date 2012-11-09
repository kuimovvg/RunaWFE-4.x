package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Required;

public class AssignStalledTasks extends TransactionalTaskBase {
    private TaskAssigner executor;

    @Required
    public void setExecutor(TaskAssigner executor) {
        this.executor = executor;
    }

    @Override
    protected void doExecute() {
        executor.execute();
    }

}
