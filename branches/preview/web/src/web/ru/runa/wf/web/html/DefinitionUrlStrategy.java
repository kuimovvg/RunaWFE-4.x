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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.wf.ProcessDefinition;

public class DefinitionUrlStrategy implements ItemUrlStrategy {
    private final PageContext pageContext;

    public DefinitionUrlStrategy(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getUrl(String baseUrl, Object item) {
        ProcessDefinition definition = (ProcessDefinition) item;
        Long definitionId = definition.getNativeId();
        Map<String, String> map = new HashMap<String, String>();
        map.put(IdForm.ID_INPUT_NAME, String.valueOf(definitionId));
        return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrl.Action);
    }
}
