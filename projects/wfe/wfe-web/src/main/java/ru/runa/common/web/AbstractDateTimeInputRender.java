package ru.runa.common.web;

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

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;

public abstract class AbstractDateTimeInputRender {
    private final static String CALENDAR_RENDERED_ATTRIBUTE_NAME = AbstractDateTimeInputRender.class.getName();
    private static final String CSS = "/calendar/calendar-blue.css";
    private static final String CALENDAR_JS = "/calendar/calendar.js";
    private static final String CALENDAR_SETUP_JS = "/calendar/calendar-setup.js";
    private static final String CALENDAR_HELPER_JS = "/calendar/calendar-helper.js";
    private static final String CALENDAR_BUTTON_IMAGE = "/images/calendar.gif";
    public static final String VAR_TAG_DATE_TIME_INPUT_LOCALIZATION_JS_PATH = "var_tag.date_time_input.localization_js";

    public String getHtml(Subject subject, String varName, Date dateValue, PageContext pageContext) {
        try {
            StringBuilder sb = new StringBuilder();

            if (pageContext != null && pageContext.getAttribute(CALENDAR_RENDERED_ATTRIBUTE_NAME) == null) {
                Link style = new Link();
                style.setType(Style.CSS);
                style.setHref(Commons.getUrl(CSS, pageContext, PortletUrlType.Resource));
                style.setRel("stylesheet");
                sb.append(style.toString());

                Script calendarHelperJsScript = new Script();
                calendarHelperJsScript.setSrc(Commons.getUrl(CALENDAR_HELPER_JS, pageContext, PortletUrlType.Resource));
                sb.append(calendarHelperJsScript.toString());

                Script calendarJsScript = new Script();
                calendarJsScript.setSrc(Commons.getUrl(CALENDAR_JS, pageContext, PortletUrlType.Resource));
                sb.append(calendarJsScript.toString());

                Script calendarL10NJsScript = new Script();
                String l10nfile = Commons.getMessage(VAR_TAG_DATE_TIME_INPUT_LOCALIZATION_JS_PATH, pageContext);
                calendarL10NJsScript.setSrc(Commons.getUrl(l10nfile, pageContext, PortletUrlType.Resource));
                sb.append(calendarL10NJsScript.toString());

                Script calendarSetupJsScript = new Script();
                calendarSetupJsScript.setSrc(Commons.getUrl(CALENDAR_SETUP_JS, pageContext, PortletUrlType.Resource));
                sb.append(calendarSetupJsScript.toString());

                pageContext.setAttribute(CALENDAR_RENDERED_ATTRIBUTE_NAME, Boolean.TRUE);
            }

            Input input = new Input(Input.TEXT, varName);
            String inputId = varName + String.valueOf(System.currentTimeMillis() * Math.random());
            input.setID(inputId);
            if (dateValue != null) {
                input.setValue(CalendarUtil.format(dateValue, getFormat()));
            }

            sb.append(input.toString());

            if (pageContext != null) {
                IMG img = new IMG();
                String imageId = inputId + "img";
                img.setID(imageId);
                img.setSrc(Commons.getUrl(CALENDAR_BUTTON_IMAGE, pageContext, PortletUrlType.Resource));
                img.setOnClick(getConfiguration(inputId, imageId));
                sb.append(img.toString());
            }
            return sb.toString();
        } catch (JspException e) {
            throw new InternalApplicationException(e);
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
