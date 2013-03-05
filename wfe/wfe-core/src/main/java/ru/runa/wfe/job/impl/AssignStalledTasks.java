package ru.runa.wfe.job.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.cache.CachingLogic;

public class AssignStalledTasks extends TimerTask {
    private static final Log log = LogFactory.getLog(AssignStalledTasks.class);
    private TaskAssigner taskAssigner;

    @Required
    public void setExecutor(TaskAssigner executor) {
        taskAssigner = executor;
    }

    @Override
    public final void run() {
        try {
            if (taskAssigner.areUnassignedTasksExist()) {
                try {
                    taskAssigner.execute();
                } finally {
                    CachingLogic.onTransactionComplete();
                }
            }
        } catch (Throwable th) {
            log.error("timer task error", th);
        }
    }

}
