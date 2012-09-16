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

import org.apache.ecs.Element;
import org.apache.ecs.html.TD;

import ru.runa.af.Permission;
import ru.runa.common.web.Messages;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.LogPresentationBuilder;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.action.CancelProcessInstanceAction;
import ru.runa.wf.web.logs.HtmlLogPresentationBuilder;

/**
 * 
 * @jsp.tag name = "showHistoryForm" body-content = "JSP"
 */
public class ShowHistoryFormTag extends ProcessInstanceBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            LogPresentationBuilder builder = new HtmlLogPresentationBuilder(tdFormElement, pageContext, getSubject());
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            List<Element> result = (List<Element>) executionService.getInvocationLogs(getSubject(), getIdentifiableId(), builder);
            for (Element element : result) {
                tdFormElement.addElement(element);
            }
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
        return Messages.getMessage(Messages.TITLE_HISTORY, pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessInstanceAction.ACTION_PATH;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }
}
