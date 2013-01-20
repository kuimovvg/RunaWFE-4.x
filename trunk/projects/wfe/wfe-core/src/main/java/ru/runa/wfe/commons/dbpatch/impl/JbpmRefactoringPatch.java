package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.dbpatch.DBPatch;

import com.google.common.collect.Lists;

public class JbpmRefactoringPatch extends DBPatch {
    private boolean jbpmIdTablesExist;
    private boolean jbpmCommentTableExists;

    @Override
    protected List<String> getDDLQueriesBefore() {
        if (dbType != DBType.MSSQL) {
            throw new InternalApplicationException("Database migration patch from RunaWFE 3.x to 4.x is currently supported only for MS SQL Server");
        }
        // "MySQL DB update to version RunaWFE4.x is not supported because of mass column (which are foreign keys) renames [Error on rename of (errno: 150)]"
        List<String> sql = super.getDDLQueriesBefore();
        // removed unused tables and columns
        sql.add(getDDLRemoveTable("PROPERTY_IDS"));
        sql.add(getDDLRemoveTable("JBPM_TASKACTORPOOL"));
        sql.add(getDDLRemoveTable("JBPM_POOLEDACTOR"));

        // refactored batch presentations;
        sql.add(getDDLRemoveTable("ACTIVE_BATCH_PRESENTATIONS"));
        sql.add(getDDLRemoveTable("BP_DYNAMIC_FIELDS"));
        sql.add(getDDLRemoveTable("CRITERIA_CONDITIONS"));
        sql.add(getDDLRemoveTable("DISPLAY_FIELDS"));
        sql.add(getDDLRemoveTable("FILTER_CRITERIAS"));
        sql.add(getDDLRemoveTable("GROUP_FIELDS"));
        sql.add(getDDLRemoveTable("GROUP_FIELDS_EXPANDED"));
        sql.add(getDDLRemoveTable("SORTED_FIELDS"));
        sql.add(getDDLRemoveTable("SORTING_MODES"));
        sql.add(getDDLTruncateTable("BATCH_PRESENTATIONS"));
        sql.add(getDDLTruncateTableUsingDelete("PROFILES"));

        // ru.runa.wfe.user.Profile
        sql.add(getDDLRenameTable("PROFILES", "PROFILE"));
        sql.add(getDDLCreateForeignKey("PROFILE", "FK_PROFILE_ACTOR", "ACTOR_ID", "EXECUTORS", "ID"));
        // ru.runa.wfe.presentation.BatchPresentation
        sql.add(getDDLRenameTable("BATCH_PRESENTATIONS", "BATCH_PRESENTATION"));
        sql.add(getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_NAME", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BATCH_PRESENTATION", "PRESENTATION_ID", new ColumnDef("CATEGORY", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("IS_ACTIVE", Types.TINYINT)));
        sql.add(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("FIELDS", Types.VARBINARY)));
        sql.add(getDDLRemoveIndex("BATCH_PRESENTATION", "PRESENTATION_NAME_ID_IDX"));
        sql.add(getDDLCreateIndex("BATCH_PRESENTATION", "PROFILE_ID_IDX", "PROFILE_ID"));
        // ru.runa.wfe.user.dao.ActorPassword
        sql.add(getDDLRenameTable("PASSWORDS", "ACTOR_PASSWORD"));
        sql.add(getDDLRenameColumn("ACTOR_PASSWORD", "PASSWD", new ColumnDef("PASSWORD", Types.VARBINARY)));
        // ru.runa.wfe.bot.BotStation
        sql.add(getDDLRenameTable("BOT_STATIONS", "BOT_STATION"));
        sql.add(getDDLRemoveColumn("BOT_STATION", "BS_USER"));
        sql.add(getDDLRemoveColumn("BOT_STATION", "BS_PASS"));
        // ru.runa.wfe.bot.Bot
        sql.add(getDDLRenameTable("BOTS", "BOT"));
        sql.add(getDDLRemoveColumn("BOT", "MAX_PERIOD"));
        sql.add(getDDLRenameColumn("BOT", "LAST_INVOKED", new ColumnDef("START_TIMEOUT", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BOT", "WFE_USER", new ColumnDef("USERNAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BOT", "WFE_PASS", new ColumnDef("PASSWORD", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BOT", new ColumnDef("VERSION", Types.BIGINT)));
        // ru.runa.wfe.bot.BotTask
        sql.add(getDDLRenameTable("BOT_TASKS", "BOT_TASK"));
        sql.add(getDDLRemoveColumn("BOT_TASK", "CONFIG"));
        sql.add(getDDLRenameColumn("BOT_TASK", "CLAZZ", new ColumnDef("TASK_HANDLER", Types.VARCHAR)));
        // ru.runa.wfe.user.Executor
        sql.add(getDDLRenameTable("EXECUTORS", "EXECUTOR"));
        sql.add(getDDLRenameColumn("EXECUTOR", "IS_GROUP", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_LEVEL", Types.INTEGER)));
        sql.add(getDDLCreateColumn("EXECUTOR", new ColumnDef("ESCALATION_EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("EXECUTOR", "FK_GROUP_ESCALATION_EXECUTOR", "ESCALATION_EXECUTOR_ID", "EXECUTOR", "ID"));
        // ru.runa.wfe.user.ExecutorGroupMembership
        sql.add(getDDLRenameTable("EXECUTOR_GROUP_RELATIONS", "EXECUTOR_GROUP_MEMBER"));
        // ru.runa.wfe.relation.Relation
        sql.add(getDDLRenameTable("RELATION_GROUPS", "EXECUTOR_RELATION"));
        // ru.runa.wfe.relation.RelationPair
        sql.add(getDDLRenameTable("EXECUTOR_RELATIONS", "EXECUTOR_RELATION_PAIR"));
        sql.add(getDDLRenameForeignKey("FK_RELATION_FROM_EXECUTOR", "FK_ERP_EXECUTOR_FROM"));
        sql.add(getDDLRenameForeignKey("FK_RELATION_TO_EXECUTOR", "FK_ERP_EXECUTOR_TO"));
        sql.add(getDDLRenameForeignKey("FK_RELATION_GROUP_ID", "FK_ERP_RELATION"));
        sql.add(getDDLRenameColumn("EXECUTOR_RELATION_PAIR", "RELATION_GROUP", new ColumnDef("RELATION_ID", Types.BIGINT)));
        // ru.runa.wfe.commons.dao.Localization
        List<ColumnDef> lColumns = Lists.newArrayList();
        lColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        lColumns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)));
        lColumns.add(new ColumnDef("VALUE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)));
        sql.add(getDDLCreateTable("LOCALIZATION", lColumns, null));
        // ru.runa.wfe.security.dao.PrivelegedMapping
        sql.add(getDDLRemoveTable("PRIVELEGE_MAPPINGS"));
        List<ColumnDef> privColumns = Lists.newArrayList();
        privColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        privColumns.add(new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        privColumns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("PRIVELEGED_MAPPING", privColumns, null));
        sql.add(getDDLCreateForeignKey("PRIVELEGED_MAPPING", "FK_PM_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("PRIVELEGED_MAPPING", "IDX_TYPE", "TYPE"));
        // ru.runa.wfe.security.dao.PermissionMapping
        List<ColumnDef> permColumns = Lists.newArrayList();
        permColumns.add(new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey());
        permColumns.add(new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false));
        permColumns.add(new ColumnDef("VERSION", Types.BIGINT, false));
        permColumns.add(new ColumnDef("MASK", Types.BIGINT, false));
        permColumns.add(new ColumnDef("IDENTIFIABLE_ID", Types.BIGINT, false));
        permColumns.add(new ColumnDef("EXECUTOR_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("PERMISSION_MAPPING", permColumns, null));
        sql.add(getDDLCreateForeignKey("PERMISSION_MAPPING", "FK_PERMISSION_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IDX_EXECUTOR", "EXECUTOR_ID"));
        sql.add(getDDLCreateIndex("PERMISSION_MAPPING", "IDX_TYPE", "TYPE"));

        // ru.runa.wfe.definition.Deployment
        sql.add(getDDLRenameTable("JBPM_PROCESSDEFINITION", "BPM_PROCESS_DEFINITION"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "CLASS_"));
        sql.add(getDDLRemoveForeignKey("BPM_PROCESS_DEFINITION", "FK_PROCDEF_STRTSTA"));
        sql.add(getDDLRemoveIndex("BPM_PROCESS_DEFINITION", "IDX_PROCDEF_STRTST"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "STARTSTATE_"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "ISTERMINATIONIMPLICIT_"));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("LANGUAGE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("CATEGORY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("BYTES", Types.VARBINARY)));

        // ru.runa.wfe.execution.Process
        sql.add(getDDLRenameTable("JBPM_PROCESSINSTANCE", "BPM_PROCESS"));
        sql.add(getDDLRemoveIndex("BPM_PROCESS", "IDX_PROCIN_KEY"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "KEY_"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "ISSUSPENDED_"));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "START_", new ColumnDef("START_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "PROCESSDEFINITION_", new ColumnDef("DEFINITION_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_PROCESS", "IDX_PROCIN_PROCDEF", "IDX_PROCESS_DEFINITION"));
        sql.add(getDDLRenameForeignKey("FK_PROCIN_PROCDEF", "FK_PROCESS_DEFINITION"));
        sql.add(getDDLRenameColumn("BPM_PROCESS", "ROOTTOKEN_", new ColumnDef("ROOT_TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_PROCESS", "IDX_PROCIN_ROOTTK", "IDX_PROCESS_ROOT_TOKEN"));
        sql.add(getDDLRenameForeignKey("FK_PROCIN_ROOTTKN", "FK_PROCESS_ROOT_TOKEN"));
        sql.add(getDDLRemoveForeignKey("BPM_PROCESS", "FK_PROCIN_SPROCTKN"));
        sql.add(getDDLRemoveIndex("BPM_PROCESS", "IDX_PROCIN_SPROCTK"));
        sql.add(getDDLRemoveColumn("BPM_PROCESS", "SUPERPROCESSTOKEN_"));

        // ru.runa.wfe.execution.Token
        sql.add(getDDLRenameTable("JBPM_TOKEN", "BPM_TOKEN"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "ISTERMINATIONIMPLICIT_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "LOCK_"));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", "IDX_TOKEN_NODE"));
        sql.add(getDDLRemoveForeignKey("BPM_TOKEN", "FK_TOKEN_NODE"));
        sql.add(getDDLRemoveIndex("BPM_TOKEN", "IDX_TOKEN_SUBPI"));
        sql.add(getDDLRemoveForeignKey("BPM_TOKEN", "FK_TOKEN_SUBPI"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NODEENTER_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NEXTLOGINDEX_"));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "START_", new ColumnDef("START_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_TOKEN", "IDX_TOKEN_PROCIN", "IDX_PROCESS"));
        sql.add(getDDLRenameForeignKey("FK_TOKEN_PROCINST", "FK_TOKEN_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "PARENT_", new ColumnDef("PARENT_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TOKEN", "ISABLETOREACTIVATEPARENT_", new ColumnDef("REACTIVATE_PARENT", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_TYPE", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TOKEN", new ColumnDef("TRANSITION_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        // ru.runa.wfe.execution.Swimlane
        sql.add(getDDLRenameTable("JBPM_SWIMLANEINSTANCE", "BPM_SWIMLANE"));
        sql.add(getDDLRemoveForeignKey("BPM_SWIMLANE", "FK_SWIMLANEINST_SL"));
        sql.add(getDDLRemoveIndex("BPM_SWIMLANE", "IDX_SWIMLINST_SL"));
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "SWIMLANE_"));
        sql.add(getDDLRemoveForeignKey("BPM_SWIMLANE", "FK_SWIMLANEINST_TM"));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_SWIMLANE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_PROCESS", "PROCESS_ID", "BPM_PROCESS", "ID"));
        sql.add(getDDLCreateIndex("BPM_SWIMLANE", "IDX_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateColumn("BPM_SWIMLANE", new ColumnDef("EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SWIMLANE", "FK_SWIMLANE_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));

        // ru.runa.wfe.task.Task
        sql.add(getDDLRenameTable("JBPM_TASKINSTANCE", "BPM_TASK"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISBLOCKING_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "PRIORITY_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISCANCELLED_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "CLASS_"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "START_"));
        sql.add(getDDLRemoveIndex("BPM_TASK", "IDX_TASKINST_TSK"));
        sql.add(getDDLRemoveForeignKey("BPM_TASK", "FK_TASKINST_TASK"));
        sql.add(getDDLRemoveForeignKey("BPM_TASK", "FK_TASKINST_TMINST"));
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISOPEN_")); // TODO 1 | 0
        sql.add(getDDLRemoveColumn("BPM_TASK", "ISSIGNALLING_")); // TODO 1 | 0
        sql.add(getDDLRenameColumn("BPM_TASK", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TASK", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TASK", "DESCRIPTION_", new ColumnDef("DESCRIPTION", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_TASK", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_TASK", "CREATE_", new ColumnDef("CREATE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "END_", new ColumnDef("END_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "DUEDATE_", new ColumnDef("DEADLINE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_TASK", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameForeignKey("FK_TASKINST_TOKEN", "FK_TASK_TOKEN"));
        sql.add(getDDLRemoveIndex("BPM_TASK", "IDX_TASKINST_TOKN"));
        sql.add(getDDLRenameColumn("BPM_TASK", "SWIMLANINSTANCE_", new ColumnDef("SWIMLANE_ID", Types.BIGINT)));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", "FK_TASK_SWIMLANE"));
        sql.add(getDDLRenameForeignKey("FK_TASKINST_SLINST", "FK_TASK_SWIMLANE"));
        sql.add(getDDLRemoveIndex("BPM_TASK", "IDX_TSKINST_SLINST"));
        sql.add(getDDLRenameColumn("BPM_TASK", "PROCINST_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameForeignKey("FK_TSKINS_PRCINS", "FK_TASK_PROCESS"));
        sql.add(getDDLCreateIndex("BPM_TASK", "IDX_PROCESS", "PROCESS_ID"));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("FIRST_OPEN", Types.TINYINT)));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("EXECUTOR_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_TASK", "FK_TASK_EXECUTOR", "EXECUTOR_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateIndex("BPM_TASK", "IDX_EXECUTOR", "EXECUTOR_ID"));

        // ru.runa.wfe.audit.ProcessLog
        sql.add(getDDLTruncateTable("JBPM_LOG"));
        sql.add(getDDLRenameTable("JBPM_LOG", "BPM_LOG"));
        sql.add(getDDLRenameColumn("BPM_LOG", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_LOG", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        sql.add(getDDLRemoveColumn("BPM_LOG", "INDEX_"));
        sql.add(getDDLRenameColumn("BPM_LOG", "DATE_", new ColumnDef("DATE", Types.DATE)));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_TOKEN"));
        sql.add(getDDLRenameColumn("BPM_LOG", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_PARENT"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "PARENT_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "MESSAGE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "EXCEPTION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_ACTION"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "ACTION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_NODE"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NODE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "ENTER_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "LEAVE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "DURATION_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGVALUE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_TRANSITION"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TRANSITION_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_CHILDTOKEN"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "CHILD_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_SOURCENODE"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "SOURCENODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_DESTNODE"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "DESTINATIONNODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_VARINST"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "VARIABLEINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDDATEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWDATEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDDOUBLEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWDOUBLEVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDLONGVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWLONGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGIDVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "OLDSTRINGVALUE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "NEWSTRINGVALUE_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_TASKINST"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKACTORID_"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "TASKOLDACTORID_"));
        sql.add(getDDLRemoveForeignKey("BPM_LOG", "FK_LOG_SWIMINST"));
        sql.add(getDDLRemoveColumn("BPM_LOG", "SWIMLANEINSTANCE_"));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("BYTES", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("CONTENT", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_LOG", "FK_LOG_PROCESS", "PROCESS_ID", "BPM_PROCESS", "ID"));
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("SEVERITY", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        // ru.runa.wfe.job.Job
        sql.add(getDDLRenameTable("JBPM_JOB", "BPM_JOB"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ISSUSPENDED_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "LOCKTIME_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ISEXCLUSIVE_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "EXCEPTION_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "RETRIES_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "REPEAT_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", "FK_JOB_TSKINST"));
        sql.add(getDDLRemoveIndex("BPM_JOB", "IDX_JOB_TSKINST"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "TASKINSTANCE_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", "FK_JOB_NODE"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "NODE_"));
        sql.add(getDDLRemoveForeignKey("BPM_JOB", "FK_JOB_ACTION"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "ACTION_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "GRAPHELEMENTTYPE_"));
        sql.add(getDDLRemoveColumn("BPM_JOB", "GRAPHELEMENT_"));
        sql.add(getDDLRenameColumn("BPM_JOB", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_JOB", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_JOB", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "DUEDATE_", new ColumnDef("DUE_DATE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_JOB", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_JOB", "IDX_JOB_PRINST", "IDX_JOB_PROCESS"));
        sql.add(getDDLRemoveIndex("BPM_JOB", "IDX_JOB_TOKEN"));
        sql.add(getDDLRenameColumn("BPM_JOB", "TOKEN_", new ColumnDef("TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_JOB", "TRANSITIONNAME_", new ColumnDef("TRANSITION_NAME", Types.VARCHAR)));
        sql.add(getDDLCreateColumn("BPM_JOB", new ColumnDef("REPEAT_DURATION", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        sql.add(getDDLRenameForeignKey("FK_JOB_PRINST", "FK_JOB_PROCESS"));

        // ru.runa.wfe.var.Variable
        sql.add(getDDLRenameTable("JBPM_VARIABLEINSTANCE", "BPM_VARIABLE"));
        sql.add(getDDLRemoveIndex("BPM_VARIABLE", "IDX_VARINST_TKVARMP"));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", "FK_VARINST_TKVARMP"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TOKENVARIABLEMAP_"));
        sql.add(getDDLRemoveIndex("BPM_VARIABLE", "IDX_VARINST_TK"));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", "FK_VARINST_TK"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TOKEN_"));
        sql.add(getDDLRemoveForeignKey("BPM_VARIABLE", "FK_VAR_TSKINST"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "TASKINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "STRINGIDCLASS_"));
        sql.add(getDDLRemoveColumn("BPM_VARIABLE", "LONGIDCLASS_"));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "CLASS_", new ColumnDef("DISCRIMINATOR", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "NAME_", new ColumnDef("NAME", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "VERSION_", new ColumnDef("VERSION", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "CONVERTER_", new ColumnDef("CONVERTER", Types.CHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "PROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_VARIABLE", "IDX_VARINST_PRCINS", "IDX_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "LONGVALUE_", new ColumnDef("LONGVALUE", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "STRINGVALUE_", new ColumnDef("STRINGVALUE", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "DATEVALUE_", new ColumnDef("DATEVALUE", Types.DATE)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "DOUBLEVALUE_", new ColumnDef("DOUBLEVALUE", Types.FLOAT)));
        sql.add(getDDLRenameColumn("BPM_VARIABLE", "BYTES_", new ColumnDef("BYTES", Types.VARBINARY)));
        sql.add(getDDLRemoveTable("JBPM_TOKENVARIABLEMAP"));

        // ru.runa.wfe.execution.NodeProcess
        sql.add(getDDLRenameTable("JBPM_NODE_SUBPROC", "BPM_SUBPROCESS"));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "ID_", new ColumnDef("ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "PROCESSINSTANCE_", new ColumnDef("PARENT_PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_SUBPROCESS", "IDX_NODE_SUBPROC_PROCINST", "IDX_PARENT_PROCESS"));
        sql.add(getDDLRenameForeignKey("FK_NODE_SUBPROC_SUBPROCINST", "FK_SUBPROCESS_PROCESS"));
        sql.add(getDDLRenameColumn("BPM_SUBPROCESS", "SUBPROCESSINSTANCE_", new ColumnDef("PROCESS_ID", Types.BIGINT)));
        sql.add(getDDLRenameIndex("BPM_SUBPROCESS", "IDX_NODE_SUBPROC_SUBPROCINST", "IDX_PROCESS"));
        sql.add(getDDLRenameForeignKey("FK_NODE_SUBPROC_PROCINST", "FK_SUBPROCESS_PARENT_PROCESS"));
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_TOKEN_ID", Types.BIGINT)));
        sql.add(getDDLCreateForeignKey("BPM_SUBPROCESS", "FK_SUBPROCESS_TOKEN", "PARENT_TOKEN_ID", "BPM_TOKEN", "ID"));
        sql.add(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("PARENT_NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        // ru.runa.wfe.ss.SubstitutionCriteria
        sql.add(getDDLRenameTable("SUBSTITUTION_CRITERIAS", "SUBSTITUTION_CRITERIA"));
        sql.add(getDDLRenameColumn("SUBSTITUTION_CRITERIA", "TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        // ru.runa.wfe.ss.Substitution
        sql.add(getDDLRenameTable("SUBSTITUTIONS", "SUBSTITUTION"));
        sql.add(getDDLRenameColumn("SUBSTITUTION", "IS_TERMINATOR", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLCreateIndex("SUBSTITUTION", "CRITERIA_ID_IDX", "CRITERIA_ID"));
        sql.add(getDDLCreateIndex("SUBSTITUTION", "ACTOR_ID_IDX", "ACTOR_ID"));
        sql.add(getDDLCreateForeignKey("SUBSTITUTION", "FK_SUBSTITUTION_CRITERIA", "CRITERIA_ID", "SUBSTITUTION_CRITERIA", "ID"));
        // ru.runa.wfe.audit.SystemLog
        sql.add(getDDLTruncateTable("SYSTEM_LOG"));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "LOG_TYPE", new ColumnDef("DISCRIMINATOR", Types.VARCHAR)));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "ACTOR_CODE", new ColumnDef("ACTOR_ID", Types.BIGINT)));
        sql.add(getDDLRenameColumn("SYSTEM_LOG", "PROCESS_INSTANCE", new ColumnDef("PROCESS_ID", Types.BIGINT)));

        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesBefore();
        if (jbpmIdTablesExist) {
            sql.add(getDDLRemoveTable("JBPM_ID_MEMBERSHIP"));
            sql.add(getDDLRemoveTable("JBPM_ID_PERMISSIONS"));
            sql.add(getDDLRemoveTable("JBPM_ID_GROUP"));
            sql.add(getDDLRemoveTable("JBPM_ID_USER"));
        }
        if (jbpmCommentTableExists) {
            sql.add(getDDLRemoveTable("JBPM_COMMENT"));
        }
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "TASKMGMTINSTANCE_"));
        sql.add(getDDLRemoveColumn("BPM_SWIMLANE", "ACTORID_"));

        // removed jbpm definition layer persistance
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_NODE_ACTION"));
        sql.add(getDDLRemoveForeignKey("JBPM_NODE", "FK_NODE_SCRIPT"));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", "FK_VARACC_PROCST"));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", "FK_VARACC_SCRIPT"));
        sql.add(getDDLRemoveForeignKey("JBPM_VARIABLEACCESS", "FK_VARACC_TSKCTRL"));
        sql.add(getDDLRemoveForeignKey("JBPM_SWIMLANE", "FK_SWL_ASSDEL"));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", "FK_TASK_STARTST"));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", "FK_TASK_TASKNODE"));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", "FK_TASK_ASSDEL"));
        sql.add(getDDLRemoveForeignKey("JBPM_TASK", "FK_TSK_TSKCTRL"));
        sql.add(getDDLRemoveColumn("JBPM_TASK", "TASKCONTROLLER_"));
        sql.add(getDDLRemoveForeignKey("JBPM_TRANSITION", "FK_TRANSITION_FROM"));
        sql.add(getDDLRemoveForeignKey("JBPM_TRANSITION", "FK_TRANSITION_TO"));

        sql.add(getDDLRemoveTable("JBPM_DECISIONCONDITIONS"));
        sql.add(getDDLRemoveTable("JBPM_RUNTIMEACTION"));
        sql.add(getDDLRemoveTable("JBPM_ACTION"));
        sql.add(getDDLRemoveTable("JBPM_EVENT"));
        sql.add(getDDLRemoveTable("JBPM_PROCESSFILES"));
        sql.add(getDDLRemoveTable("JBPM_VARIABLEACCESS"));
        sql.add(getDDLRemoveTable("JBPM_NODE"));
        sql.add(getDDLRemoveTable("JBPM_EXCEPTIONHANDLER"));
        sql.add(getDDLRemoveTable("JBPM_TASKCONTROLLER"));
        sql.add(getDDLRemoveTable("JBPM_DELEGATION"));

        sql.add(getDDLRemoveForeignKey("JBPM_MODULEDEFINITION", "FK_TSKDEF_START"));
        sql.add(getDDLRemoveTable("JBPM_TASK"));
        sql.add(getDDLRemoveTable("JBPM_SWIMLANE"));

        sql.add(getDDLRemoveTable("EXECUTOR_OPEN_TASKS"));
        sql.add(getDDLRemoveTable("PERMISSION_MAPPINGS"));
        sql.add(getDDLRemoveTable("SECURED_OBJECT_TYPES"));
        sql.add(getDDLRemoveTable("SECURED_OBJECTS"));

        sql.add(getDDLRemoveTable("PROCESS_TYPES"));
        sql.add(getDDLRemoveTable("PROCESS_DEFINITION_INFO"));
        sql.add(getDDLRemoveTable("JBPM_PASSTRANS"));
        sql.add(getDDLRemoveTable("JBPM_TRANSITION"));

        sql.add(getDDLRemoveTable("JBPM_MODULEINSTANCE"));

        sql.add(getDDLRemoveTable("JBPM_MODULEDEFINITION"));

        sql.add(getDDLRemoveColumn("BPM_TOKEN", "NODE_"));
        sql.add(getDDLRemoveColumn("BPM_TOKEN", "SUBPROCESSINSTANCE_"));

        sql.add(getDDLRemoveColumn("JBPM_TASKINSTANCE", "TASK_"));

        sql.add(getDDLRemoveColumn("BPM_JOB", "LOCKOWNER_"));

        sql.add(getDDLRemoveColumn("BPM_SUBPROCESS", "NODE_"));

        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        try {
            session.createSQLQuery("SELECT COUNT(*) FROM JBPM_ID_USER").uniqueResult();
            jbpmIdTablesExist = true;
        } catch (Exception e) {
            // may be missed
        }
        // fillPRIVELEGED_MAPPING
        //
        // convert PermissionMapping
        //
        // TODO fill process history for diagram drawing ... (JBPM_TRANSITION +
        // JBPM_PASSTRANS)

        // TODO почистить job c фиктивным LOCKOWNER_

        // TODO fill BPM_SUBPROCESS
    }

}
