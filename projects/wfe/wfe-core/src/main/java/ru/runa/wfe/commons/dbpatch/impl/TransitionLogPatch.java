package ru.runa.wfe.commons.dbpatch.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Maps;

public class TransitionLogPatch extends DBPatch {

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private ProcessLogDAO processLogDAO;

    @Override
    protected void applyPatch(Session session) throws Exception {
        String q;
        List<Object[]> list;
        // JBPM_PASSTRANS
        q = "DELETE FROM JBPM_PASSTRANS WHERE TRANSITION_ID IS NULL OR NODE_ID IS NULL OR PROCESS_ID IS NULL";
        log.info("Deleted bad JBPM_PASSTRANS " + session.createSQLQuery(q).executeUpdate());

        q = "SELECT PROCESS_ID, NODE_ID, TRANSITION_ID FROM JBPM_PASSTRANS ORDER BY ID ASC";
        list = session.createSQLQuery(q).list();
        int failed = 0;
        int success = 0;
        Map<Deployment, Date> failedDeployments = Maps.newHashMap();
        for (Object[] objects : list) {
            Process process = processDAO.get(((Number) objects[0]).longValue());
            Deployment deployment = process.getDeployment();
            try {
                ProcessDefinition definition = processDefinitionLoader.getDefinition(deployment.getId());
                try {
                    Node node = definition.getNodeNotNull((String) objects[1]);
                    Transition transition = node.getLeavingTransitionNotNull((String) objects[2]);
                    TransitionLog transitionLog = new TransitionLog(transition);
                    transitionLog.setProcessId(process.getId());
                    // TODO where to get it?
                    transitionLog.setTokenId(process.getRootToken().getId());
                    transitionLog.setDate(new Date());
                    processLogDAO.create(transitionLog);
                    success++;
                } catch (Exception e) {
                    log.warn(e);
                    failed++;
                }
            } catch (InvalidDefinitionException e) {
                if (!failedDeployments.containsKey(deployment)) {
                    failedDeployments.put(deployment, process.getEndDate());
                    log.error("Unable to restore history for " + deployment + ": " + e);
                } else {
                    Date endDate = failedDeployments.get(deployment);
                    if (endDate != null && (process.getEndDate() == null || endDate.before(process.getEndDate()))) {
                        failedDeployments.put(deployment, process.getEndDate());
                    }
                }
            }
        }
        log.info("-------------------- RESULT OF " + getClass());
        for (Map.Entry<Deployment, Date> entry : failedDeployments.entrySet()) {
            log.warn("Unparsed definition " + entry.getKey() + ", last process end date = " + entry.getValue());
        }
        log.info("Reverted history [for parsed definitions] result: success " + success + ", failed " + failed);
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLRemoveTable("JBPM_PASSTRANS"));
        return sql;
    }
}
