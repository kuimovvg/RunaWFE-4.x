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
package ru.runa.wf.web.ftl.method;

import java.text.DateFormat;
import java.util.Date;

import ru.runa.common.web.AbstractDateTimeInputRender;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DateTimeInputTag extends FreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        String view = getParameterAs(String.class, 1);

        Date varValue = getVariableAs(Date.class, varName, true);

        if ("time".equals(view)) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<input type=\"text\" length=\"5\" name=\"").append(varName).append("\" ");
            if (varValue != null) {
                buffer.append("value=\"").append(varValue).append("\" ");
            }
            buffer.append("/>");
            return buffer.toString();
        } else {
            CalendarRenderer calendarRenderer;
            if ("date".equals(view)) {
                calendarRenderer = new CalendarRenderer(CalendarUtil.DATE_WITHOUT_TIME_FORMAT, "%d.%m.%Y", false);
            } else if ("datetime".equals(view)) {
                calendarRenderer = new CalendarRenderer(CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT, "%d.%m.%Y %H:%M", true);
            } else {
                throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
            }
            return calendarRenderer.getHtml(subject, varName, varValue, pageContext);
        }
    }

    static class CalendarRenderer extends AbstractDateTimeInputRender {
        private final DateFormat format;
        private final String pattern;
        private final boolean showTime;

        public CalendarRenderer(DateFormat format, String pattern, boolean showTime) {
            this.format = format;
            this.pattern = pattern;
            this.showTime = showTime;
        }

        @Override
        protected DateFormat getFormat() {
            return format;
        }

        @Override
        protected String getPattern() {
            return pattern;
        }

        @Override
        protected boolean isShowTime() {
            return showTime;
        }

    }
}
