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

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.web.action.LoadProcessDefinitionHtmlFileAction;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DescriptionProcessTDBuilder implements TDBuilder {

    public DescriptionProcessTDBuilder() {
    }

    public TD build(Object object, Env env) {
        ProcessDefinition definitionStub = (ProcessDefinition) object;
        ConcreteElement descriptionLink;
        if (definitionStub.hasHtmlDescription()) {
            String url = Commons.getActionUrl(LoadProcessDefinitionHtmlFileAction.ACTION_PATH, IdForm.ID_INPUT_NAME,
                    String.valueOf(definitionStub.getNativeId()), env.getPageContext(), PortletUrl.Render);
            descriptionLink = new A(url, definitionStub.getDescription());
        } else {
            descriptionLink = new StringElement(definitionStub.getDescription());
        }
        TD td = new TD(descriptionLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    public String getValue(Object object, Env env) {
        ProcessDefinition pd = (ProcessDefinition) object;
        String result = pd.getDescription();
        if (result == null) {
            result = "";
        }
        return result;
    }
    
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
