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

import java.util.List;

import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.html.ProcessSwimlaneAssignmentRowBuilder;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Created on 29.11.2004
 * 
 * @jsp.tag name = "processSwimlaneAssignmentMonitor" body-content = "empty"
 */
public class ProcessSwimlaneAssignmentMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = -6604411496585798625L;
    private String swimlaneName;

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

    public void setSwimlaneName(String swimlaneName) {
        this.swimlaneName = swimlaneName;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        try {
            ExecutionService executionService = Delegates.getExecutionService();
            List<WfTask> activeTasks = executionService.getActiveTasks(getUser(), getIdentifiableId());
            List<WfTask> filteredTasks = Lists.newArrayList();
            for (WfTask task : activeTasks) {
                if (Objects.equal(swimlaneName, task.getSwimlaneName())) {
                    filteredTasks.add(task);
                }
            }
            HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { Messages.getMessage(Messages.LABEL_STATE_NAME, pageContext),
                    Messages.getMessage(Messages.LABEL_EXECUTOR_NAME, pageContext) });
            RowBuilder rowBuilder = new ProcessSwimlaneAssignmentRowBuilder(filteredTasks, pageContext);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        } catch (ExecutorDoesNotExistException e) {
            // i was against this crap, but was urged to
            Span span = new Span();
            span.setClass(ru.runa.common.web.Resources.CLASS_ERROR);
            span.addElement(Commons.getMessage(Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, pageContext,
                    new Object[] { e.getExecutorName() }));
            tdFormElement.addElement(span);
        }
    }

}
