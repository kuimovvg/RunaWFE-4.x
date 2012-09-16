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

import java.util.Date;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.forms.format.DateTimeFormat;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
abstract public class ProcessInstanceBaseDateTDBuilder implements TDBuilder {

    public ProcessInstanceBaseDateTDBuilder() {
    }

    public TD build(Object object, Env env) {
        ConcreteElement dateElement = new StringElement();
        if (hasDate((ProcessInstanceStub) object)) {
            String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessInstanceAction(), IdForm.ID_INPUT_NAME, String
                    .valueOf(((ProcessInstanceStub) object).getId()), env.getPageContext(), PortletUrl.Render);
            String dateText = new DateTimeFormat().format(getDate((ProcessInstanceStub) object, false));
            dateElement = new A(url, dateText);
        }
        TD td = new TD(dateElement);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    public String getValue(Object object, Env env) {
        if (hasDate((ProcessInstanceStub) object)) {
            return new DateTimeFormat().format(getDate((ProcessInstanceStub) object, false));
        }
        return "";
    }

    abstract protected boolean hasDate(ProcessInstanceStub processInstanceStub);

    abstract protected Date getDate(ProcessInstanceStub processInstanceStub);

    Date getDate(ProcessInstanceStub processInstanceStub, boolean fake) {
        Date result = getDate(processInstanceStub);
        if (result == null) {
            return new Date();
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
