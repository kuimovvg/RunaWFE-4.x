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
package ru.runa.wfe.presentation.filter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.hibernate.criterion.Criterion;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

import com.google.common.base.Objects;

public abstract class FilterCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String[] filterTemplates;
    protected int templatesCount;

    public int getTemplatesCount() {
        return templatesCount;
    }

    protected abstract void validate(String[] newTemplates) throws FilterFormatException;

    public String[] getFilterTemplates() {
        return filterTemplates;
    }

    protected void setFilterTemplates(String[] filterTemplates) {
        this.filterTemplates = filterTemplates;
    }

    public void applyFilterTemplates(String[] filterTemplates) throws FilterFormatException {
        validate(filterTemplates);
        this.filterTemplates = filterTemplates;
    }

    public abstract String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders);

    public abstract void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap);

    public abstract Criterion buildCriterion(String fieldName);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        if (Arrays.equals(((FilterCriteria) obj).filterTemplates, filterTemplates)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode((Object[]) filterTemplates);
    }
}
