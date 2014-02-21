package ru.runa.wfe.job.impl;


public class AssignStalledTasks extends JobTask<TaskAssigner> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }

}
