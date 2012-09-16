package ru.runa.wf.dbpatch;

import java.util.List;

import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.commons.dbpatch.DBPatchBase;

public class AddHierarchyProcessInstance extends DBPatchBase {
    private static final String DELIM = "/";

    @Override
    public void applyPatch() {
        addColumn("JBPM_PROCESSINSTANCE", new ColumnDef("HIERARCHYSUBPROCESS_", dialect.getTypeName(java.sql.Types.VARCHAR, 1024, 1024, 1024)));

        List<Number> results = session.createSQLQuery("SELECT ID_ FROM JBPM_PROCESSINSTANCE").list();
        for (Number id : results) {
            Long processId = id.longValue();
            ProcessInstance processInstance = (ProcessInstance) session.load(ProcessInstance.class, processId);
            if (processInstance != null) {
                StringBuilder hierarchy = new StringBuilder();
                hierarchy.append(processInstance.getId());
                appendParentsProcessId(processInstance.getSuperProcessToken(), hierarchy);
                applyHierarchyValueToDB(hierarchy.toString(), processId);
                log.info("updated process instance");
            }
        }
    }

    private void appendParentsProcessId(Token superToken, StringBuilder hierarchy) {
        if (superToken != null) {
            insertProcessIdToHierarchy(hierarchy, superToken.getProcessInstance().getId());
            while (superToken.getProcessInstance().getSuperProcessToken() != null) {
                superToken = superToken.getProcessInstance().getSuperProcessToken();
                insertProcessIdToHierarchy(hierarchy, superToken.getProcessInstance().getId());
            }
        }
    }

    private void insertProcessIdToHierarchy(StringBuilder hierarchy, Long id) {
        hierarchy.insert(0, DELIM);
        hierarchy.insert(0, id);
    }

    private void applyHierarchyValueToDB(String hierarchyValue, Long processId) {
        session.createSQLQuery("UPDATE JBPM_PROCESSINSTANCE SET HIERARCHYSUBPROCESS_ = '" + hierarchyValue + "' WHERE ID_ = " + processId)
                .executeUpdate();
    }

}
