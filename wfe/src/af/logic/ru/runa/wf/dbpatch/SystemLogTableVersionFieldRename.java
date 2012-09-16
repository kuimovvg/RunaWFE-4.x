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

import ru.runa.commons.dbpatch.DBPatchBase;

public class SystemLogTableVersionFieldRename extends DBPatchBase {

    @Override
    public void applyPatch() {
        if (isOracle()) {
            List<ColumnDef> columnDefinitions = new ArrayList<DBPatchBase.ColumnDef>();
            columnDefinitions.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
            columnDefinitions.add(new ColumnDef("LOG_TYPE", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255), false));
            columnDefinitions.add(new ColumnDef("PROCESS_INSTANCE", Types.BIGINT));
            columnDefinitions.add(new ColumnDef("PROCESS_DEFINITION_NAME", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255)));
            columnDefinitions.add(new ColumnDef("PROCESS_DEFINITION_VERSION", Types.BIGINT));
            columnDefinitions.add(new ColumnDef("PROCESS_DEFINITION_PI_EXIST", Types.BIT));
            columnDefinitions.add(new ColumnDef("PROCESS_DEFINITION_LAST_VER", Types.BIT));
            columnDefinitions.add(new ColumnDef("ACTOR_CODE", Types.BIGINT, false));
            columnDefinitions.add(new ColumnDef("TIME", Types.TIMESTAMP, false));
            createTable("SYSTEM_LOG", columnDefinitions, null);
            return;
        }
        renameColumn("SYSTEM_LOG", "PROCESS_DEFINITION_LAST_VERSION", new ColumnDef("PROCESS_DEFINITION_LAST_VER", Types.BIT));
    }
    
}
