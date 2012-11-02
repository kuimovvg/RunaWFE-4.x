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

import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

/**
 * Created on 01.09.2005
 */
public class IntegerFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 642103915780987672L;

    public IntegerFilterCriteria() {
        filterTemplates = new String[] { "" };
        templatesCount = 1;
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        if (newTemplates.length != templatesCount) {
            throw new IllegalArgumentException("Incorrect parameters count");
        }
        try {
            Integer.parseInt(newTemplates[0]);
        } catch (NumberFormatException nfe) {
            throw new FilterFormatException(nfe.getMessage());
        }
    }

    @Override
    public Criterion buildCriterion(String fieldName) {
        Criterion criterion = Restrictions.eqProperty(fieldName, filterTemplates[0]);
        return criterion;
    }

    @Override
    public String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        StringBuilder whereStringBuilder = new StringBuilder(persistetObjectQueryAlias);
        whereStringBuilder.append(".").append(fieldName);
        whereStringBuilder.append(" = :").append(fieldName.replaceAll("\\.", ""));
        whereStringBuilder.append(" ");
        placeholders.put(fieldName.replaceAll("\\.", ""), new QueryParameter(fieldName.replaceAll("\\.", ""), Long.valueOf(filterTemplates[0])));
        return whereStringBuilder.toString();
    }

    @Override
    public void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap) {
        query.append(persistetObjectQueryAlias).append(".").append(persistetObjectFieldName);
        query.append(" = :").append(persistetObjectFieldName.replaceAll("\\.", ""));
        queryNamedParameterNameValueMap.put(persistetObjectFieldName.replaceAll("\\.", ""), Long.valueOf(filterTemplates[0]));
    }
}
