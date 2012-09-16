package ru.runa.wf.dbpatch;

import java.sql.Types;

import ru.runa.commons.dbpatch.DBPatchBase;

/**
 * This patch fixes BLOB type to LONGBLOB for file storage tables.
 * Maximum allowed size for BLOB in MySQL is 65Kb.
 * Maximum allowed size for LONGBLOB in MySQL is 4Gb.
 * 
 * @author dofs
 */
public class MySQLExtendBlobs extends DBPatchBase {

	@Override
	protected void applyPatch() throws Exception {
		if (isMySQL()) {
			modifyColumn("jbpm_variableinstance", "bytes_", dialect.getTypeName(Types.VARBINARY));
			modifyColumn("jbpm_processfiles", "bytes_", dialect.getTypeName(Types.VARBINARY));
		}
	}

}
