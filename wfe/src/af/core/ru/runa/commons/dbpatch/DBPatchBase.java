package ru.runa.commons.dbpatch;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;

import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.hibernate.HibernateSessionFactory;

public abstract class DBPatchBase implements DBPatch {
    protected Log log = LogFactory.getLog(getClass());

    protected boolean isArchiveDBinit;
    protected Session session;
    protected Dialect dialect;

    @Override
    public final void apply(boolean isArchiveDBinit) throws Exception {
        this.isArchiveDBinit = isArchiveDBinit;
        this.session = /*TODO isArchiveDBinit ? HibernateSessionFactory.getArchiveSession() : */HibernateSessionFactory.getSession();
        this.dialect = /*TODO HibernateSessionFactory.getConfiguration().buildSettings().getDialect();*/null;
        applyPatch();
    }

    protected abstract void applyPatch() throws Exception;

    protected boolean isHSQL() {
        return ApplicationContextFactory.getDialectClassName().contains("HSQL");
    }
 
    protected boolean isOracle() {
        return ApplicationContextFactory.getDialectClassName().contains("Oracle");
    }

    protected boolean isPostgreSQL() {
        return ApplicationContextFactory.getDialectClassName().contains("Postgre");
    }

    protected boolean isMySQL() {
        return ApplicationContextFactory.getDialectClassName().contains("MySQL");
    }

    protected boolean isSQLServer() {
        return ApplicationContextFactory.getDialectClassName().contains("SQLServer");
    }

    protected void createSequence(String sequenceName) {
        if (isOracle()) {
            session.createSQLQuery("CREATE SEQUENCE " + sequenceName).executeUpdate();
        }
    }

    protected void createTable(String tableName, List<ColumnDef> columnDefinitions, String unique) {
        String q = "CREATE TABLE " + tableName + " (";

        for (ColumnDef columnDef : columnDefinitions) {
            if (columnDefinitions.indexOf(columnDef) > 0) {
                q += ", ";
            }
            q += columnDef.name + " " + columnDef.getSqlTypeName(dialect);
            if (columnDef.primaryKey) {
                String primaryKeyModifier = "identity not null primary key";
                if (isPostgreSQL()) {
                    primaryKeyModifier = "PRIMARY KEY";
                }
                if (isMySQL()) {
                    primaryKeyModifier = "NOT NULL PRIMARY KEY AUTO_INCREMENT";
                }
                if (isOracle()) {
                    primaryKeyModifier = "NOT NULL PRIMARY KEY";
                }
                q += " " + primaryKeyModifier;
                continue;
            }
            if (!columnDef.allowNulls) {
                q += " NOT NULL";
            }
        }
        if (unique != null) {
            q += ", UNIQUE " + unique;
        }
        q += ")";
        Query query = session.createSQLQuery(q);
        log.info("creating table: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void createIndex(String indexName, String tableName, String columnName) {
        Query query = session.createSQLQuery("create index " + indexName + " on " + tableName + " (" + columnName + ")");
        log.info("creating index: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void createForeignKey(String keyName, String tableName, String columnName, String refTableName, String refColumnName) {
        Query query = session.createSQLQuery("alter table " + tableName + " add constraint " + keyName + " foreign key (" + columnName
                + ") references " + refTableName + " (" + refColumnName + ")");
        log.info("creating foreign key: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void removeForeignKey(String tableName, String keyName) {
        String constraint = "CONSTRAINT";
        if (isMySQL()) {
            constraint = "FOREIGN KEY";
        }
        Query query = session.createSQLQuery("ALTER TABLE " + tableName + " DROP "+constraint+" " + keyName);
        log.info("removing foreign key: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void renameTable(String oldTableName, String newTableName) {
        Query query = session.createSQLQuery("ALTER TABLE " + oldTableName + " RENAME TO " + newTableName);
        if (isSQLServer()) {
            query = session.createSQLQuery("sp_rename '" + oldTableName + "', '" + newTableName + "'");
        }
        if (isMySQL()) {
            query = session.createSQLQuery("RENAME TABLE " + oldTableName + " TO " + newTableName);
        }
        log.info("renaming table: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void removeTable(String tableName) {
        Query query = session.createSQLQuery("DROP TABLE " + tableName);
        log.info("removing table: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void addColumn(String tableName, ColumnDef columnDef) {
        String lBraced = "";
        String rBraced = "";
        if (isOracle()) {
            lBraced = "(";
            rBraced = ")";
        }
        String q = "ALTER TABLE " + tableName + " ADD " + lBraced;
        q += columnDef.name + " " + columnDef.getSqlTypeName(dialect);
        if (columnDef.defaultValue != null) {
            q += " DEFAULT " + columnDef.defaultValue;
        }
        if (!columnDef.allowNulls) {
            q += " NOT NULL";
        }
        q += rBraced;
        Query query = session.createSQLQuery(q);
        log.info("adding column: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void renameColumn(String tableName, String oldColumnName, ColumnDef newColumnDef) {
        Query query = session.createSQLQuery("ALTER TABLE " + tableName + " ALTER COLUMN " + oldColumnName + " RENAME TO " + newColumnDef.name);
        if (isPostgreSQL()) {
            query = session.createSQLQuery("ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnDef.name);
        }
        if (isSQLServer()) {
            query = session.createSQLQuery("sp_rename '" + tableName + "." + oldColumnName + "', '" + newColumnDef.name + "', 'COLUMN'");
        }
        if (isMySQL()) {
            query = session.createSQLQuery("ALTER TABLE " + tableName + " CHANGE " + oldColumnName + " " + newColumnDef.name + " "
                    + newColumnDef.getSqlTypeName(dialect));
        }
        log.info("renaming column: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void modifyColumn(String tableName, String columnName, String sqlTypeName) {
        Query query = session.createSQLQuery("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + sqlTypeName);
        if (isOracle()) {
            query = session.createSQLQuery("ALTER TABLE " + tableName + " MODIFY(" + columnName + " " + sqlTypeName + ")");
        }
        if (isMySQL()) {
            query = session.createSQLQuery("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + sqlTypeName);
        }
        if (isPostgreSQL()) {
            query = session.createSQLQuery("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + sqlTypeName);
        }
        log.info("modifying column: " + query.getQueryString());
        query.executeUpdate();
    }

    protected void removeColumn(String tableName, String columnName) {
        Query query = session.createSQLQuery("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
        log.info("removing column: " + query.getQueryString());
        query.executeUpdate();
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
            this.primaryKey = true;
            return this;
        }

        public ColumnDef setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }
}
