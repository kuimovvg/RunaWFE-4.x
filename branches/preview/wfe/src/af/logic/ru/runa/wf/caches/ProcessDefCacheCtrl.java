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
package ru.runa.wf.caches;

import java.util.List;
import java.util.Set;

import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.bpm.db.ProcessDefinitionDAO;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.commons.cache.BaseCacheCtrl;
import ru.runa.commons.cache.CachingLogic;
import ru.runa.commons.cache.ProcessDefChangeListener;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;

public class ProcessDefCacheCtrl extends BaseCacheCtrl<ProcessDefCacheImpl> implements ProcessDefChangeListener {
    @Autowired
    private ProcessDefinitionDAO processDefinitionDAO;

    private ProcessDefCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public ProcessDefCacheImpl buildCache() {
        return new ProcessDefCacheImpl();
    }

    @Override
    public void doOnChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object);
            return;
        }
        ProcessDefCacheImpl cache = getCache();
        if (cache != null) {
            cache.clear((ArchievedProcessDefinition) object);
        }
    }

    public ExecutableProcessDefinition getDefinition(Long definitionId) throws ProcessDefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(this).getDefinition(processDefinitionDAO, definitionId);
    }

    public ExecutableProcessDefinition getLatestDefinition(String definitionName) throws ProcessDefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(this).getLatestDefinition(processDefinitionDAO, definitionName);
    }

    public List<ExecutableProcessDefinition> getLatestProcessDefinitions() {
        return CachingLogic.getCacheImpl(this).getLatestProcessDefinitions(processDefinitionDAO);
    }

    public Set<String> getSwimlaneNamesForDefinition(String processName) {
        return CachingLogic.getCacheImpl(this).getSwimlaneNamesForDefinition(processDefinitionDAO, processName);
    }

    public String[] getDefinitionType(String processName) {
        return CachingLogic.getCacheImpl(this).getDefinitionType(processName);
    }
}
