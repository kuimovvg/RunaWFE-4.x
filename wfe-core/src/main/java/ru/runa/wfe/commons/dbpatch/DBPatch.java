package ru.runa.wfe.commons.dbpatch;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Interface for database patch (Applied during version update).
 * 
 * @author Dofs
 */
public abstract class DBPatch {
    protected Log log = LogFactory.getLog(getClass());
    protected final Dialect dialect = ApplicationContextFactory.getDialect();
    protected final DBType dbType = ApplicationContextFactory.getDBType();

    protected boolean skipDatabaseSpecificDDL() {
        return "true".equals(System.getProperty("patch.skip.advanced.ddl"));
    }

    /**
     * Execute patch DDL statements before DML (non-transacted mode in most
     * databases).
     */
    public final void executeDDLBefore(boolean inTransaction) throws Exception {
        executeDDL("[DDLBefore]", getDDLQueriesBefore(), inTransaction);
    }

    protected List<String> getDDLQueriesBefore() {
        return Lists.newArrayList();
    }

    /**
     * Execute patch DML statements (in one transaction).
     */
    public final void executeDML() throws Exception {
        Session session = ApplicationContextFactory.getCurrentSession();
        session.setCacheMode(CacheMode.IGNORE);
        applyPatch(session);
        session.flush();
    }

    protected abstract void applyPatch(Session session) throws Exception;

    /**
     * Execute patch DDL statements after DML (non-transacted mode in most
     * databases).
     */
    public final void executeDDLAfter(boolean inTransaction) throws Exception {
        executeDDL("[DDLAfter]", getDDLQueriesAfter(), inTransaction);
    }

    protected List<String> getDDLQueriesAfter() {
        return Lists.newArrayList();
    }

    private void executeDDL(String category, List<String> queries, boolean inTransaction) throws Exception {
        for (String query : queries) {
            if (!Strings.isNullOrEmpty(query)) {
                log.info(category + ": " + query);
                if (inTransaction) {
                    ApplicationContextFactory.getCurrentSession().createSQLQuery(query).executeUpdate();
                } else {
                    ApplicationContextFactory.getDataSource().getConnection().createStatement().executeUpdate(query);
                }
            }
        }
    }

    protected final String getDDLCreateSequence(String sequenceName) {
        if (dbType == DBType.Oracle || dbType == DBType.PostgreSQL) {
            return "CREATE SEQUENCE " + sequenceName;
        }
        return null;
    }

    protected final String getDDLCreateTable(String tableName, List<ColumnDef> columnDefinitions, String unique) {
        String query = "CREATE TABLE " + tableName + " (";
        for (ColumnDef columnDef : columnDefinitions) {
            if (columnDefinitions.indexOf(columnDef) > 0) {
                query += ", ";
            }
            query += columnDef.name + " " + columnDef.getSqlTypeName(dialect);
            if (columnDef.primaryKey) {
                String primaryKeyModifier;
                switch (dbType) {
                case HSQL:
                case MSSQL:
                    primaryKeyModifier = "IDENTITY NOT NULL PRIMARY KEY";
                    break;
                case Oracle:
                    primaryKeyModifier = "NOT NULL PRIMARY KEY";
                    break;
                case PostgreSQL:
                    primaryKeyModifier = "PRIMARY KEY";
                    break;
                case MySQL:
                    primaryKeyModifier = "NOT NULL PRIMARY KEY AUTO_INCREMENT";
                    break;
                default:
                    primaryKeyModifier = "PRIMARY KEY";
                    break;
                }
                query += " " + primaryKeyModifier;
                continue;
            }
            if (!columnDef.allowNulls) {
                query += " NOT NULL";
            }
        }
        if (unique != null) {
            query += ", UNIQUE " + unique;
        }
        query += ")";
        return query;
    }

    protected final String getDDLRenameTable(String oldTableName, String newTableName) {
        String query;
        switch (dbType) {
        case MSSQL:
            query = "sp_rename '" + oldTableName + "', '" + newTableName + "'";
            break;
        case MySQL:
            query = "RENAME TABLE " + oldTableName + " TO " + newTableName;
            break;
        default:
            query = "ALTER TABLE " + oldTableName + " RENAME TO " + newTableName;
            break;
        }
        return query;
    }

    protected final String getDDLRemoveTable(String tableName) {
        return "DROP TABLE " + tableName; // TODO IF EXISTS
    }

    protected final String getDDLCreateIndex(String tableName, String indexName, String... columnNames) {
        String conjunctedColumnNames = Joiner.on(", ").join(columnNames);
        return "CREATE INDEX " + indexName + " ON " + tableName + " (" + conjunctedColumnNames + ")";
    }

    protected final String getDDLCreateUniqueKey(String tableName, String indexName, String... columnNames) {
        String conjunctedColumnNames = Joiner.on(", ").join(columnNames);
        return "ALTER TABLE " + tableName + " ADD CONSTRAINT " + indexName + " UNIQUE (" + conjunctedColumnNames + ")";
    }

