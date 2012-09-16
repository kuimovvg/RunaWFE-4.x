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
package ru.runa.af.log;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PDExcDel")
public class ProcessDefinitionDeleteExceptionLog extends ProcessDefinitionDeleteLog {

    public ProcessDefinitionDeleteExceptionLog(Long actorCode, String name, Long version) {
        super(actorCode, name, version);
    }

    boolean processInstanceExist;
    boolean lastVersion;

    @Column(name = "PROCESS_DEFINITION_PI_EXIST", updatable = false)
    public boolean getProcessInstanceExist() {
        return processInstanceExist;
    }

    public void setProcessInstanceExist(boolean processInstanceExist) {
        this.processInstanceExist = processInstanceExist;
    }

    @Column(name = "PROCESS_DEFINITION_LAST_VER", updatable = false)
    public boolean getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(boolean lastVersion) {
        this.lastVersion = lastVersion;
    }
}
