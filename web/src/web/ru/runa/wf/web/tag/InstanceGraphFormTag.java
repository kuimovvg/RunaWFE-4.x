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
package ru.runa.wf.web.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.af.Permission;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.action.ProcessInstanceGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;

/**
 * Created on 15.04.2004
 * 
 * @jsp.tag name = "instanceGraphForm"
 */
public class InstanceGraphFormTag extends ProcessInstanceBaseFormTag {
    private static final long serialVersionUID = -2668305021294162818L;

    private Long taskId;
    private Long childProcessId;

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    public void setChildProcessId(Long childProcessId) {
        this.childProcessId = childProcessId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getChildProcessId() {
        return childProcessId;
    }

    @Override
    protected void fillFormData(final TD formDataTD) throws JspException {
//        if (!isProcessDefinitionExist()) {
//            return;
//        }
        Map<String, String> params = new HashMap<String, String>();
        params.put(IdForm.ID_INPUT_NAME, String.valueOf(getProcessInstance().getId()));
        params.put("childProcessId", String.valueOf(childProcessId));
        if (taskId > 0) {
            params.put(TaskIdForm.TASK_ID_INPUT_NAME, String.valueOf(taskId));
        }
        String href = Commons.getActionUrl(ProcessInstanceGraphImageAction.ACTION_PATH, params, pageContext, PortletUrl.Resource);
        IMG img = new IMG();
        img.setID("graph");
        img.setSrc(href);
        img.setBorder(0);
        addImageActions(formDataTD, img);
        formDataTD.addElement(img);
    }

    /**
     * Adds various actions to image: Links to subprocesses, tool tips for collapsed states and so on.
     * 
     * @param formDataTD
     *            Root form element
     * @param img
     *            Process graph image.
     */
    private void addImageActions(final TD formDataTD, IMG img) throws JspException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            List<GraphElementPresentation> elements = executionService.getProcessInstanceGraphElements(getSubject(), getIdentifiableId());
            InstanceGraphElementPresentationVisitor operation = new InstanceGraphElementPresentationVisitor(taskId, pageContext, formDataTD);
            for (GraphElementPresentation graphElementPresentation : elements) {
                graphElementPresentation.visit(operation);
            }
            if (!operation.getResultMap().isEmpty()) {
                formDataTD.addElement(operation.getResultMap());
                img.setUseMap("#processInstanceMap");
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    @Override
    protected Permission getPermission() {
        return ProcessInstancePermission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected boolean isVisible() throws JspException {
        return true;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS_GRAPH, pageContext);
    }
}
