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
package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.web.action.CreateExecutorAction;
import ru.runa.af.web.form.CreateExecutorForm;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.FormTag;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "createExecutorForm" body-content = "empty"
 */
public class CreateExecutorFormTag extends FormTag {
    private static final long serialVersionUID = 8049519129092850184L;
    boolean isActor;

    public void setType(String type) {
        isActor = CreateExecutorForm.TYPE_ACTOR.equals(type);
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true" description = "actor|group"
     */
    public String getType() {
        return isActor ? CreateExecutorForm.TYPE_ACTOR : CreateExecutorForm.TYPE_GROUP;
    }

    public void fillFormElement(TD tdFormElement) {
        ExecutorTableBuilder exetb = new ExecutorTableBuilder(isActor, pageContext);
        tdFormElement.addElement(exetb.buildTable());
        tdFormElement.addElement(createHiddenType());
    }

    private Input createHiddenType() {
        return new Input(Input.HIDDEN, CreateExecutorForm.EXECUTOR_TYPE_INPUT_NAME, isActor ? CreateExecutorForm.TYPE_ACTOR
                : CreateExecutorForm.TYPE_GROUP);
    }

    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    public String getAction() {
        return CreateExecutorAction.ACTION_PATH;
    }
}
