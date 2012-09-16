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

import org.hibernate.Query;

import ru.runa.commons.dbpatch.DBPatchBase;

/**
 * Patch for insertion column IS_EXTERNAL into SUBSTITUTIONS table.
 */
public class SubstitutionExternalFlagAddition extends DBPatchBase {
    
    @Override
    public void applyPatch() {
        addColumn("SUBSTITUTIONS", new ColumnDef("IS_EXTERNAL", Types.BIT).setDefaultValue("0"));
        // update existing substitutions
        Query queryUpdateExternalFlagAll = session.createSQLQuery("update substitutions set IS_EXTERNAL=0");
        queryUpdateExternalFlagAll.executeUpdate();
    }
    
}
