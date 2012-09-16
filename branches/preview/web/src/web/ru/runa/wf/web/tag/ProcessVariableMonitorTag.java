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

import java.util.ArrayList;
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
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.VariableStub;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.forms.format.StringFormat;
import ru.runa.wf.web.html.ProcessInstanceVariablesRowBuilder;

/**
 * Created on 29.11.2004
 * 
 * 
 * @jsp.tag name = "processVariableMonitor" body-content = "empty"
 */
public class ProcessVariableMonitorTag extends ProcessInstanceBaseFormTag {

    private static final long serialVersionUID = 161759402000861245L;

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            String nameHeader = Messages.getMessage(Messages.LABEL_VARIABLE_NAME, pageContext);
            String typeHeader = Messages.getMessage(Messages.LABEL_VARIABLE_TYPE, pageContext);
            String valueHeader = Messages.getMessage(Messages.LABEL_VARIABLE_VALUE, pageContext);
            HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { nameHeader, typeHeader, valueHeader });
            RowBuilder rowBuilder = new ProcessInstanceVariablesRowBuilder(getIdentifiableId(), getVariables(), pageContext);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Loads variables, presents in process instance. If variable is declared, but not set, it wouldn't loaded (only really set variables is loaded).
     * 
     * @return Variables instances, stored in process instance.
     */
    private List<VariableStub> getVariables() throws ProcessInstanceDoesNotExistException, AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, JspException {
        DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
        List<VariableDefinition> variableDefinitions = definitionService.getProcessDefinitionVariables(getSubject(), getProcessInstance()
                .getProcessDefinitionNativeId());
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        Map<String, Object> variables = executionService.getInstanceVariables(getSubject(), super.getIdentifiableId());
        List<VariableStub> result = new ArrayList<VariableStub>(variables.size());
        for (VariableDefinition variableDefinition : variableDefinitions) {
            Object value = variables.remove(variableDefinition.getName());
            if (value != null) {
                result.add(new VariableStub(variableDefinition, value));
            }
        }
        for (Map.Entry<String, Object> variable : variables.entrySet()) {
            VariableDefinition variableDefinition = new VariableDefinition(variable.getKey(), StringFormat.class.getName());
            result.add(new VariableStub(variableDefinition, variable.getValue()));
        }
        return result;
    }

    @Override
    protected Permission getPermission() {
        return ProcessInstancePermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_INSANCE_VARIABLE_LIST, pageContext);
    }
}
