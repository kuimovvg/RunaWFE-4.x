/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.context.exe.variableinstance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.log.variableinstance.DateUpdateLog;

@Entity
@DiscriminatorValue(value="D")
public class DateInstance extends VariableInstance<Date> {
    protected Date object;

    @Override
    public boolean isStorable(Object value) {
        if (value == null) {
            return true;
        }
        return (Date.class.isAssignableFrom(value.getClass()));
    }

    @Column(name="DATEVALUE_")
    @Override
    protected Date getObject() {
        return object;
    }
    public void setObject(Date object) {
        this.object = object;
    }

    @Override
    protected void setNewValue(Date value) {
        if (token != null) {
            token.addLog(new DateUpdateLog(this, this.object, value));
        }
        this.object = value;
    }
}
