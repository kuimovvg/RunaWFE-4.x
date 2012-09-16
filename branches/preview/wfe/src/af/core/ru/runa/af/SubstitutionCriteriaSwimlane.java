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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.bpm.taskmgmt.exe.TaskInstance;

/**
 * Substitution with this criteria applies when task process swimlane equals configuration.
 */
@Entity
@DiscriminatorValue(value = "swimlane")
public class SubstitutionCriteriaSwimlane extends SubstitutionCriteria {
    private static final long serialVersionUID = 812323181231L;

    private String DISPLAY_TYPE = "substitutioncriteria.SubstitutionCriteriaSwimlane";

    public SubstitutionCriteriaSwimlane() {
    }

    @Override
    public boolean isSatisfied(TaskInstance taskInstance, Actor asActor, Actor substitutorActor) {
        if (getConf() == null
                || !getConf().equals(
                        taskInstance.getProcessInstance().getProcessDefinition().getName() + "." + taskInstance.getSwimlaneInstance().getName())) {
            return false;
        }
        return true;
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
