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

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.TaskStub;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.form.TaskIdForm;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class TaskProcessInstanceIdTDBuilder implements TDBuilder {

    public TaskProcessInstanceIdTDBuilder() {
    }

    public TD build(Object object, Env env) {
        TaskStub taskStub = (TaskStub) object;
        Long instanceId = taskStub.getProcessInstanceId();
        ConcreteElement instanceLink = new StringElement(instanceId.toString());
        boolean isAllowed = false;
        try {
            isAllowed = env.isAllowed(Permission.READ, new IdentifiableExtractor() {
                public Identifiable getIdentifiable(final Object o, final Env env) {
                    return new Identifiable() {
                        @Override
                        public int identifiableType() {
                            return ProcessInstanceStub.class.getName().hashCode();
                        }

                        @Override
                        public Long getId() {
                            return ((TaskStub) o).getProcessInstanceId();
                        }
                    };
                }

                public int hashCode() {
                    return 156249887;
                }

                public boolean equals(Object other) {
                    return (other != null) && other.getClass().equals(this.getClass());
                }
            });
        } catch (Exception e) {
        }
        if (isAllowed) {
            Map<String, String> params = new HashMap<String, String>();
            if (taskStub.getSuperProcessId() == null) {
                params.put(IdForm.ID_INPUT_NAME, String.valueOf(instanceId));
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, String.valueOf(taskStub.getId()));
            } else {
                params.put(IdForm.ID_INPUT_NAME, String.valueOf(taskStub.getSuperProcessId()));
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, String.valueOf(taskStub.getId()));
            }
            String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessInstanceAction(), params, env.getPageContext(), PortletUrl.Render);
            instanceLink = new A(url, instanceLink);
        }
        TD td = new TD(instanceLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    public String getValue(Object object, Env env) {
        TaskStub taskStub = (TaskStub) object;
        return Long.toString(taskStub.getProcessInstanceId());
    }

    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
