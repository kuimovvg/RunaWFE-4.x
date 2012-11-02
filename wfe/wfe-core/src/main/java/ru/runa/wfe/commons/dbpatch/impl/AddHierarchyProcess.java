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
        sql.add(getDDLAddColumn("JBPM_PROCESSINSTANCE", new ColumnDef("TREE_PATH", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))));
        return sql;
    }

    @Override
    public void applyPatch(Session session) {
        // List<Number> results = session.createSQLQuery("SELECT ID_ FROM JBPM_PROCESSINSTANCE").list();
        // for (Number id : results) {
        // Long processId = id.longValue();
        // Process process = (Process) session.load(Process.class, processId);
        // if (process != null) {
        // StringBuilder hierarchy = new StringBuilder();
        // hierarchy.append(process.getId());
        // appendParentsProcessId(process.getSuperProcessToken(), hierarchy);
        // String q = "UPDATE JBPM_PROCESSINSTANCE SET HIERARCHYSUBPROCESS_ = '" + hierarchy.toString() + "' WHERE ID_ = " + processId;
        // session.createSQLQuery(q).executeUpdate();
        // log.debug("updated process instance " + processId);
        // }
        // }
    }

    // private void appendParentsProcessId(Token superToken, StringBuilder hierarchy) {
    // if (superToken != null) {
    // insertProcessIdToHierarchy(hierarchy, superToken.getProcess().getId());
    // while (superToken.getProcess().getSuperProcessToken() != null) {
    // superToken = superToken.getProcess().getSuperProcessToken();
    // insertProcessIdToHierarchy(hierarchy, superToken.getProcess().getId());
    // }
    // }
    // }
    //
    // private void insertProcessIdToHierarchy(StringBuilder hierarchy, Long id) {
    // hierarchy.insert(0, DELIM);
    // hierarchy.insert(0, id);
    // }

}
