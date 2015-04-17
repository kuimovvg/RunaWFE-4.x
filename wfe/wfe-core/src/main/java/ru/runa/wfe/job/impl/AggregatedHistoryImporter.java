package ru.runa.wfe.job.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.dao.ProcessLogAwareDao;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.dao.Constant;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class AggregatedHistoryImporter extends TransactionalExecutor {

    private static final Log log = LogFactory.getLog(AggregatedHistoryImporter.class);
    /**
     * Constant name to store process id
     */
    private final String importFromConstantName = "AggregatedProcessLogAwareDao.ImportFromId";

    @Autowired
    private ProcessLogAwareDao processLogAwareDao;

    @Autowired
    private ConstantDAO constantDao;

    @Autowired
    private ProcessLogDAO processLogDao;

    @Autowired
    private ProcessDAO processDao;

    @Override
    protected void doExecuteInTransaction() throws Exception {
        long processId = getProcessIdToImport();
        if (processId <= 0) {
            return;
        }
        log.info("Importing logs for process " + processId + " into aggregated logs.");
        Process process = processDao.get(processId);
        Map<Long, Token> tokens = fillTokensMap(process);
        List<ProcessLog> logs = processLogDao.getAll(processId);
        for (ProcessLog log : logs) {
            processLogAwareDao.addLog(log, process, tokens.get(log.getTokenId()));
        }
        SaveProcessIdToImport(processId);
    }

    private Map<Long, Token> fillTokensMap(Process process) {
        Map<Long, Token> tokens = Maps.newHashMap();
        addTokenToMap(process.getRootToken(), tokens);
        return tokens;
    }

    private void addTokenToMap(Token token, Map<Long, Token> tokens) {
        tokens.put(token.getId(), token);
        for (Token child : token.getChildren()) {
            addTokenToMap(child, tokens);
        }
    }

    private long getProcessIdToImport() {
        Constant importFromSettings = constantDao.get(importFromConstantName);
        if (importFromSettings == null || Strings.isNullOrEmpty(importFromSettings.getValue())) {
            DetachedCriteria criteria = DetachedCriteria.forClass(Process.class).addOrder(Order.desc("id"));
            List<Process> processes = processDao.getHibernateTemplate().findByCriteria(criteria, 0, 1);
            long processId = 0;
            if (processes != null && !processes.isEmpty()) {
                processId = processes.get(0).getId();
            }
            constantDao.create(new Constant(importFromConstantName, String.valueOf(processId)));
            importFromSettings = constantDao.get(importFromConstantName);
        }
        long processId = Long.parseLong(importFromSettings.getValue());
        return processId;
    }

    private void SaveProcessIdToImport(long processId) {
        Constant importFromSettings = constantDao.get(importFromConstantName);
        if (importFromSettings == null) {
            constantDao.create(new Constant(importFromConstantName, String.valueOf(processId)));
            importFromSettings = constantDao.get(importFromConstantName);
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(Process.class).addOrder(Order.desc("id"));
        criteria.add(Restrictions.lt("id", processId));
        List<Process> processes = processDao.getHibernateTemplate().findByCriteria(criteria, 0, 1);
        if (processes != null && !processes.isEmpty()) {
            importFromSettings.setValue(String.valueOf(processes.get(0).getId()));
        } else {
            importFromSettings.setValue(String.valueOf(0));
        }
        constantDao.update(importFromSettings);
    }
}
