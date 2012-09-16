package ru.runa.wf.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.SimpleExpression;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.SecuredObject;
import ru.runa.af.dao.impl.PermissionMapping;
import ru.runa.commons.JBPMLazyLoaderHelper;

/**
 * Archiving process context.
 * 
 * @author Konstantinov Aleksey 17.01.2012
 */
public class ArchivingContext {

    /**
     * Source Hibernate session.
     */
    private final Session srcSession;

    /**
     * Target hibernate session.
     */
    private final Session targetSession;

    /**
     * System administrator.
     */
    private final Actor admin;

    /**
     * System administrators group.
     */
    private final Group adminGroup;

    /**
     * Creates archiving process context.
     * 
     * @param srcSession
     *            Hibernate session to load from database.
     */
    public ArchivingContext(Session srcSession, Session targetSession, String administratorName, String administratorGroupName) {
        this.srcSession = srcSession;
        this.targetSession = targetSession;
        admin = loadExecutor(srcSession, administratorName);
        adminGroup = loadExecutor(srcSession, administratorGroupName);
    }

    /**
     * @return Source hibernate session.
     */
    public Session getSrcSession() {
        return srcSession;
    }

    /**
     * @return Target hibernate session.
     */
    public Session getTargetSession() {
        return targetSession;
    }

    /**
     * @return System administrator.
     */
    public Actor getAdmin() {
        return admin;
    }

    /**
     * @return System administrators group.
     */
    public Group getAdminGroup() {
        return adminGroup;
    }

    /**
     * Loads permission mappings for secured object. Loading permission mappings
     * only for Admin, AdminGroup and ProcessAdmins
     * 
     * @param securedObject
     *            Secured object, to load permission mappings.
     * @return Permission mappings for secured object.
     */
    public List<PermissionMapping> loadPermissions(SecuredObject securedObject) {
        String executorPropertyName = "executor";
        Criteria localPmCriteria = srcSession.createCriteria(PermissionMapping.class);
        localPmCriteria.add(Expression.eq("securedObject", securedObject));
        SimpleExpression adminExpr = Expression.eq(executorPropertyName, admin);
        SimpleExpression adminGroupExpr = Expression.eq(executorPropertyName, adminGroup);
        localPmCriteria.add(Expression.or(adminExpr, adminGroupExpr));
        return (List<PermissionMapping>) JBPMLazyLoaderHelper.forceLoading(localPmCriteria.list());
    }

    /**
     * Loads executor by name from database.
     * 
     * @param session
     *            Hibernate session to load executor.
     * @param executorName
     *            Loading executor name.
     * @return Loaded executor or null.
     */
    private <T extends Executor> T loadExecutor(Session session, String executorName) {
        Criteria executorCriteria = session.createCriteria(Executor.class);
        executorCriteria.add(Expression.eq("name", executorName));
        return (T) JBPMLazyLoaderHelper.forceLoading(executorCriteria.uniqueResult());
    }
}
