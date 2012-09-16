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
package ru.runa.wf.web.ftl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wf.web.html.ReflectionVarTagFactory;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;
import freemarker.template.TemplateModelException;

public class VarTagMethodModel extends FreemarkerTag {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(VarTagMethodModel.class);
    private VarTag varTag;

    public void setVartagClassName(String vartagClassName) throws WorkflowFormProcessingException {
        try {
            varTag = ReflectionVarTagFactory.create(vartagClassName);
        } catch (WorkflowFormProcessingException e) {
            log.error("", e);
        }
    }

    public VarTag getVarTag() {
        return varTag;
    }

    @Override
    protected Object executeTag() throws TemplateModelException {
        String name = getParameterAs(String.class, 0);
        Object value = variables.get(name);
        try {
            return varTag.getHtml(subject, name, value, pageContext);
        } catch (Exception e) {
            log.error("", e);
            throw new TemplateModelException(e);
        }
    }

}
