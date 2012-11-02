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
 * Base class for filter criteria's, supporting selecting from predefined set of values.
 */
public abstract class EnumerationFilterCriteria extends FilterCriteria {

    private static final long serialVersionUID = 1L;

    /**
     * {@link Map} from enumerated value to property display name (struts property).
     */
    private final Map<String, String> enumerationValues;

    /**
     * Creates instance, with specified allowed values.
     * 
     * @param enumerationValues
     *            {@link Map} from enumerated value to property display name (struts property).
     */
    protected EnumerationFilterCriteria(Map<String, String> enumerationValues) {
        super();
        templatesCount = 1;
        filterTemplates = new String[] { "" };
        this.enumerationValues = enumerationValues;
    }

    @Override
    public Criterion buildCriterion(String fieldName) {
        return Restrictions.eq(fieldName, filterTemplates[0]);
    }

    @Override
    public void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap) {
        query.append(persistetObjectQueryAlias).append(".").append(persistetObjectFieldName);
        query.append(" = '").append(filterTemplates[0]).append("'");
    }

    @Override
    public String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        StringBuilder whereStringBuilder = new StringBuilder(persistetObjectQueryAlias);
        whereStringBuilder.append(".").append(fieldName);
        whereStringBuilder.append(" = '").append(filterTemplates[0]).append("' ");
        return whereStringBuilder.toString();
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        if (newTemplates.length != templatesCount) {
            throw new IllegalArgumentException("Incorrect parameters count");
        }
        if (!enumerationValues.keySet().contains(newTemplates[0])) {
            throw new IllegalArgumentException("Value " + newTemplates[0] + " is not allowed by enumeration criteria of type "
                    + this.getClass().getName());
        }
    }
}
