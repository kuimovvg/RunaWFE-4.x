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
package ru.runa.af;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.dao.ExecutorDAO;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

/**
 * Substitution with this criteria applies when substitutor not in actor list. Actor list contains executors from process variable with 'conf' name.
 */
@Entity
@DiscriminatorValue(value = "not_equals")
public class SubstitutionCriteriaNotEquals extends SubstitutionCriteria {
    private static final long serialVersionUID = 812323181232L;
    private static final Log log = LogFactory.getLog(SubstitutionCriteriaNotEquals.class);

    private String DISPLAY_TYPE = "substitutioncriteria.SubstitutionCriteriaNotEquals";

    public SubstitutionCriteriaNotEquals() {
    }

    @Override
    public boolean isSatisfied(TaskInstance taskInstance, Actor asActor, Actor substitutorActor) {
        Object variableValue = taskInstance.getVariable(getConf());
        if (variableValue == null) {
            return true;
        }
        String value = variableValue.toString();
        ExecutorDAO executorDAO = TmpApplicationContextFactory.getExecutorDAO();
        try {
            Set<Executor> confActors = new HashSet<Executor>();
            try {
                Long actorCode = new Long(value);
                Executor executor = executorDAO.getActorByCode(actorCode);
                confActors.add(executor);
            } catch (NumberFormatException e) {
                // means that variableValue is string, i.e. actorName
                if (!executorDAO.isExecutorExist(value)) {
                    return true;
                }
                Executor executor = executorDAO.getExecutor(value);
                if (executor instanceof Group) {
                    confActors.addAll(executorDAO.getGroupActors((Group) executor));
                } else {
                    confActors.add(executor);
                }
            }
            return !confActors.contains(substitutorActor);
        } catch (ExecutorOutOfDateException e) {
            log.warn(e.getMessage());
            return true;
        }
    }

    @Override
    public boolean validate() {
        return !(getConf() == null || getConf().isEmpty());
    }

    @Override
    public String displayType() {
        return DISPLAY_TYPE;
    }
}
