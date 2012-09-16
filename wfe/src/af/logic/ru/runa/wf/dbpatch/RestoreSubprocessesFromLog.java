/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.dbpatch;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;

import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.commons.dbpatch.DBPatchBase;

public class RestoreSubprocessesFromLog extends DBPatchBase {

    public void applyPatch() {
        List<ColumnDef> columnDefinitions = new ArrayList<ColumnDef>();
        columnDefinitions.add(new ColumnDef("ID_", Types.BIGINT).setPrimaryKey());
        columnDefinitions.add(new ColumnDef("PROCESSINSTANCE_", Types.BIGINT));
        columnDefinitions.add(new ColumnDef("SUBPROCESSINSTANCE_", Types.BIGINT));
        columnDefinitions.add(new ColumnDef("NODE_", Types.BIGINT));
        createTable("JBPM_NODE_SUBPROC", columnDefinitions, null);
        createIndex("IDX_NODE_SUBPROC_NODE", "JBPM_NODE_SUBPROC", "NODE_");
        createIndex("IDX_NODE_SUBPROC_PROCINST", "JBPM_NODE_SUBPROC", "PROCESSINSTANCE_");
        createIndex("IDX_NODE_SUBPROC_SUBPROCINST", "JBPM_NODE_SUBPROC", "SUBPROCESSINSTANCE_");
        createForeignKey("FK_NODE_SUBPROC_SUBPROCINST", "JBPM_NODE_SUBPROC", "SUBPROCESSINSTANCE_", "JBPM_PROCESSINSTANCE", "ID_");
        createForeignKey("FK_NODE_SUBPROC_PROCINST", "JBPM_NODE_SUBPROC", "PROCESSINSTANCE_", "JBPM_PROCESSINSTANCE", "ID_");

        Criteria criteria = session.createCriteria(ProcessStateLog.class);
        List<ProcessStateLog> logs = criteria.list();
        for (ProcessStateLog log : logs) {
            session.save(new StartedSubprocesses(log.getToken().getProcessInstance(), log.getSubProcessInstance(), log.getNode()));
        }
    }
}
