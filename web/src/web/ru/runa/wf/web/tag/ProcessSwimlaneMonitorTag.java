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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.html.ProcessInstanceSwimlaneRowBuilder;

/**
 * Created on 29.11.2004
 * 
 * 
 * @jsp.tag name = "processSwimlaneMonitor" body-content = "empty"
 */
public class ProcessSwimlaneMonitorTag extends ProcessInstanceBaseFormTag {

    private static final long serialVersionUID = -5024428545159087986L;

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            List<SwimlaneStub> swimlanes = executionService.getSwimlanes(getSubject(), super.getIdentifiableId());

            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            Map<String, String> orgFunctionMappings = new HashMap<String, String>();
            try {
                orgFunctionMappings = definitionService.getOrgFunctionFriendlyNamesMapping(getSubject(), super.getProcessInstance()
                        .getProcessDefinitionNativeId());
            } catch (AuthenticationException e) {
            } catch (AuthorizationException e) {
            }

            HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { Messages.getMessage(Messages.LABEL_SWIMLANE_NAME, pageContext),
                    Messages.getMessage(Messages.LABEL_SWIMLANE_ASSIGNMENT, pageContext),
                    Messages.getMessage(Messages.LABEL_SWIMLANE_ORGFUNCTION, pageContext) });

            RowBuilder rowBuilder = new ProcessInstanceSwimlaneRowBuilder(getIdentifiableId(), swimlanes, orgFunctionMappings, pageContext);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected Permission getPermission() {
        return ProcessInstancePermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_INSANCE_SWINLANE_LIST, pageContext);
    }
}
