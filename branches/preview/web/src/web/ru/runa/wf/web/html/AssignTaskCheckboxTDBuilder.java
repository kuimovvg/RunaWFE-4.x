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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.common.web.html.CheckboxTDBuilder;
import ru.runa.wf.TaskStub;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class AssignTaskCheckboxTDBuilder extends CheckboxTDBuilder {

    boolean enableControl = true;

    public AssignTaskCheckboxTDBuilder() {
        super(null, null);
    }

    public AssignTaskCheckboxTDBuilder(boolean enableControl) {
        super(null, null);
        this.enableControl = enableControl;
    }

    public TD build(Object object, Env env) throws JspException {
        Input input = new Input(Input.CHECKBOX, StrIdsForm.IDS_INPUT_NAME, getIdValue(object));

        if (!isEnabled(object, env)) {
            input.setDisabled(true);
        }
        if (isChecked(object, env)) {
            input.setChecked(true);
        }

        TD td = new TD(input);

        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    protected boolean isEnabled(Object object, Env env) throws JspException {
        TaskStub task = (TaskStub) object;
        return task.isGroupAssigned() && enableControl;
    }

    protected boolean isChecked(Object object, Env env) {
        TaskStub task = (TaskStub) object;
        return !task.isGroupAssigned();
    }

    public String getValue(Object object, Env env) {
        return "";
    }

    protected String getIdValue(Object object) throws JspException {
        TaskStub task = (TaskStub) object;
        return String.valueOf(task.getId()) + ":" + task.getName() + ":" + String.valueOf(task.getTargetActor().getId());
    }
}
