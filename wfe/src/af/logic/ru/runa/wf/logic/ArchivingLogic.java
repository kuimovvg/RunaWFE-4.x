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
package ru.runa.wf.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.SystemLogDAO;
import ru.runa.af.log.ProcessInstanceDeleteLog;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.dao.ArchiveDAO;

public class ArchivingLogic extends JbpmCommonLogic {

    private static final Log log = LogFactory.getLog(ArchivingLogic.class);

    @Autowired
    private ArchiveDAO archiveDAO;
    @Autowired
    private SystemLogDAO systemLogDAO;

    public boolean isProcessInstanceInArchive(Long processId) {
        // Session session = null;
        // try {
        // session =
        // ru.runa.af.dao.impl.HibernateSessionFactory.openArchiveSession(true);
        // return
        // session.createCriteria(ProcessInstance.class).add(Expression.eq("id",
        // processId)).uniqueResult() != null;
        // } catch (Throwable e) {
        // return false;
        // } finally {
        // HibernateSessionFactory.closeArchiveSession(true);
        // }
        return false;
    }

    public void archiveProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        archiveDAO.initSessionTransferToArchive();
        for (ProcessInstance processInstance : getAffectedProcessInstances(subject, startDate, finishDate, name, version, id, idTill, onlyFinished,
                dateInterval)) {
            log.info("Will archive process instance " + processInstance.getId());
            archiveDAO.copyProcessInstanceInArchive(processInstance);
        }
    }

    public void removeProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        for (ProcessInstance processInstance : getAffectedProcessInstances(subject, startDate, finishDate, name, version, id, idTill, onlyFinished,
                dateInterval)) {
            log.info("Will remove process instance " + processInstance.getId());
            removeProcessInstance(subject, processInstance);
        }
    }

    public void restoreProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id, Long idTill,
            boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        archiveDAO.initSessionTransferFromArchive();
        for (ProcessInstance processInstance : getAffectedProcessInstances(subject, startDate, finishDate, name, version, id, idTill, onlyFinished,
                dateInterval)) {
            log.info("Will restore process instance " + processInstance.getId());
            archiveDAO.copyProcessInstanceFromArchive(processInstance);
        }
    }

    private List<ProcessInstance> getAffectedProcessInstances(Subject subject, Date startDate, Date finishDate, String name, int version, Long id,
            Long idTill, boolean onlyFinished, boolean dateInterval) throws AuthenticationException, ProcessInstanceDoesNotExistException,
            SuperProcessInstanceExistsException, SecuredObjectOutOfDateException {
        List<ProcessInstance> result = new ArrayList<ProcessInstance>();
        if ((id != 0) && (idTill == 0)) {
            ProcessInstance instance = tmpDAO.get(ProcessInstance.class, id);
            if (instance == null) {
                throw new ProcessInstanceDoesNotExistException(id);
            }
            List<StartedSubprocesses> rootProcesses = tmpDAO.getRootSubprocesses(id);
            if (rootProcesses != null && rootProcesses.size() > 0) {
                return result;
            }
            if (isExecuteOperation(instance, startDate, finishDate, name, version, onlyFinished)) {
                result.add(instance);
            }
        } else {
            List<ProcessInstance> processInstances = null;
            if ((id != 0) && (idTill != 0)) {
                processInstances = tmpDAO.getProcessInstanceByIdInterval(id, idTill, onlyFinished);
            } else {
                if (dateInterval) {
                    processInstances = tmpDAO.getProcessInstanceByStartDateInterval(startDate, finishDate, onlyFinished);
                } else {
                    processInstances = tmpDAO.getProcessInstanceByDate(startDate, finishDate, onlyFinished);
                }
            }
            for (ProcessInstance processInstance : processInstances) {
                List<StartedSubprocesses> rootProcesses = tmpDAO.getRootSubprocesses(processInstance.getId());
                if (rootProcesses != null && rootProcesses.size() > 0) {
                    continue;
                }
                if (isExecuteOperation(processInstance, null, null, name, version, onlyFinished)) {
                    result.add(processInstance);
                }
            }
        }
        return result;
    }

    public List<ExecutableProcessDefinition> getAffectedProcessDefinitions(Subject subject, String definitionName, int version, int versionTo)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        // List<ProcessDefinitionGraphImpl> result = new
        // ArrayList<ProcessDefinitionGraphImpl>();
        // List<ProcessDefinitionGraphImpl> definitions =
        // tmpDAO.getProcessDefinitions(definitionName);
        // if (definitions == null || definitions.size() == 0) {
        // throw new ProcessDefinitionDoesNotExistException(definitionName);
        // }
        // if (version == 0) {
        // List<ProcessDefinitionGraphImpl> definitionsWithOutLast = new
        // ArrayList<ProcessDefinitionGraphImpl>();
        // definitionsWithOutLast.addAll(definitions.subList(1,
        // definitions.size()));
        // for (ProcessDefinitionGraphImpl definition : definitionsWithOutLast)
        // {
        // List<ProcessInstance> instances =
        // tmpDAO.getProcessInstancesForDefinitionVersion(definitionName,
        // definition.getVersion());
        // if (instances == null || instances.size() == 0) {
        // result.add(definition);
        // }
        // }
        // } else {
        // ProcessDefinitionGraphImpl defLastVersion = definitions.get(0);
        // if (defLastVersion.getVersion() == version) {
        // log.warn("Process definition:" + definitionName + " version:" +
        // version + " is last version.");
        // return result;
        // }
        // if (versionTo != 0) {
        // for (ProcessDefinitionGraphImpl definition : definitions) {
        // if (definition.getVersion() >= version && definition.getVersion() <=
        // versionTo) {
        // List<ProcessInstance> instances =
        // tmpDAO.getProcessInstancesForDefinitionVersion(definition.getName(),
        // definition.getVersion());
        // if (instances != null && instances.size() > 0) {
        // log.warn(" There are process instances for process definition:" +
        // definition.getName() + " version:"
        // + definition.getVersion() + ".");
        // } else {
        // result.add(definition);
        // }
        // }
        // }
        // } else {
        // for (ProcessDefinitionGraphImpl definition : definitions) {
        // if (definition.getVersion() == version) {
        // List<ProcessInstance> instances =
        // tmpDAO.getProcessInstancesForDefinitionVersion(definition.getName(),
        // definition.getVersion());
        // if (instances != null && instances.size() > 0) {
        // log.warn(" There are process instances for process definition:" +
        // definition.getName() + " version:"
        // + definition.getVersion() + ".");
        // // return new ArrayList<ProcessDefinition>();
        // } else {
        // result.add(definition);
        // }
        // break;
        // }
        // }
        // }
        // }
        return null;
    }

    public void archiveProcessDefinition(Subject subject, String definitionName, int version) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        // archiveDAO.initSessionTransferToArchive();
        // List<ProcessDefinitionGraphImpl> definitions =
        // getAffectedProcessDefinitions(subject, definitionName, version, 0);
        // for (ProcessDefinitionGraphImpl definition : definitions) {
        // archiveDAO.copyProcessDefinitionInArchive(definition.getId());
        // }
    }

    public void removeProcessDefinition(Subject subject, String definitionName, int version, int versionTo) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        // List<ProcessDefinition> definitions =
        // getAffectedProcessDefinitions(subject,
        // definitionName, version, versionTo);
        // for (ProcessDefinition definition : definitions) {
        // ProcessDefinitionDescriptor definitionStub =
        // createProcessDefinitionStub(definition);
        // checkPermissionAllowed(subject, definitionStub,
        // ProcessDefinitionPermission.UNDEPLOY_DEFINITION);
        // processDefinitionDAO.deleteDefinition(definition);
        // ProcessDefinitionDeleteLog log = new
        // ProcessDefinitionDeleteLog(SubjectPrincipalsHelper.getActor(subject).getCode(),
        // definition
        // .getName(), definition.getVersion());
        // systemLogDAO.create(log);
        // }
        // TODO
    }

    public void restoreProcessDefinitionFromArchive(Subject subject, String definitionName, int version) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        // archiveDAO.initSessionTransferFromArchive();
        // List<ProcessDefinitionGraphImpl> definitions =
        // getAffectedProcessDefinitions(subject, definitionName, version, 0);
        // for (ProcessDefinitionGraphImpl definition : definitions) {
        // archiveDAO.copyProcessDefinitionFromArchive(definition.getId());
        // }
    }

    private void removeProcessInstance(Subject subject, ProcessInstance processInstance) throws SuperProcessInstanceExistsException,
            SecuredObjectOutOfDateException, AuthenticationException {
        if (processInstance.getSuperProcessToken() != null) {
            log.error("Could not remove process instance " + processInstance.getId());
            throw new SuperProcessInstanceExistsException(processInstance.getProcessDefinition().getName(), processInstance.getSuperProcessToken()
                    .getProcessInstance().getProcessDefinition().getName());
        }
        prepareProcessRemoval(processInstance);
        processExecutionDAO.deleteInstance(processInstance);
        log.debug("Process Instance with id = " + processInstance.getId() + " is deleted.");
        systemLogDAO.create(new ProcessInstanceDeleteLog(SubjectPrincipalsHelper.getActor(subject).getCode(), processInstance.getId()));
    }

    private boolean isExecuteOperation(ProcessInstance instance, Date startDate, Date finishDate, String name, int version, boolean onlyFinished) {
        // if (name != null && name.trim().length() > 0) {
        // if (!instance.getProcessDefinition().getName().equals(name)) {
        // return false;
        // }
        // }
        // if (version != 0) {
        // if (instance.getProcessDefinition().getVersion() > version) {
        // return false;
        // }
        // }
        // if (startDate != null) {
        // if (instance.getStartDate().after(startDate)) {
        // return false;
        // }
        // }
        // if (finishDate != null) {
        // if (instance.getEndDate().after(finishDate)) {
        // return false;
        // }
        // }
        // if (onlyFinished && !instance.hasEnded()) {
        // return false;
        // }
        return true;
    }
}
