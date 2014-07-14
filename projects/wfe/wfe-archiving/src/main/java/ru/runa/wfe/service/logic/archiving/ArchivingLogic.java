package ru.runa.wfe.service.logic.archiving;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.service.ArchivingService;
import ru.runa.wfe.service.exceptions.DefinitionHasProcessesException;
import ru.runa.wfe.service.exceptions.PermissionDeniedException;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDAO;

public class ArchivingLogic extends WFCommonLogic implements ArchivingService {

    private static final String PROCESS_TABLE = "BPM_PROCESS";

    private static final String TOKEN_TABLE = "BPM_TOKEN";

    private static final String PROCESS_DEFINITION_TABLE = "BPM_PROCESS_DEFINITION";

    protected final Log log = LogFactory.getLog(ArchivingLogic.class);

    @Autowired
    @Qualifier("processDAO")
    private ProcessDAO processDAO;
    @Autowired
    @Qualifier("archProcessDAO")
    private ProcessDAO archProcessDAO;
    @Autowired
    @Qualifier("deploymentDAO")
    private DeploymentDAO deploymentDAO;
    @Autowired
    @Qualifier("archDeploymentDAO")
    private DeploymentDAO archDeploymentDAO;
    @Autowired
    @Qualifier("archTokenDAO")
    private TokenDAO archTokenDAO;
    @Autowired
    @Qualifier("tokenDAO")
    private TokenDAO tokenDAO;
    @Autowired
    @Qualifier("archExecutorDAO")
    private ExecutorDAO archExecutorDAO;
    @Autowired
    @Qualifier("executorDAO")
    private ExecutorDAO executorDAO;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backupProcess(User user, Long processId) {
        processLogic(user, processId, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backupProcessDefinition(User user, String definitionName, Long version) {
        deploymentLogic(user, definitionName, version, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreProcess(User user, Long processId) {
        processLogic(user, processId, false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreProcessDefinition(User user, String definitionName, Long version) {
        deploymentLogic(user, definitionName, version, false);
    }

    @SuppressWarnings("unchecked")
    private void processLogic(User user, Long processId, boolean toArchive) {
        try {
            HibernateTemplate template = null;
            Configuration configuration = null;
            ProcessDAO targetProcessDAO = null;
            ProcessDAO sourceProcessDAO = null;
            DeploymentDAO targetDeploymentDAO = null;

            if (toArchive) {
                targetProcessDAO = archProcessDAO;
                targetDeploymentDAO = archDeploymentDAO;
                sourceProcessDAO = processDAO;
                configuration = getArchiveConfiguration();
            } else {
                targetProcessDAO = processDAO;
                targetDeploymentDAO = deploymentDAO;
                sourceProcessDAO = archProcessDAO;
                configuration = ApplicationContextFactory.getConfiguration();
            }
            Process process = sourceProcessDAO.get(processId);
            if (process == null) {
                throw new IllegalArgumentException(String.format("process with id = %s not found", processId));
            }
            template = targetProcessDAO.getHibernateTemplate();
            configuration.buildMappings();
            Iterator<PersistentClass> iter = configuration.getClassMappings();
            while (iter.hasNext()) {
                PersistentClass persistentClass = iter.next();
                String strategy = "assigned";
                KeyValue val = persistentClass.getIdentifier();
                if (val instanceof SimpleValue) {
                    SimpleValue vv = (SimpleValue) val;
                    vv.setIdentifierGeneratorStrategy(strategy);
                    vv.setNullValue("undefined");
                }
                persistentClass.setLazy(false);
            }

            template.setSessionFactory(configuration.buildSessionFactory());
            Session session = template.getSessionFactory().getCurrentSession();

            targetProcessDAO.setHibernateTemplate(template);
            targetDeploymentDAO.setHibernateTemplate(template);

            if (toArchive) {
                boolean isAllowed = isPermissionAllowed(user, process, ProcessPermission.CANCEL_PROCESS);
                if (!isAllowed) {
                    throw new PermissionDeniedException();
                }
            }
            HibernateTemplate tplArchive = template;
            Token rootToken = process.getRootToken();
            clearChild(rootToken);
            rootToken.setProcess(null);
            Deployment deployment = process.getDeployment();
            Deployment existDep = null;
            try {
                existDep = targetDeploymentDAO.findDeployment(deployment.getName(), deployment.getVersion());
            } catch (DefinitionDoesNotExistException e) {

            }
            if (existDep != null) {
                deployment = existDep;
            } else {
                setIdentityInsert(session, true, PROCESS_DEFINITION_TABLE);
                prepareReplicate(toArchive);
                tplArchive.replicate(deployment, ReplicationMode.OVERWRITE);
                finishReplicate(toArchive);
                setIdentityInsert(session, false, PROCESS_DEFINITION_TABLE);
            }
            setIdentityInsert(session, true, TOKEN_TABLE);
            prepareReplicate(toArchive);
            tplArchive.replicate(rootToken, ReplicationMode.OVERWRITE);
            finishReplicate(toArchive);
            setIdentityInsert(session, false, TOKEN_TABLE);
            process.setDeployment(deployment);
            prepareReplicate(toArchive);
            setIdentityInsert(session, true, PROCESS_TABLE);
            tplArchive.replicate(process, ReplicationMode.OVERWRITE);
            rootToken.setProcess(process);
            finishReplicate(toArchive);
            if (toArchive) {
                Process toDelete = sourceProcessDAO.get(processId);
                deleteProcess(toDelete);
            }
            setIdentityInsert(session, false, PROCESS_TABLE);
        } catch (Exception e) {
            log.error(String.format("error backup process with id = %s", processId));
            log.error("", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void deploymentLogic(User user, String definitionName, Long version, boolean toArchive) {
        try {
            DeploymentDAO sourceDeploymentDAO = null;
            DeploymentDAO targetDeploymentDAO = null;
            Deployment source = null;
            Deployment target = null;

            if (toArchive) {
                sourceDeploymentDAO = deploymentDAO;
                targetDeploymentDAO = archDeploymentDAO;
            } else {
                sourceDeploymentDAO = archDeploymentDAO;
                targetDeploymentDAO = deploymentDAO;
            }

            try {
                source = sourceDeploymentDAO.findDeployment(definitionName, version);
                if (toArchive && source != null) {
                    List<Deployment> deployments = processDAO.getHibernateTemplate().find("from Process p where p.deployment = ?", source);
                    if (deployments != null && deployments.size() > 0) {
                        throw new DefinitionHasProcessesException();
                    }
                }

            } catch (DefinitionDoesNotExistException e) {
            }
            if (source != null) {
                if (toArchive) {
                    if (!isPermissionAllowed(user, source, DefinitionPermission.UNDEPLOY_DEFINITION)) {
                        throw new PermissionDeniedException();
                    }
                }
                try {
                    target = targetDeploymentDAO.findDeployment(definitionName, version);
                } catch (DefinitionDoesNotExistException e) {
                }
                if (toArchive) {
                    if (target != null) {
                        sourceDeploymentDAO.delete(source);
                        return;
                    }
                }
                prepareReplicate(toArchive);
                targetDeploymentDAO.getHibernateTemplate().replicate(source, ReplicationMode.OVERWRITE);
                finishReplicate(toArchive);
                if (toArchive) {
                    targetDeploymentDAO.delete(source);
                }
            }
        } catch (DefinitionHasProcessesException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("error backup process definition with name = %s and version = %s", definitionName, version));
            log.error("", e);
        }
    }

    private void prepareReplicate(boolean toArchive) {
        if (toArchive) {
            prepareReplicate(processDAO.getHibernateTemplate(), archProcessDAO.getHibernateTemplate());
        } else {
            prepareReplicate(archProcessDAO.getHibernateTemplate(), processDAO.getHibernateTemplate());
        }
    }

    private void finishReplicate(boolean toArchive) {
        if (toArchive) {
            finishReplicate(archProcessDAO.getHibernateTemplate());
        } else {
            finishReplicate(processDAO.getHibernateTemplate());
        }
    }

    private void prepareReplicate(HibernateTemplate src, HibernateTemplate target) {
        src.clear();
        target.flush();
        target.clear();
    }

    private void finishReplicate(HibernateTemplate target) {
        target.flush();
        target.clear();
    }

    private void clearChild(Token rootToken) {
        if (rootToken.getChildren() == null) {
            return;
        }
        for (Token childToken : rootToken.getChildren()) {
            clearChild(childToken);
            childToken.setProcess(null);
        }
    }

    public static synchronized Configuration getArchiveConfiguration() {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) ApplicationContextFactory.getContext().getBean("&sessionFactoryA");
        return factoryBean.getConfiguration();
    }

    private synchronized void setIdentityInsert(Session session, boolean enableInsert, String tableName) {
        try {
            // TODO: session.connection() is depricated (scheduled for removal in 4.x). Replacement depends on need; for doing direct JDBC stuff use
            // TODO: doWork(org.hibernate.jdbc.Work); for opening a 'temporary Session' use (TBD).
            session.connection().createStatement().execute(String.format("SET IDENTITY_INSERT %s %s", tableName, (enableInsert ? "ON" : "OFF")));
        } catch (HibernateException e) {
            log.error("", e);
        } catch (SQLException e) {
            log.error("", e);
        }
    }
}
