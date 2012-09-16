package ru.runa.wf.web.logs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.Element;
import org.apache.ecs.html.TD;

import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.wf.LogPresentationBuilder;

public class HtmlLogPresentationBuilder implements LogPresentationBuilder, Serializable {

    public static final long serialVersionUID = 1L;
    private final PageContext pageContext;
    private final Subject subject;

    public HtmlLogPresentationBuilder(TD tdFormElement, PageContext pageContext, Subject subject) {
        this.pageContext = pageContext;
        this.subject = subject;
    }

    public List<Element> processLogs(final List<ProcessLog> logs) throws Exception {
        try {
            if (logs.isEmpty()) {
                return new ArrayList<Element>();
            }
            List<Element> resState = BaseState.acceptLog(subject, pageContext, new LogIterator(logs));
            return resState;
        } catch (Exception e) {
            throw new JspException(e);
        }
    }
}
