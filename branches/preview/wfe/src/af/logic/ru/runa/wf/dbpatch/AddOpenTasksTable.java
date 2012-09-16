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

public class AddOpenTasksTable extends DBPatchBase {
    
    @Override
    public void applyPatch() {
        List<ColumnDef> columnDefinitions = new ArrayList<DBPatchBase.ColumnDef>();
        columnDefinitions.add(new ColumnDef("ID", Types.BIGINT).setPrimaryKey());
        columnDefinitions.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        columnDefinitions.add(new ColumnDef("TASK_ID", Types.BIGINT, false));
        createTable("EXECUTOR_OPEN_TASKS", columnDefinitions, "(EXECUTOR_ID, TASK_ID)");

        createIndex("EXTSK_EXEC_ID_IDX", "EXECUTOR_OPEN_TASKS", "EXECUTOR_ID");
        createIndex("EXTSK_TASK_ID_IDX", "EXECUTOR_OPEN_TASKS", "TASK_ID");

        createForeignKey("FK899B60256F8BB3C4", "EXECUTOR_OPEN_TASKS", "TASK_ID", "JBPM_TASKINSTANCE", "ID_");
        createForeignKey("FK899B602553B7236", "EXECUTOR_OPEN_TASKS", "EXECUTOR_ID", "EXECUTORS", "ID");
    }
    
}
