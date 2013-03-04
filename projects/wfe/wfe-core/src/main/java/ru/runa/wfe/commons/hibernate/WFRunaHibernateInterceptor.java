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
package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

public class WFRunaHibernateInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;

    private boolean isOracleDatabase() {
        return ApplicationContextFactory.getDBType() == DBType.Oracle;
    }

    private boolean onChanges(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types,
            boolean fixOracleStrings) {
        boolean modified = false;
        if (fixOracleStrings && isOracleDatabase()) {
            // Oracle handles empty strings as NULLs so we change empty strings
            // to ' '.
            for (int i = 0; i < currentState.length; ++i) {
                if (currentState[i] instanceof String && ((String) currentState[i]).length() == 0) {
                    currentState[i] = " ";
                    modified = true;
                }
            }
        }
        if (entity instanceof Task || entity instanceof Swimlane) {
            CachingLogic.onTaskChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Substitution || entity instanceof SubstitutionCriteria) {
            CachingLogic.onSubstitutionChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Executor || entity instanceof ExecutorGroupMembership) {
            CachingLogic.onExecutorChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Deployment) {
            CachingLogic.onProcessDefChange(entity, currentState, previousState, propertyNames, types);
        }
        return modified;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        return onChanges(entity, state, null, propertyNames, types, true);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        onChanges(entity, state, null, propertyNames, types, false);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return onChanges(entity, currentState, previousState, propertyNames, types, true);
    }

}
