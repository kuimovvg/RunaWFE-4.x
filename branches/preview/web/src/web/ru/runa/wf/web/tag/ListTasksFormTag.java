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
package ru.runa.wf.web.tag;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.InternalApplicationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.action.ProcessTaskAssignmentAction;
import ru.runa.wf.web.html.AssignTaskCheckboxTDBuilder;
import ru.runa.wf.web.html.TaskUrlStrategy;

/**
 * Created on 15.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 * @jsp.tag name = "listTasksForm" body-content = "JSP"
 */
public class ListTasksFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = -6863052817853155919L;

    private static boolean isButtonEnabled;

    private static final String[] NO_PREFIX_HEADER_NAMES = new String[0];

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Subject subject = getSubject();
            BatchPresentation batchPresentation = getBatchPresentation();
            boolean isTaskTableBuild = false;
            while (!isTaskTableBuild) {
                try {
                    List<TaskStub> tasks = executionService.getTasks(subject, batchPresentation);
                    Table table = buildTasksTable(pageContext, batchPresentation, tasks, getReturnAction(), false);

                    PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, tasks.size());
                    navigation.addPagingNavigationTable(tdFormElement);
                    tdFormElement.addElement(table);
                    navigation.addPagingNavigationTable(tdFormElement);

                    isTaskTableBuild = true;
                } catch (InternalApplicationException e) {
                    if (e.getCause() == null || !(e.getCause() instanceof TaskDoesNotExistException || e.getCause() instanceof SQLException)) {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public static Table buildTasksTable(PageContext pageContext, BatchPresentation batchPresentation, List<TaskStub> tasks, String returnAction,
            boolean disableCheckbox) throws JspException {
        isButtonEnabled = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isGroupAssigned()) {
                if (!disableCheckbox) {
                    isButtonEnabled = true;
                    break;
                }
            }
        }

        TDBuilder[] builders = getBuilders(new TDBuilder[] { new AssignTaskCheckboxTDBuilder(!disableCheckbox) }, batchPresentation,
                new TDBuilder[] {});

        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, NO_PREFIX_HEADER_NAMES.length, returnAction, pageContext);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(tasks, batchPresentation, pageContext,
                Resources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, returnAction, new TaskUrlStrategy(pageContext), builders);
        rowBuilder.setCssClassStrategy(new TasksCssClassStrategy());
        return new TableBuilder().build(headerBuilder, rowBuilder);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return isButtonEnabled;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_TASKS, pageContext);
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_ACCEPT_TASK, pageContext);
    }

    @Override
    public String getAction() {
        return ProcessTaskAssignmentAction.ACTION_PATH;
    }

    public static class TasksCssClassStrategy implements CssClassStrategy {

        public String getClassName(Object item, Subject subject) {
            if (((TaskStub) item).getDeadline() == null) {
                return null;
            }
            TaskStub taskStub = (TaskStub)item;
            
            Date deadline = taskStub.getDeadline();
            Date warningDate = TaskStub.calculateAlmostDeadlineDate(taskStub.getCreationDate(), deadline);
                        
            String t = "deadlineExists";
            if (warningDate.before(new Date())) t = "deadlineAlmostExpired";
        	if (deadline.before(new Date())) t = "deadlineExpired";
        	if (taskStub.isEscalated()) t = "escalatedTask";
            return t;
        }

        public String getCssStyle(Object item) {
            if (((TaskStub) item).isFirstOpen()) {
                return "font-weight: bold;";
            }
            return null;
        }
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.ACCEPT_TASK_PARAMETER;
    }
}
