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
 * aLong with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.caches;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.db.ProcessDefinitionDAO;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.par.ProcessArchive;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.commons.cache.BaseCacheImpl;
import ru.runa.commons.cache.Cache;
import ru.runa.commons.hibernate.HibernateSessionFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionInfo;
import ru.runa.wf.logic.JbpmDefinitionLogic;

import com.google.common.collect.Lists;

class ProcessDefCacheImpl extends BaseCacheImpl implements ProcessDefinitionCache {

    public static final String definitionIdToDefinitionName = "ru.runa.wf.caches.definitionIdToDefinition";
    public static final String definitionNameToLatestDefinitionName = "ru.runa.wf.caches.definitionNameToLatestDefinition";
    public static final String definitionNameToSwimlaneNamesName = "ru.runa.wf.caches.definitionNameToSwimlaneNames";
    public static final String definitionNameToTypeName = "ru.runa.wf.caches.definitionNameToType";
    public static final String definitionIDtoFilesName = "ru.runa.wf.caches.definitionIDtoFiles";
    public static final String definitionIDtoFormsName = "ru.runa.wf.caches.definitionIDtoForms";
    public static final String instanceIDtoDefinitionName = "ru.runa.wf.caches.instanceIDtoDefinition";
    public static final String taskIDtoDefinitionName = "ru.runa.wf.caches.taskIDtoDefinition";

    private final Cache<Long, ExecutableProcessDefinition> definitionIdToDefinition;
    private final Cache<String, Long> definitionNameToId;
    private final Cache<String, HashSet<String>> definitionNameToSwimlaneNames;
    private final Cache<String, String[]> definitionNameToType;
    private final List<ExecutableProcessDefinition> latestProcessDefinition = Lists.newArrayList();

    public ProcessDefCacheImpl() {
        definitionIdToDefinition = createCache(definitionIdToDefinitionName);
        definitionNameToId = createCache(definitionNameToLatestDefinitionName);
        definitionNameToSwimlaneNames = createCache(definitionNameToSwimlaneNamesName);
        definitionNameToType = createCache(definitionNameToTypeName);
    }

    public void clear(ArchievedProcessDefinition definition) {
        definitionIdToDefinition.remove(definition.getId());
        definitionNameToId.remove(definition.getName());
        definitionNameToSwimlaneNames.remove(definition.getName());
        latestProcessDefinition.clear();
        definitionNameToType.clear();
    }

    @Override
    public ExecutableProcessDefinition getDefinition(ProcessDefinitionDAO processDefinitionDAO, Long definitionId)
            throws ProcessDefinitionDoesNotExistException {
        ExecutableProcessDefinition processDefinition = null;
        synchronized (this) {
            processDefinition = definitionIdToDefinition.get(definitionId);
            if (processDefinition != null) {
                return processDefinition;
            }
        }
        ArchievedProcessDefinition processDefinitionDbImpl = processDefinitionDAO.getDefinitionNotNull(definitionId);
        processDefinitionDbImpl = (ArchievedProcessDefinition) JBPMLazyLoaderHelper.forceLoading(processDefinitionDbImpl);
        ProcessArchive archive = new ProcessArchive(processDefinitionDbImpl.getParFile());
        processDefinition = archive.parseProcessDefinition(processDefinitionDbImpl);
        synchronized (this) {
            definitionIdToDefinition.put(definitionId, processDefinition);
        }
        return processDefinition;
    }

    @Override
    public ExecutableProcessDefinition getLatestDefinition(ProcessDefinitionDAO processDefinitionDAO, String definitionName) {
        Long definitionId = null;
        synchronized (this) {
            definitionId = definitionNameToId.get(definitionName);
            if (definitionId != null) {
                return getDefinition(processDefinitionDAO, definitionId);
            }
        }
        definitionId = processDefinitionDAO.findLatestDefinition(definitionName).getId();
        synchronized (this) {
            definitionNameToId.put(definitionName, definitionId);
        }
        return getDefinition(processDefinitionDAO, definitionId);
    }

    @Override
    public List<ExecutableProcessDefinition> getLatestProcessDefinitions(ProcessDefinitionDAO processDefinitionDAO) {
        synchronized (this) {
            if (latestProcessDefinition != null) {
                return latestProcessDefinition;
            }
        }
        List<ArchievedProcessDefinition> result = processDefinitionDAO.findLatestDefinitions();
        synchronized (this) {
            for (ArchievedProcessDefinition processDefinition : result) {
                latestProcessDefinition.add(getDefinition(processDefinitionDAO, processDefinition.getId()));
            }
        }
        return latestProcessDefinition;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getSwimlaneNamesForDefinition(ProcessDefinitionDAO processDefinitionDAO, String processName) {
        HashSet<String> result = null;
        synchronized (this) {
            result = definitionNameToSwimlaneNames.get(processName);
            if (result != null) {
                return result;
            }
            result = new HashSet<String>();
        }
        List<ArchievedProcessDefinition> definitionList = processDefinitionDAO.findAllDefinitionVersions(processName);
        for (ArchievedProcessDefinition processDefinitionDBImpl : definitionList) {
            ExecutableProcessDefinition processDefinition = getDefinition(processDefinitionDAO, processDefinitionDBImpl.getId());
            for (String name : processDefinition.getSwimlanes().keySet()) {
                result.add(processName + JbpmDefinitionLogic.DEFINITION_NAME_SWIMLANE_NAME_SEPARATOR + name);
            }
        }
        synchronized (this) {
            definitionNameToSwimlaneNames.put(processName, result);
        }
        return result;
    }

    @Override
    public String[] getDefinitionType(String processName) {
        synchronized (this) {
            String[] result = definitionNameToType.get(processName);
            if (result != null) {
                return result;
            }
        }
        Session session = HibernateSessionFactory.openSession();
        try {
            List<ProcessDefinitionInfo> pInfoList = session.createQuery(
                    "select pInfo from ru.runa.wf.ProcessDefinitionInfo as pInfo where pInfo.processName='" + processName + "'").list();
            if (pInfoList.size() != 1) {
                throw new InternalApplicationException("Can't find type for process " + processName);
            }
            String[] result = pInfoList.get(0).getProcessType();
            synchronized (this) {
                definitionNameToType.put(processName, result);
            }
            return result;
        } finally {
            HibernateSessionFactory.closeSession(false);
        }
    }

}