    protected final String getDDLRenameIndex(String tableName, String indexName, String newIndexName) {
        String query;
        switch (dbType) {
        case MSSQL:
            query = "sp_rename '" + tableName + "." + indexName + "', '" + newIndexName + "'";
            break;
        default:
            throw new InternalApplicationException("TODO");
        }
        return query;
    }

    protected final String getDDLRemoveIndex(String tableName, String indexName) {
        switch (dbType) {
        case Oracle:
        case PostgreSQL:
            return "DROP INDEX " + indexName;
        default:
            return "DROP INDEX " + indexName + " ON " + tableName;
        }
    }

    protected final String getDDLCreateForeignKey(String tableName, String keyName, String columnName, String refTableName, String refColumnName) {
        return "ALTER TABLE " + tableName + " ADD CONSTRAINT " + keyName + " FOREIGN KEY (" + columnName + ") REFERENCES " + refTableName + " ("
                + refColumnName + ")";
    }

    protected final String getDDLRenameForeignKey(String keyName, String newKeyName) {
        String query;
        switch (dbType) {
        case MSSQL:
            query = "sp_rename '" + keyName + "', '" + newKeyName + "'";
            break;
        default:
            throw new InternalApplicationException("TODO");
        }
        return query;
    }

    protected final String getDDLRemoveForeignKey(String tableName, String keyName) {
        String constraint;
        switch (dbType) {
        case MySQL:
            constraint = "FOREIGN KEY";
            break;
        default:
            constraint = "CONSTRAINT";
            break;
        }
        return "ALTER TABLE " + tableName + " DROP " + constraint + " " + keyName;
    }

    protected final String getDDLCreateColumn(String tableName, ColumnDef columnDef) {
        String lBraced = "";
        String rBraced = "";
        if (dbType == DBType.Oracle) {
            lBraced = "(";
            rBraced = ")";
        }
        String query = "ALTER TABLE " + tableName + " ADD " + lBraced;
        query += columnDef.name + " " + columnDef.getSqlTypeName(dialect);
        if (columnDef.defaultValue != null) {
            query += " DEFAULT " + columnDef.defaultValue;
        }
        if (!columnDef.allowNulls) {
            query += " NOT NULL";
        }
        query += rBraced;
        return query;
    }

    protected final String getDDLRenameColumn(String tableName, String oldColumnName, ColumnDef newColumnDef) {
        String query;
        switch (dbType) {
        case Oracle:
        case PostgreSQL:
            query = "ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnDef.name;
            break;
        case MSSQL:
            query = "sp_rename '" + tableName + "." + oldColumnName + "', '" + newColumnDef.name + "', 'COLUMN'";
            break;
        case MySQL:
            query = "ALTER TABLE " + tableName + " CHANGE " + oldColumnName + " " + newColumnDef.name + " " + newColumnDef.getSqlTypeName(dialect);
            break;
        default:
            query = "ALTER TABLE " + tableName + " ALTER COLUMN " + oldColumnName + " RENAME TO " + newColumnDef.name;
            break;
        }
        return query;
    }

    protected final String getDDLModifyColumn(String tableName, String columnName, String sqlTypeName) {
        String query;
        switch (dbType) {
        case Oracle:
            query = "ALTER TABLE " + tableName + " MODIFY(" + columnName + " " + sqlTypeName + ")";
            break;
        case PostgreSQL:
            query = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + sqlTypeName;
            break;
        case MySQL:
            query = "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + sqlTypeName;
            break;
        default:
            query = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + sqlTypeName;
            break;
        }
        return query;
    }

    protected final String getDDLRemoveColumn(String tableName, String columnName) {
        return "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
    }

    protected final String getDDLTruncateTable(String tableName) {
        return "TRUNCATE TABLE " + tableName;
    }

    protected final String getDDLTruncateTableUsingDelete(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public static class ColumnDef {
        private boolean primaryKey;
        private String name;
        private int sqlType;
        private String sqlTypeName;
        private boolean allowNulls;
        private String defaultValue;

        public ColumnDef(String name, int sqlType, boolean allowNulls) {
            this.name = name;
            this.sqlType = sqlType;
            this.allowNulls = allowNulls;
        }

        public ColumnDef(String name, String sqlTypeName, boolean allowNulls) {
            this.name = name;
            this.sqlTypeName = sqlTypeName;
            this.allowNulls = allowNulls;
        }

        /**
         * Creates column def which allows null values.
         */
        public ColumnDef(String name, int sqlType) {
            this(name, sqlType, true);
        }

        /**
         * Creates column def which allows null values.
         */
        public ColumnDef(String name, String sqlTypeName) {
            this(name, sqlTypeName, true);
        }

        public String getSqlTypeName(Dialect dialect) {
            if (sqlTypeName != null) {
                return sqlTypeName;
            }
            return dialect.getTypeName(sqlType);
        }

        public ColumnDef setPrimaryKey() {
            primaryKey = true;
            return this;
        }

        public ColumnDef setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }
}
