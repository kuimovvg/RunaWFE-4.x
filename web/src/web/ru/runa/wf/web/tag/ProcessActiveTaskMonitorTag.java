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
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.html.ProcessInstanceSwimlaneAssignmentRowBuilder;

import com.google.common.collect.Maps;

/**
 * Created on 29.11.2004
 * 
 * @jsp.tag name = "processActiveTaskMonitor" body-content = "empty"
 */
public class ProcessActiveTaskMonitorTag extends ProcessInstanceBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, List<Executor>> swimlaneExecutorsMap = Maps.newHashMap();
            for (SwimlaneStub swimlane : executionService.getSwimlanes(getSubject(), getIdentifiableId())) {
                swimlaneExecutorsMap.putAll(executionService.getSwimlaneExecutorMap(getSubject(), getIdentifiableId(), swimlane.getId()));
            }
            HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { Messages.getMessage(Messages.LABEL_STATE_NAME, pageContext),
                    Messages.getMessage(Messages.LABEL_SWIMLANE_ASSIGNMENT, pageContext) });
            RowBuilder rowBuilder = new ProcessInstanceSwimlaneAssignmentRowBuilder(swimlaneExecutorsMap, pageContext);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected Permission getPermission() throws JspException {
        return ProcessInstancePermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_INSANCE_TASKS_LIST, pageContext);
    }
}
