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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.runa.af.util.QueryParameter;

@Entity
@DiscriminatorValue(value = "java.util.Date")
public class DateFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 3369866551410013540L;

    private Date dateStart;

    private Date dateEnd;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy H:mm");

    private static final Log log = LogFactory.getLog(DateFilterCriteria.class);

    protected DateFilterCriteria() {
        filterTemplates = new String[] { "", "" };
        templatesCount = 2;
    }

    protected void validate(String[] newTemplates) throws FilterFormatException {
        if (newTemplates.length != templatesCount) {
            throw new IllegalArgumentException("Incorrect parameters count");
        }
        try {
            if (newTemplates[0].length() > 0) {
                dateFormat.parse(newTemplates[0]);
            }
            if (newTemplates[1].length() > 0) {
                dateFormat.parse(newTemplates[1]);
            }
        } catch (ParseException e) {
            throw new FilterFormatException(e.getLocalizedMessage());
        }
    }

    protected void setFilterTemplates(String[] filterTemplates) {
        if (filterTemplates == null || filterTemplates.length != 2) {
            this.filterTemplates = new String[] { "", "" };
        } else {
            this.filterTemplates = filterTemplates;
        }
    }

    private void initDates() {
        try {
            if (filterTemplates[0].length() > 0) {
                dateStart = dateFormat.parse(filterTemplates[0]);
            }
        } catch (ParseException e) {
            log.warn("start date parsing error: " + filterTemplates[0]);
        }
        try {
            if (filterTemplates[1].length() > 0) {
                dateEnd = dateFormat.parse(filterTemplates[1]);
            }
        } catch (ParseException e) {
            log.warn("end date parsing error: " + filterTemplates[1]);
        }
    }

    public Criterion buildCriterion(String fieldName) {
        return Restrictions.like(fieldName, filterTemplates[0], MatchMode.EXACT);
    }

    public void buildWhereClausePart(StringBuilder query, String persistetObjectFieldName, String persistetObjectQueryAlias,
            Map<String, Object> queryNamedParameterNameValueMap) {
        initDates();

        String placeholderStart = (persistetObjectFieldName + "Start").replaceAll("\\.", "");
        String placeholderEnd = (persistetObjectFieldName + "End").replaceAll("\\.", "");

        query.append(persistetObjectQueryAlias).append(".").append(persistetObjectFieldName);

        if (dateStart == null) {
            if (dateEnd == null) {
                // empty date (NULL value)
                query.append(" is null");
            } else {
                // less than
                query.append(" < :").append(placeholderEnd);
            }
        } else {
            if (dateEnd == null) {
                // more than
                query.append(" > :").append(placeholderStart);
            } else {
                // between
                query.append(" between :");
                query.append(placeholderStart);
                query.append(" and :");
                query.append(placeholderEnd);
            }
        }

        if (dateStart != null) {
            queryNamedParameterNameValueMap.put(placeholderStart, dateStart);
        }
        if (dateEnd != null) {
            queryNamedParameterNameValueMap.put(placeholderEnd, dateEnd);
        }
    }

    public String buildWhereCondition(String fieldName, String persistetObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        initDates();

        String placeholderStart = (fieldName + "Start").replaceAll("\\.", "");
        String placeholderEnd = (fieldName + "End").replaceAll("\\.", "");

        StringBuilder whereStringBuilder = new StringBuilder(persistetObjectQueryAlias);
        whereStringBuilder.append(".").append(fieldName);

        if (dateStart == null) {
            if (dateEnd == null) {
                // empty date (NULL value)
                whereStringBuilder.append(" is null");
            } else {
                // less than
                whereStringBuilder.append(" < :").append(placeholderEnd);
            }
        } else {
            if (dateEnd == null) {
                // more than
                whereStringBuilder.append(" > :").append(placeholderStart);
            } else {
                // between
                whereStringBuilder.append(" between :");
                whereStringBuilder.append(placeholderStart);
                whereStringBuilder.append(" and :");
                whereStringBuilder.append(placeholderEnd);
            }
        }

        if (dateStart != null) {
            placeholders.put(placeholderStart, new QueryParameter(placeholderStart, dateStart, Hibernate.TIMESTAMP));
        }
        if (dateEnd != null) {
            placeholders.put(placeholderEnd, new QueryParameter(placeholderEnd, dateEnd, Hibernate.TIMESTAMP));
        }

        whereStringBuilder.append(" ");
        return whereStringBuilder.toString();
    }
}
