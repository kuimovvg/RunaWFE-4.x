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
package ru.runa.af.presentation.filter;

import java.io.Serializable;

/**
 * 
 * Created on 25.12.2006
 *
 */
public class ExternalCriteriaFilter implements Cloneable, Serializable {

    private static final long serialVersionUID = -5818458739074211461L;

    private String name = "";

    private String value = "";

    private boolean filterOn = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null) ? "" : name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = (value == null) ? "" : value;
    }

    public boolean isFilterOn() {
        return filterOn;
    }

    public void setFilterOn(boolean filterOn) {
        this.filterOn = filterOn;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalCriteriaFilter)) {
            return false;
        }

        ExternalCriteriaFilter obj = (ExternalCriteriaFilter) o;

        return name.equals(obj.getName()) && value.equals(obj.getValue()) && filterOn == obj.isFilterOn();
    }

    public Object clone() {
        try {
            ExternalCriteriaFilter clone;
            clone = (ExternalCriteriaFilter) super.clone();
            clone.setName(getName());
            clone.setValue(getValue());
            clone.setFilterOn(isFilterOn());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone error");
        }
    }
}
