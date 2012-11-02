package ru.runa.wf.web.tag;

import java.util.Date;

import javax.servlet.jsp.JspException;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.common.web.Messages;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.Task;

/**
 * Renders Gantt diagram.
 * 
 * @author Dofs
 * @jsp.tag name = "showGanttDiagram" body-content = "JSP"
 */
public class ShowGanttDiagramTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        String js = "";
        try {
            ExecutionService executionService = DelegateFactory.getExecutionService();
            ProcessLogFilter filter = new ProcessLogFilter(getIdentifiableId());
            ProcessLogs logs = executionService.getProcessLogs(getSubject(), filter);
            for (ProcessLog log : logs.getLogs()) {
                if (log instanceof TaskCreateLog) {
                    Task task = null; // ((TaskCreateLog) log).getTask();
                    js += "g.AddTaskItem(new JSGantt.TaskItem(" + task.getId() + ",'" + task.getName() + "','"
                            + CalendarUtil.formatDate(task.getCreateDate()) + "','";
                    if (task.isActive()) {
                        js += CalendarUtil.formatDate(new Date());
                    } else {
                        js += CalendarUtil.formatDate(task.getEndDate());
                    }
                    js += "','ff0000','',0,'";
                    if (task.getExecutor() != null) {
                        js += ExecutorNameConverter.getName(task.getExecutor(), pageContext);
                    }
                    js += "',0,0,0,1));\n";
                }
            }
        } catch (Exception e) {
            handleException(e);
        }

        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");
        script.addElement(new StringElement(js));
        tdFormElement.addElement(script);
    }

    @Override
    protected Permission getPermission() throws JspException {
        return ProcessPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.LABEL_SHOW_GANTT_DIAGRAM, pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessAction.ACTION_PATH;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }
}
