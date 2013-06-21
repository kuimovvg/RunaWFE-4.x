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

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.common.web.Messages;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/*
 * Created on 20.08.2004
 */
public class ExecutorTableBuilder extends BaseDetailTableBuilder {
    private final boolean isEmpty;

    private Executor executor;

    private final boolean isActor;

    private boolean areInputsDisabled = false;

    private final PageContext pageContext;

    /**
     * Use for update
     * 
     * @param executor
     *            executor for update
     */
    public ExecutorTableBuilder(Executor executor, boolean areInputsDisabled, PageContext pageContext) {
        this.executor = executor;
        this.pageContext = pageContext;
        isActor = (executor instanceof Actor);
        isEmpty = false;
        this.areInputsDisabled = areInputsDisabled;
    }

    /**
     * Use for create
     * 
     * @param isActor
     *            type of table
     */
    public ExecutorTableBuilder(boolean isActor, PageContext pageContext) {
        this.isActor = isActor;
        isEmpty = true;
        this.pageContext = pageContext;
    }

    public Table buildTable() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        table.addElement(createTRWith2TDRequired(Messages.getMessage(Messages.LABEL_EXECUTOR_NAME, pageContext),
                UpdateExecutorDetailsForm.NEW_NAME_INPUT_NAME, isEmpty || executor == null ? "" : executor.getName(), areInputsDisabled, Input.TEXT));
        if (isActor) {
            table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_ACTOR_FULL_NAME, pageContext),
                    UpdateExecutorDetailsForm.FULL_NAME_INPUT_NAME, isEmpty ? "" : ((Actor) executor).getFullName(), areInputsDisabled));
        }
        table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_EXECUTOR_DESCRIPTION, pageContext),
                UpdateExecutorDetailsForm.DESCRIPTION_INPUT_NAME, isEmpty ? "" : executor.getDescription(), areInputsDisabled));
        if (isActor) {
            table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_ACTOR_CODE, pageContext), UpdateExecutorDetailsForm.CODE_INPUT_NAME,
                    isEmpty ? "" : String.valueOf(((Actor) executor).getCode()), areInputsDisabled));
            table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_ACTOR_EMAIL, pageContext),
                    UpdateExecutorDetailsForm.EMAIL_INPUT_NAME,
                    isEmpty || ((Actor) executor).getEmail() == null ? "" : String.valueOf(((Actor) executor).getEmail()), areInputsDisabled));
            table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_ACTOR_PHONE, pageContext),
                    UpdateExecutorDetailsForm.PHONE_INPUT_NAME,
                    isEmpty || ((Actor) executor).getPhone() == null ? "" : ((Actor) executor).getPhone(), areInputsDisabled));
        } else {
            table.addElement(createTRWith2TD(
                    Messages.getMessage(Messages.LABEL_GROUP_AD, pageContext),
                    UpdateExecutorDetailsForm.EMAIL_INPUT_NAME,
                    isEmpty ? ""
                            : ((Group) executor).getLdapGroupName() != null ? String.valueOf(((Group) executor).getLdapGroupName())
                                    : "", areInputsDisabled));
        }
        return table;
    }
}
