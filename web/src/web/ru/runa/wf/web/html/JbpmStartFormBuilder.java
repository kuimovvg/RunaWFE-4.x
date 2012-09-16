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
package ru.runa.wf.web.html;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.af.AuthenticationException;
import ru.runa.wf.form.Interaction;

import com.google.common.base.Charsets;

public class JbpmStartFormBuilder implements StartFormBuilder {

    @Override
    public String build(Subject subject, Long definitionId, PageContext pageContext, Interaction interaction) throws AuthenticationException, WorkflowFormProcessingException {
        try {
            JbpmHtmlTaskFormParser parser = new JbpmHtmlTaskFormParser(subject, pageContext, interaction);
            parser.setDefinitionId(definitionId);
            byte[] formBytes = parser.getParsedFormBytes();
            return new String(formBytes, Charsets.UTF_8);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

}
