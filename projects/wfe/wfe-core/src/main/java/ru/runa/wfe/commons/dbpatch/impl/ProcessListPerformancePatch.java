package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.security.SecuredObjectType;

import com.google.common.collect.Lists;

public class ProcessListPerformancePatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLCreateColumn("PERMISSION_MAPPING", new ColumnDef("TYPE_ID", Types.BIGINT)));
        sql.add(getDDLRemoveColumn("PERMISSION_MAPPING", "VERSION"));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLRemoveIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IX_PERMISSION_TYPE_ID", "TYPE_ID"));
        sql.add(getDDLRemoveColumn("PERMISSION_MAPPING", "TYPE"));
        sql.add(getDDLCreateIndex("BPM_VARIABLE", "IX_VARIABLE_NAME", "NAME"));
        sql.add(getDDLCreateIndex("BPM_VARIABLE", "IX_VARIABLE_VALUE", "STRINGVALUE"));
        String createIndex = "CREATE NONCLUSTERED INDEX IX_PERMISSION_INDEX ON PERMISSION_MAPPING (MASK,TYPE_ID,EXECUTOR_ID) INCLUDE (IDENTIFIABLE_ID)";
        if (dbType == DBType.MSSQL) {
            sql.add(createIndex);
        } else {
            log.warn("Index does not created; create it yourself");
            System.out.println("MSSQL syntax: " + createIndex);
            if (!skipDatabaseSpecificDDL()) {
                throw new InternalApplicationException(
                        "Patch cannot create index for your dialect; check log for index syntax, create it yourself and run WFE with option -Dpatch.skip.advanced.ddl");
            }
        }
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        for (SecuredObjectType type : SecuredObjectType.values()) {
            String q = "UPDATE PERMISSION_MAPPING SET TYPE_ID=" + type.ordinal() + " WHERE TYPE='" + type.name() + "'";
            log.info("Updated permission mappings (" + type + "): " + session.createSQLQuery(q).executeUpdate());
        }
    }

}
