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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;

import ru.runa.af.Identifiable;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;

public abstract class ProcessInstanceBaseFormTag extends IdentifiableFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Identifiable getIdentifiable() throws JspException {
        return getProcessInstance();
    }

    protected ProcessInstanceStub getProcessInstance() throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Subject subject = getSubject();
            return executionService.getProcessInstanceStub(subject, getIdentifiableId());
        } catch (Exception e) {
            return null;
        }
    }
}
