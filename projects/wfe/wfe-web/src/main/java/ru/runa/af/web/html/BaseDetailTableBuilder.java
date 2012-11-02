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
package ru.runa.af.web.html;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

public abstract class BaseDetailTableBuilder {

    protected TR createTRWith2TD(String label, String name, String value, boolean areInputsDisabled) {
        return createTRWith2TD(label, name, value, areInputsDisabled, Input.TEXT);
    }

    protected TR createTRWith2TDRequired(String label, String name, String value, boolean areInputsDisabled, String type) {
        return createTRWith2TD(label + " <span style='color: #990000'>*</span>", name, value, areInputsDisabled, type);
    }

    protected TR createTRWithLabelAndCheckbox(String label, String name, boolean isChecked, boolean areInputsDisabled) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new Input(Input.CHECKBOX, name).setChecked(isChecked).setDisabled(areInputsDisabled))
                .setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    protected TR createTRWithLabelAndSelect(String label, String name, Option[] options, boolean areInputsDisabled) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        Select select = new Select(name, options).setDisabled(areInputsDisabled);
        select.setID(name);
        tr.addElement(new TD(select).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    protected TR createTRWith2TD(String label, String name, String value, boolean areInputsDisabled, String type) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new Input(type, name, value).setDisabled(areInputsDisabled)).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }
}
