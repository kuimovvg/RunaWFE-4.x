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

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.runa.af.util.QueryParameter;

@Entity
@DiscriminatorValue(value = "java.lang.String_Any")
public class AnywhereStringFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = -1849845246809052465L;

    public AnywhereStringFilterCriteria() {
        this(new String[] { "" });
    }

    public AnywhereStringFilterCriteria(String[] filterTemplates) {
        this.filterTemplates = filterTemplates;
        templatesCount = 1;
    }

    protected void validate(String[] newTemplates) throws FilterFormatException {
        if (newTemplates.length != templatesCount) {
            throw new IllegalArgumentException("Incorrect parameters count");
        }
    }

    public Criterion buildCriterion(String fieldName) {
        Criterion criterion = Restrictions.like(fieldName, filterTemplates[0], MatchMode.ANYWHERE);
        return criterion;
    }

    public String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        StringBuilder whereStringBuilder = new StringBuilder("CAST(" + persistetObjectQueryAlias);
        String alias = persistetObjectQueryAlias + fieldName.replaceAll("\\.", "");
        whereStringBuilder.append(".").append(fieldName).append(" as char(50))");
        whereStringBuilder.append(" like :").append(alias);
        whereStringBuilder.append(" ");

        placeholders.put(alias, new QueryParameter(alias, "%" + filterTemplates[0] + "%", Hibernate.STRING));

        return whereStringBuilder.toString();
    }

    @Override
    public void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap) {
        query.append(persistetObjectQueryAlias).append(".").append(persistetObjectFieldName);
        query.append(" like :").append(persistetObjectFieldName.replaceAll("\\.", ""));
        queryNamedParameterNameValueMap.put(persistetObjectFieldName.replaceAll("\\.", ""), "%" + filterTemplates[0] + "%");
    }
}
