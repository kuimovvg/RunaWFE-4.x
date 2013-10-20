package ru.cg.runaex.database.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.structure.ForeignKey;
import liquibase.diff.Diff;
import liquibase.lockservice.LockService;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.cg.runaex.components.ConnectionInfo;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.dao.DbObjectDao;
import ru.cg.runaex.database.factory.LiquibaseDatabaseFactory;
import ru.cg.runaex.database.util.AsciiToNative;

/**
 * @author Петров А.
 */
@Service("updateDbService")
public class UpdateDbServiceImpl implements UpdateDbService {

  private static final SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSSS");

  private static final Log logger = LogFactory.getLog(UpdateDbServiceImpl.class);

  private final String CHANGELOGS_DIR;

  private final String BACKUP_DIR;

  private Properties defaultValues = new Properties();

  private Properties sqlTemplates = new Properties();

  private String runaex;

  private String dbUrl;

  @Autowired
  private DbObjectDao dbObjectDao;

  @Autowired
  private LiquibaseDatabaseFactory liquibaseDatabaseFactory;

  @Autowired
  private ConnectionInfo connectionInfo;

  @Autowired
  public void init() {
    this.runaex = connectionInfo.getTargetDbName();
    this.dbUrl = connectionInfo.getDbUrl();
  }

  public UpdateDbServiceImpl() throws IOException {
    defaultValues.loadFromXML(getClass().getResourceAsStream("/ru/cg/runaex/database/dao/config/default_values.xml"));
    sqlTemplates.loadFromXML(getClass().getResourceAsStream("/ru/cg/runaex/database/dao/config/sql_templates.xml"));

    String userHome = System.getProperty("user.home");
    if (!userHome.endsWith("/") && !userHome.endsWith("\\")) {
      userHome = userHome.concat(File.separator);
    }

    CHANGELOGS_DIR = userHome.concat("runaex").concat(File.separator).concat("changelogs/");
    File changelogsDir = new File(CHANGELOGS_DIR);
    changelogsDir.mkdirs();

    BACKUP_DIR = userHome.concat("runaex").concat(File.separator).concat("backups/");
    File backupsDir = new File(BACKUP_DIR);
    backupsDir.mkdirs();
  }

  public void setConnectionInfo(ConnectionInfo connectionInfo) {
    this.runaex = connectionInfo.getTargetDbName();
    this.dbUrl = connectionInfo.getDbUrl();
  }

  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  public DbObjectDao getDbObjectDao() {
    return dbObjectDao;
  }

  public void setDbObjectDao(DbObjectDao dbObjectDao) {
    this.dbObjectDao = dbObjectDao;
  }

  public LiquibaseDatabaseFactory getLiquibaseDatabaseFactory() {
    return liquibaseDatabaseFactory;
  }

  public void setLiquibaseDatabaseFactory(LiquibaseDatabaseFactory liquibaseDatabaseFactory) {
    this.liquibaseDatabaseFactory = liquibaseDatabaseFactory;
  }

