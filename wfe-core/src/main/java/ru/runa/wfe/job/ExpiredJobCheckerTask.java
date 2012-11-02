package ru.runa.wfe.job;

import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.job.dao.JobDAO;

public class ExpiredJobCheckerTask extends TimerTask {
    private static final Log log = LogFactory.getLog(ExpiredJobCheckerTask.class);

    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private JobExecutor jobExecutor;

    @Override
    public void run() {
        try {
            List<Job> jobs = jobDAO.getExpiredJobs();
            log.info("Expired jobs: " + jobs.size());
            if (jobs.size() == 0) {
                return;
            }
            try {
                CachingLogic.onTaskChange(null, null, null, null, null);
                for (Job job : jobs) {
                    try {
                        jobExecutor.executeJobs(job.getId());
                    } catch (Exception e) {
                        log.error("Error execute job " + job, e);
                    }
                }
            } finally {
                CachingLogic.onTransactionComplete();
            }
        } catch (Throwable th) {
            log.error("timer error", th);
        }
    }

}
