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
package ru.runa.wf.web.ftl.tags;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.wf.FileVariable;
import ru.runa.wf.web.ftl.FreemarkerTag;

import com.google.common.base.Charsets;

import freemarker.template.TemplateModelException;

public class FileTag extends FreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        String view = getParameterAs(String.class, 1);

        FileVariable fv = (FileVariable) variables.get(varName);
        if ("content".equals(view)) {
            return new String(fv.getData(), Charsets.UTF_8);
        } else if ("contentlength".equals(view)) {
            return fv.getData().length;
        } else if ("contenttype".equals(view)) {
            return fv.getContentType();
        } else if ("raw".equals(view)) {
            return fv;
        } else if ("drawimage".equals(view)) {
            String fileName = fv.getName();
            pageContext.getSession().setAttribute(fileName, fv.getData());
            String actionUrl = Commons.getActionUrl("/getSessionImage", "fileName", fileName, pageContext, PortletUrl.Render);
            return "<img src='" + actionUrl + "' />";
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

}
