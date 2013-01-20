package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddHierarchyProcess extends DBPatch {
    private static final String DELIM = "/";

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("JBPM_PROCESSINSTANCE", new ColumnDef("TREE_PATH", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

    @Override
    public void applyPatch(Session session) {
        List<Object[]> list = session.createSQLQuery("SELECT ID_, SUPERPROCESSTOKEN_ FROM JBPM_PROCESSINSTANCE").list();
        for (Object[] results : list) {
            Long processId = ((Number) results[0]).longValue();
            StringBuilder hierarchy = new StringBuilder();
            hierarchy.append(processId);
            appendParentsProcessId(session, results[1], hierarchy);
            String q = "UPDATE JBPM_PROCESSINSTANCE SET TREE_PATH = '" + hierarchy.toString() + "' WHERE ID_ = " + processId;
            session.createSQLQuery(q).executeUpdate();
            log.debug("updated process instance " + processId);
        }
    }

    private void appendParentsProcessId(Session session, Object superProcessTokenId, StringBuilder hierarchy) {
        if (superProcessTokenId != null) {
            Number processId = (Number) session.createSQLQuery("SELECT PROCESSINSTANCE_ FROM JBPM_TOKEN WHERE ID_=" + superProcessTokenId)
                    .uniqueResult();
            hierarchy.insert(0, DELIM);
            hierarchy.insert(0, processId);
            superProcessTokenId = session.createSQLQuery("SELECT SUPERPROCESSTOKEN_ FROM JBPM_PROCESSINSTANCE WHERE ID_=" + processId).uniqueResult();
            appendParentsProcessId(session, superProcessTokenId, hierarchy);
        }
    }

}
