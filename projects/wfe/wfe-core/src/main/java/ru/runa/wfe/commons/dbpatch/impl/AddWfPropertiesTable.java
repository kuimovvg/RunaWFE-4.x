package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddWfPropertiesTable extends DBPatch {

	@Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        List<ColumnDef> columns = new LinkedList<DBPatch.ColumnDef>();
        ColumnDef id = new ColumnDef("ID", dialect.getTypeName(Types.INTEGER, 1024, 1024, 1024), false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("FILE_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("VALUE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        sql.add(getDDLCreateTable("WFE_PROPERTIES", columns, null));
        return sql;
	}
	
	@Override
	protected void applyPatch(Session session) throws Exception {	}

}
