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
package ru.runa.af.dbpatch;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import ru.runa.commons.dbpatch.DBPatchBase;

public class SubstitutionCriteriaPatch extends DBPatchBase {
    
    public void applyPatch() {
        createSequence("SEQ_SUBSTITUTION_CRITERIAS");
        addColumn("SUBSTITUTIONS", new ColumnDef("CRITERIA_ID", Types.BIGINT));
        createIndex("CRITERIA_ID_IDX", "SUBSTITUTIONS", "CRITERIA_ID");
        
        List<ColumnDef> columnDefinitions = new ArrayList<ColumnDef>();
        columnDefinitions.add(new ColumnDef("ID", Types.BIGINT).setPrimaryKey());
        columnDefinitions.add(new ColumnDef("TYPE", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255)));
        columnDefinitions.add(new ColumnDef("NAME", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255), false));
        columnDefinitions.add(new ColumnDef("CONF", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255)));
        createTable("SUBSTITUTION_CRITERIAS", columnDefinitions, "(TYPE, CONF)");
        if (isMySQL()) {
            Query query = session.createSQLQuery("ALTER TABLE SUBSTITUTION_CRITERIAS ALTER COLUMN ID RESTART WITH 1");
            query.executeUpdate();
        }
        createForeignKey("FKB04074F6FE444BF9", "SUBSTITUTIONS", "CRITERIA_ID", "SUBSTITUTION_CRITERIAS", "ID");

        for (Object objQueries : session.createSQLQuery("SELECT ID, CRITERIA FROM SUBSTITUTIONS").list()) {
            Object[] objQuery = (Object[]) objQueries;
            String sqlSelect = "SELECT ID FROM SUBSTITUTION_CRITERIAS WHERE TYPE = 'swimlane' AND CONF = '" + objQuery[1] + "'";
            if (!"substitution.always".equals(objQuery[1]) && objQuery[1] != null) {
                if (session.createSQLQuery(sqlSelect).list().isEmpty()) {
                    String sqlInsert;
                    if (isOracle()) {
                        sqlInsert = "INSERT INTO SUBSTITUTION_CRITERIAS (ID, TYPE, NAME, CONF) VALUES (SEQ_SUBSTITUTION_CRITERIAS.NEXTVAL, 'swimlane', '"
                                + objQuery[1] + "', '" + objQuery[1] + "')";
                    } else {
                        sqlInsert = "INSERT INTO SUBSTITUTION_CRITERIAS (TYPE, NAME, CONF) VALUES ('swimlane', '" + objQuery[1] + "', '"
                                + objQuery[1] + "')";
                    }
                    session.createSQLQuery(sqlInsert).executeUpdate();
                }
                long criteriaId = ((Number) session.createSQLQuery(sqlSelect).uniqueResult()).longValue();
                session.createSQLQuery("UPDATE SUBSTITUTIONS SET CRITERIA_ID = " + criteriaId + " WHERE ID = " + objQuery[0]).executeUpdate();
            }
        }
        removeColumn("SUBSTITUTIONS", "CRITERIA");
    }
}
