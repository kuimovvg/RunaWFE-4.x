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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.CreateRelationAction;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.FormTag;
import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

/**
 * @jsp.tag name = "createRelationLeftExecutorForm" body-content = "empty"
 */
public class CreateRelationLeftExecutorFormTag extends FormTag {

    private static final long serialVersionUID = 1L;

    public static final String relationFromName = "relationFrom";

    public static final String relationToName = "relationTo";

    private String relationName;

    private Long executorId;

    @Override
    public String getAction() {
        return CreateRelationAction.ACTION_PATH;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage("button.create_relation", pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
        return true;
    }

    @Override
    protected boolean isFormButtonEnabled(Identifiable identifiable, Permission permission) throws JspException {
        return true;
    }

    @Override
    protected boolean isFormButtonVisible() throws JspException {
        return true;
    }

    @Override
    protected boolean isMultipleSubmit() {
        return false;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        Table table = new Table();
        TR tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_CREATE_RELATION_FROM, pageContext)));
        tr.addElement(new ActorSelectTD(getSubject(), relationFromName, getExecutorName(), getExecutors()));
        table.addElement(tr);
        tr = new TR();
        tr.addElement(new TD(Messages.getMessage(Messages.LABEL_CREATE_RELATION_TO, pageContext)));
        tr.addElement(new ActorSelectTD(getSubject(), relationToName, null, false));
        table.addElement(tr);
        tdFormElement.addElement(table);
        tdFormElement.addElement(new Input(Input.HIDDEN, "relationName", relationName));
        tdFormElement.addElement(new Input(Input.HIDDEN, "executorId", Long.toString(executorId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, "success", "/manage_executor_relation_left.do"));
        tdFormElement.addElement(new Input(Input.HIDDEN, "failure", "/create_relation_left_executor.do"));
    }

    private Collection<Executor> getExecutors() throws JspException {
        Set<Executor> result = new HashSet<Executor>();
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            Executor ex = executorService.getExecutor(getSubject(), executorId);
            result.add(ex);
            BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
            for (Executor executor : executorService.getExecutorGroups(getSubject(), ex, batchPresentation, false)) {
                result.add(executor);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return result;
    }

    private String getExecutorName() throws JspException {
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            return executorService.getExecutor(getSubject(), executorId).getName();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
