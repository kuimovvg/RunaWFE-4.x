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
package ru.runa.wfe.presentation;

import java.io.Serializable;

import com.google.common.base.Objects;

class DynamicField implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long fieldIdx; // Index against classPresentation.getFields();
    private String dynamicValue; // Value inserted by user (variable name for example)

    protected DynamicField() {
    }

    public DynamicField(long fieldIdx, String fieldValue) {
        this.fieldIdx = fieldIdx;
        this.dynamicValue = fieldValue;
    }

    public Long getFieldIdx() {
        return fieldIdx;
    }

    protected void setFieldIdx(Long fieldIdx) {
        this.fieldIdx = fieldIdx;
    }

    public String getDynamicValue() {
        return dynamicValue;
    }

    protected void setDynamicValue(String dynamicValue) {
        this.dynamicValue = dynamicValue;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldIdx, dynamicValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DynamicField)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DynamicField other = (DynamicField) obj;
        return Objects.equal(fieldIdx, other.fieldIdx) && Objects.equal(dynamicValue, other.dynamicValue);
    }

}
