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
package ru.runa.af.logic;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.InitializerDAO;
import ru.runa.af.dao.PermissionDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.commons.Loader;
import ru.runa.commons.dbpatch.DBPatch;

import com.google.common.base.Strings;

public abstract class InitializerLogic {
    private static final Log log = LogFactory.getLog(InitializerLogic.class);
    private static final String[] dbPatches = new String[] { "ru.runa.af.dbpatch.DBVersionConstantInitializing",
            "ru.runa.wf.dbpatch.ReassignColumnInTask", "ru.runa.wf.dbpatch.AddOpenTasksTable", "ru.runa.wf.dbpatch.DynamicFieldsTableRename",
            "ru.runa.wf.dbpatch.RestoreJobsFromLog", "ru.runa.wf.dbpatch.RestorePassedTransitionsFromLog",
            "ru.runa.wf.dbpatch.RestoreSubprocessesFromLog", "ru.runa.wf.dbpatch.AddSystemLogTable", "ru.runa.af.dbpatch.RelationTablesAddition",
            "ru.runa.af.dbpatch.ProcessStarterExecutorAddition", "ru.runa.wf.dbpatch.AddExecutorToSystemLog",
            "ru.runa.af.dbpatch.ActorPhoneAddition", "ru.runa.af.dbpatch.SubstitutionExternalFlagAddition", "ru.runa.wf.dbpatch.MessageNodesPatch",
            "ru.runa.wf.dbpatch.SystemLogTableVersionFieldRename", "ru.runa.af.dbpatch.SubstitutionCriteriaPatch",
            "ru.runa.wf.dbpatch.BytesRefactoringPatch", "ru.runa.wf.dbpatch.ChangeStringVariableMaxSize", "ru.runa.wf.dbpatch.MySQLExtendBlobs",
            "ru.runa.wf.dbpatch.AddHierarchyProcessInstance", "ru.runa.wf.dbpatch.CleanupJbpmPatch" };

    @Autowired
    protected InitializerDAO initializerDAO;
    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    protected SecuredObjectDAO securedObjectDAO;
    @Autowired
    protected PermissionDAO permissionDAO;

    private boolean isAlreadyIntialized() {
        String version = initializerDAO.getValue(LogicResources.IS_DATABASE_INITIALIZED_VARIABLE_NAME);
        return "true".equalsIgnoreCase(version);
    }

    public void init(UserTransaction transaction, boolean force, boolean isArchiveDBinit) {
        if (isArchiveDBinit) {
            return;
            // if (!HibernateSessionFactory.isArchiveConfigured()) {
            // log.warn("Archive DB is disabled");
            // return;
            // }
            // HibernateSessionFactory.initFactory(HibernateSessionFactory.archiveSession);
        }
        try {
            initWithSession(transaction, force, isArchiveDBinit);
        } catch (RuntimeException e) {
            log.fatal("initialization failed", e);
            throw e;
        } finally {
            // HibernateSessionFactory.initFactory(HibernateSessionFactory.session);
            // HibernateSessionFactory.ResetFactories();
        }
    }

    /**
     * Initialize database if needed. Default hibernate session must be already
     * set to archive if required.
     * 
     * @param isArchiveDBinit
     *            Flag, equals true if archiving database is initialized; false
     *            for main database.
     */
    private void initWithSession(UserTransaction transaction, boolean force, boolean isArchiveDBinit) {
        if (force || !isAlreadyIntialized()) {
            initializeDatabase(transaction, force);
        } else {
            applyPatches(transaction, isArchiveDBinit);
        }
    }

    /**
     * Apply patches to initialized database.
     * 
     * @param isArchiveDBinit
     *            Flag, equals true if archiving database is initialized; false
     *            for main database.
     */
    private void applyPatches(UserTransaction transaction, boolean isArchiveDBinit) {
        log.info("database is initialized. skipping initialization ...");
        if (isArchiveDBinit) {
            return;
        }
        try {
            String versionString = initializerDAO.getValue(LogicResources.DATABASE_VERSION_VARIABLE_NAME);
            int dbVersion = Strings.isNullOrEmpty(versionString) ? 0 : Integer.parseInt(versionString);
            while (dbVersion < dbPatches.length) {
                try {
                    transaction.begin();
                    DBPatch patch = (DBPatch) Loader.loadObject(dbPatches[dbVersion], null, true);
                    patch.apply(isArchiveDBinit);
                    dbVersion++;
                    initializerDAO.saveOrUpdateConstant(LogicResources.DATABASE_VERSION_VARIABLE_NAME, Long.toString(dbVersion));
                    log.info("Patch " + dbPatches[dbVersion - 1] + "(" + (dbVersion - 1) + ") is applayed to database successfuly.");
                    transaction.commit();
                } catch (Throwable th) {
                    log.error("Can't apply patch " + dbPatches[dbVersion] + "(" + dbVersion + ").", th);
                    rollbackTransaction(transaction);
                    break;
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    private void rollbackTransaction(UserTransaction transaction) {
        int status = -1;
        try {
            status = transaction.getStatus();
            if (status != Status.STATUS_NO_TRANSACTION) {
                transaction.setRollbackOnly();
            } else {
                log.warn("Unable to rollback, status: " + status);
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to rollback, status: " + status, e);
        }
    }

    /**
     * Initialize database.
     * 
     * @param daoHolder
     *            Helper object for getting DAO's.
     * @param force
     *            Flag, equals true if database must be initialized even if it
     *            already exists.
     */
    private void initializeDatabase(UserTransaction transaction, boolean force) {
        // TODO CachingLogic.onGenericChange(); at startup does not matter
        if (force) {
            log.info("forcing database initialization...");
        } else {
            log.info("database is not initialized. initializing...");
        }
        createTablesInternal();
        try {
            transaction.begin();
            exportDataInternal();
            initializerDAO.saveOrUpdateConstant(LogicResources.IS_DATABASE_INITIALIZED_VARIABLE_NAME, String.valueOf(Boolean.TRUE));
            initializerDAO.saveOrUpdateConstant(LogicResources.DATABASE_VERSION_VARIABLE_NAME, String.valueOf(dbPatches.length));
            transaction.commit();
        } catch (Throwable th) {
            rollbackTransaction(transaction);
            throw new InternalApplicationException(th);
        }
    }

    protected abstract void createTablesInternal();

    protected abstract void exportDataInternal();
}