  /**
   * @param sql
   * @param schemas
   * @throws Exception
   */
  @Override
  public void applyDb(String sql, List<String> schemas) throws Exception {
    Database defaultRefDb = liquibaseDatabaseFactory.createReferenceDatabase("");
    LockService lockService = LockService.getInstance(defaultRefDb);
    lockService.acquireLock();

    Database targetDatabase;
    Database referenceDatabase;
    OutputStream changeLogOutputStream = null;
    try {
      schemas.remove("public");

      dbObjectDao.executeSqlOnTmpDb(sql);
      List<String> targetDbSchemas = dbObjectDao.getSchemas();
      targetDbSchemas.remove("public");

      List<String> affectedTargetDbSchemas = new ArrayList<String>();

      ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();
      List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
      Map<String, DatabaseSnapshot> referenceDbSnapshots = new HashMap<String, DatabaseSnapshot>(schemas.size());
      for (String schema : schemas) {
        targetDatabase = liquibaseDatabaseFactory.createTargetDatabase(schema);
        referenceDatabase = liquibaseDatabaseFactory.createReferenceDatabase(schema);

        DatabaseSnapshot referenceDbSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase, schema, null);
        referenceDbSnapshots.put(schema, referenceDbSnapshot);

        try {
          if (!targetDbSchemas.contains(schema)) {
            dbObjectDao.createSchema(targetDatabase.escapeDatabaseObject(schema));
          }

          Diff diff = new Diff(
              referenceDbSnapshot,
              DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(targetDatabase, schema, null)
          );
          changeSets.addAll(diff.compare().getAsChangeSetList(targetDatabase));
        }
        catch (Exception ex) {
          logger.error(ex.toString(), ex);
          throw ex;
        }
        finally {
          targetDatabase.close();
          referenceDatabase.close();
        }

        if (targetDbSchemas.remove(schema)) {
          affectedTargetDbSchemas.add(schema);
        }
      }

      if (!changeSets.isEmpty()) {
        makeRunaexDbBackup(affectedTargetDbSchemas);

        targetDatabase = liquibaseDatabaseFactory.createTargetDatabase(null);

        addDefaultValuesToNotNullColumns(changeSets, targetDatabase, referenceDbSnapshots);

        String changeLogFile = CHANGELOGS_DIR.concat("db-changelog.").concat(String.valueOf(System.currentTimeMillis())).concat(".xml");
        DatabaseChangeLog changeLog = new DatabaseChangeLog(changeLogFile);
        changeLog.getChangeSets().addAll(changeSets);

        changeLogOutputStream = new FileOutputStream(changeLogFile);
        ChangeLogSerializerFactory.getInstance().getSerializer(changeLog.getPhysicalFilePath()).write(changeSets, changeLogOutputStream);

        Liquibase base = new Liquibase(changeLog, resourceAccessor, targetDatabase);
        try {
          base.update("Update");
        }
        finally {
          LockService.getInstance(targetDatabase).forceReleaseLock();
        }
        targetDatabase.close();
      }
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    finally {
      if (changeLogOutputStream != null) {
        changeLogOutputStream.close();
      }
      lockService.releaseLock();
      defaultRefDb.close();
    }
  }

  private void addDefaultValuesToNotNullColumns(List<ChangeSet> changeSets, Database targetDatabase, Map<String, DatabaseSnapshot> referenceDbSnapshots) {
    for (ChangeSet changeSet : new ArrayList<ChangeSet>(changeSets)) {
      for (Change change : new ArrayList<Change>(changeSet.getChanges())) {
        if (change instanceof AddNotNullConstraintChange) {
          String sql;
          AddNotNullConstraintChange addNotNullConstraintChange = (AddNotNullConstraintChange) change;
          ClassType columnDataType = ClassType.valueOfBySimpleName(addNotNullConstraintChange.getColumnDataType());
          ForeignKey foreignKey = findFK(referenceDbSnapshots.get(addNotNullConstraintChange.getSchemaName()).getForeignKeys(), addNotNullConstraintChange.getTableName(), addNotNullConstraintChange.getColumnName());

          if (foreignKey != null) {
            sql = generateUpdateSQLForRefColumn(addNotNullConstraintChange.getSchemaName(), addNotNullConstraintChange.getTableName(), addNotNullConstraintChange.getColumnName(),
                foreignKey.getPrimaryKeyTable().getSchema(), foreignKey.getPrimaryKeyTable().getName(), foreignKey.getPrimaryKeyColumns(),
                true, targetDatabase);
          }
          else {
            sql = generateUpdateSQLForNonRefColumn(addNotNullConstraintChange.getSchemaName(), addNotNullConstraintChange.getTableName(), addNotNullConstraintChange.getColumnName(),
                defaultValues.getProperty(columnDataType.getSimpleName().toLowerCase()), columnDataType, true, targetDatabase);
          }

          changeSet.addChangeBefore(new RawSQLChange(sql), addNotNullConstraintChange);
        }
        else if (change instanceof AddColumnChange) {
          AddColumnChange addColumnChange = (AddColumnChange) change;

          for (ColumnConfig columnConfig : addColumnChange.getColumns()) {
            if (!columnConfig.isNullable()) {
              String sql;
              ClassType columnDataType = ClassType.valueOfBySimpleName(columnConfig.getType().toLowerCase().contains("varchar") ? "varchar" : columnConfig.getType());
              ForeignKey foreignKey = findFK(referenceDbSnapshots.get(addColumnChange.getSchemaName()).getForeignKeys(), addColumnChange.getTableName(), columnConfig.getName());

              if (foreignKey != null) {
                sql = generateUpdateSQLForRefColumn(addColumnChange.getSchemaName(), addColumnChange.getTableName(), columnConfig.getName(),
                    foreignKey.getPrimaryKeyTable().getSchema(), foreignKey.getPrimaryKeyTable().getName(), foreignKey.getPrimaryKeyColumns(),
                    true, targetDatabase);
              }
              else {
                sql = generateUpdateSQLForNonRefColumn(addColumnChange.getSchemaName(), addColumnChange.getTableName(), columnConfig.getName(),
                    defaultValues.getProperty(columnDataType.getSimpleName().toLowerCase()), columnDataType, true, targetDatabase);
              }

              changeSet.addChange(new RawSQLChange(sql));

              AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
              addNotNullConstraintChange.setSchemaName(addColumnChange.getSchemaName());
              addNotNullConstraintChange.setTableName(addColumnChange.getTableName());
              addNotNullConstraintChange.setColumnName(columnConfig.getName());
              changeSet.addChange(addNotNullConstraintChange);

              columnConfig.getConstraints().setNullable(true);
            }
          }
        }
      }
    }
  }

