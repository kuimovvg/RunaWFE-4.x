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
package ru.runa.wf.web.html;

import java.text.DateFormat;
import java.util.Date;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Link;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Style;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;

/**
 * Created 12.05.2005
 * 
 */
public abstract class AbstractDateTimeInputRender implements VarTag {
    private final static String CALENDAR_RENDERED_ATTRIBUTE_NAME = AbstractDateTimeInputRender.class.getName();

    private static final String CSS = "/calendar/calendar-blue.css";

    private static final String CALENDAR_JS = "/calendar/calendar.js";

    private static final String CALENDAR_SETUP_JS = "/calendar/calendar-setup.js";

    private static final String CALENDAR_HELPER_JS = "/calendar/calendar-helper.js";

    private static final String CALENDAR_BUTTON_IMAGE = "/images/calendar.gif";

    public static final String VAR_TAG_DATE_TIME_INPUT_LOCALIZATION_JS_PATH = "var_tag.date_time_input.localization_js";

    public String getHtml(Subject subject, String varName, Object varValue, PageContext pageContext) throws WorkflowFormProcessingException {
        try {
            StringBuilder sb = new StringBuilder();

            if (pageContext != null && pageContext.getAttribute(CALENDAR_RENDERED_ATTRIBUTE_NAME) == null) {
                Link style = new Link();
                style.setType(Style.CSS);
                style.setHref(Commons.getUrl(CSS, pageContext, PortletUrl.Resource));
                style.setRel("stylesheet");
                sb.append(style.toString());

                Script calendarHelperJsScript = new Script();
                calendarHelperJsScript.setSrc(Commons.getUrl(CALENDAR_HELPER_JS, pageContext, PortletUrl.Resource));
                sb.append(calendarHelperJsScript.toString());

                Script calendarJsScript = new Script();
                calendarJsScript.setSrc(Commons.getUrl(CALENDAR_JS, pageContext, PortletUrl.Resource));
                sb.append(calendarJsScript.toString());

                Script calendarL10NJsScript = new Script();
                String l10nfile = Commons.getMessage(VAR_TAG_DATE_TIME_INPUT_LOCALIZATION_JS_PATH, pageContext);
                calendarL10NJsScript.setSrc(Commons.getUrl(l10nfile, pageContext, PortletUrl.Resource));
                sb.append(calendarL10NJsScript.toString());

                Script calendarSetupJsScript = new Script();
                calendarSetupJsScript.setSrc(Commons.getUrl(CALENDAR_SETUP_JS, pageContext, PortletUrl.Resource));
                sb.append(calendarSetupJsScript.toString());

                pageContext.setAttribute(CALENDAR_RENDERED_ATTRIBUTE_NAME, Boolean.TRUE);
            }

            Input input = new Input(Input.TEXT, varName);
            String inputId = varName + String.valueOf(System.currentTimeMillis() * Math.random());
            input.setID(inputId);
            if (varValue instanceof Date) {
                input.setValue(getFormat().format((Date) varValue));
            }

            sb.append(input.toString());

            if (pageContext != null) {
                IMG img = new IMG();
                String imageId = inputId + "img";
                img.setID(imageId);
                img.setSrc(Commons.getUrl(CALENDAR_BUTTON_IMAGE, pageContext, PortletUrl.Resource));
                img.setOnClick(getConfiguration(inputId, imageId));
                sb.append(img.toString());
            }
            return sb.toString();
        } catch (JspException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    protected abstract DateFormat getFormat();

    protected String getConfiguration(String inputId, String buttonId) {
        return "return showCalendar('" + inputId + "', '" + getPattern() + "' " + (isShowTime() ? ", '24'" : "") + ");";
    }

    /**
     * Pattern string for JavaScript calendar
     * 
     * @return
     */
    protected abstract String getPattern();

    /**
     * Show time dialog
     */
    protected boolean isShowTime() {
        return false;
    }
}
