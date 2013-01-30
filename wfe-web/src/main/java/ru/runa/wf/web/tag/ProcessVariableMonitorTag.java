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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.html.ProcessVariablesRowBuilder;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * Created on 29.11.2004
 * 
 * 
 * @jsp.tag name = "processVariableMonitor" body-content = "empty"
 */
public class ProcessVariableMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 161759402000861245L;

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            List<String> headerNames = Lists.newArrayList();
            headerNames.add(Messages.getMessage(Messages.LABEL_VARIABLE_NAME, pageContext));
            headerNames.add(Messages.getMessage(Messages.LABEL_VARIABLE_TYPE, pageContext));
            if (WebResources.isDisplayVariablesJavaType()) {
                headerNames.add("Java " + Messages.getMessage(Messages.LABEL_VARIABLE_TYPE, pageContext));
            }
            headerNames.add(Messages.getMessage(Messages.LABEL_VARIABLE_VALUE, pageContext));
            HeaderBuilder headerBuilder = new StringsHeaderBuilder(headerNames);
            ExecutionService executionService = Delegates.getExecutionService();
            List<WfVariable> variables = executionService.getVariables(getUser(), getIdentifiableId());
            RowBuilder rowBuilder = new ProcessVariablesRowBuilder(getIdentifiableId(), variables, pageContext);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_INSANCE_VARIABLE_LIST, pageContext);
    }
}