  private String generateUpdateSQLForRefColumn(String baseTableSchema, String baseTable, String baseTableColumn,
                                               String refTableSchema, String refTable, String refTableColumn, boolean addWhereClause, Database targetDatabase) {

    String sql = MessageFormat.format(
        (String) sqlTemplates.get("updateRefColumn"),
        targetDatabase.escapeTableName(baseTableSchema, baseTable),
        targetDatabase.escapeColumnName(baseTableSchema, baseTable, baseTableColumn),
        targetDatabase.escapeColumnName(refTableSchema, refTable, refTableColumn),
        targetDatabase.escapeTableName(refTableSchema, refTable)
    );

    if (addWhereClause) {
      sql += MessageFormat.format(
          (String) sqlTemplates.get("isNullWhereClause"),
          targetDatabase.escapeColumnName(baseTableSchema, baseTable, baseTableColumn)
      );
    }

    return sql;
  }

  private String generateUpdateSQLForNonRefColumn(String baseTableSchema, String baseTable, String baseTableColumn,
                                                  String value, ClassType columnType, boolean addWhereClause, Database targetDatabase) {
    switch (columnType) {
      case VARCHAR:
      case STRING:
        value = StringUtils.trimToNull(value) != null ? "'" + value + "'" : "'-'";
        break;
      case TIMESTAMP:
      case DATE:
      case DATETIME:
      case TIMESTAMP_WITH_TIMEZONE:
        value = StringUtils.trimToNull(value) != null ? "'" + value + "'" : "now()";
        break;
      case INT8:
      case LONG:
      case INTEGER:
        value = StringUtils.trimToNull(value) != null ? value : "0";
      case BOOL:
      case BOOLEAN:
        value = StringUtils.trimToNull(value) != null ? value : "false";
      case BYTEA:
      case BYTEARRAY:
        value = StringUtils.trimToNull(value) != null ? value : "E'\u0020'";
    }

    String sql = MessageFormat.format(
        (String) sqlTemplates.get("updateNonRefColumn"),
        targetDatabase.escapeTableName(baseTableSchema, baseTable),
        targetDatabase.escapeColumnName(baseTableSchema, baseTable, baseTableColumn),
        value
    );

    if (addWhereClause) {
      sql += MessageFormat.format(
          (String) sqlTemplates.get("isNullWhereClause"),
          targetDatabase.escapeColumnName(baseTableSchema, baseTable, baseTableColumn)
      );
    }

    return sql;
  }

  private ForeignKey findFK(Set<ForeignKey> foreignKeys, String table, String column) {
    for (ForeignKey foreignKey : foreignKeys) {
      if (StringUtils.equals(table, foreignKey.getForeignKeyTable().getName())
          && StringUtils.equals(column, foreignKey.getForeignKeyColumns())) {
        return foreignKey;
      }
    }

    return null;
  }

  private void makeRunaexDbBackup(List<String> schmeas) throws IOException, InterruptedException {
    String uid = fmt.format(new Date());
    File backupDir = new File(BACKUP_DIR.concat(uid));
    backupDir.mkdir();
    for (String schema : schmeas) {
      StringBuilder sb = new StringBuilder("--file=")
          .append(BACKUP_DIR)
          .append(uid)
          .append(File.separator)
          .append(schema)
          .append(".sql");
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.environment().put("PGPASSWORD", runaex);

      String host = dbUrl.replace("jdbc:postgresql://", "");

      processBuilder.command("pg_dump", "-U", runaex, "-n", AsciiToNative.convert(schema), sb.toString(),
          "-h", host.substring(0, host.indexOf(":")), "-p", host.substring(host.indexOf(":") + 1, host.length()), runaex);
      Process process = processBuilder.start();
      IOUtils.copy(process.getErrorStream(), System.out);
    }
  }
}
