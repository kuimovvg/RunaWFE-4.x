package ru.runa.wfe.job.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ProcessDefinition;

import com.google.common.base.Throwables;

public class JobExecutor {
    private static Log log = LogFactory.getLog(JobExecutor.class);
    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeJob(Long id) {
        Job job = null;
        try {
            job = jobDAO.getNotNull(id);
            log.debug("executing " + job);
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(job.getProcess().getDeployment().getId());
            ExecutionContext executionContext = new ExecutionContext(processDefinition, job.getToken());
            job.execute(executionContext);
        } catch (Exception e) {
            log.error("Error execute job " + job, e);
            // for rollback
            throw Throwables.propagate(e);
        }
    }

}
