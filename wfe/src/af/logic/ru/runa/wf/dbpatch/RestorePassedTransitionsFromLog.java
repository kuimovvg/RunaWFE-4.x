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

import ru.runa.bpm.graph.exe.PassedTransition;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.commons.dbpatch.DBPatchBase;

public class RestorePassedTransitionsFromLog extends DBPatchBase {

    public void applyPatch() {
        List<ColumnDef> columnDefinitions = new ArrayList<ColumnDef>();
        columnDefinitions.add(new ColumnDef("ID_", Types.BIGINT).setPrimaryKey());
        columnDefinitions.add(new ColumnDef("PROCESSINSTANCE_", Types.BIGINT));
        columnDefinitions.add(new ColumnDef("TRANSITION_", Types.BIGINT));
        createTable("JBPM_PASSTRANS", columnDefinitions, null);
        createIndex("IDX_PASSTRANS_TRANS", "JBPM_PASSTRANS", "TRANSITION_");
        createIndex("IDX_PASSTRANS_PRCINST", "JBPM_PASSTRANS", "PROCESSINSTANCE_");
        createForeignKey("FK_PASSTRANS_PROCINST", "JBPM_PASSTRANS", "PROCESSINSTANCE_", "JBPM_PROCESSINSTANCE", "ID_");
        createForeignKey("FK_PASSTRANS_TRANS", "JBPM_PASSTRANS", "TRANSITION_", "JBPM_TRANSITION", "ID_");

        Criteria criteria = session.createCriteria(TransitionLog.class);
        List<TransitionLog> transLogs = criteria.list();
        for (TransitionLog log : transLogs) {
            session.save(new PassedTransition(log.getToken().getProcessInstance(), log.getTransition()));
        }
    }
}
