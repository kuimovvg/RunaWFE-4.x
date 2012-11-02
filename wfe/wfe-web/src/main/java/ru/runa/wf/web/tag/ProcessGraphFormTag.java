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

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.action.ProcessGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.security.Permission;

import com.google.common.collect.Maps;

/**
 * Created on 15.04.2004
 * 
 * @jsp.tag name = "processGraphForm" body-content = "empty"
 */
public class ProcessGraphFormTag extends ProcessBaseFormTag {
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
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getProcess().getId());
        params.put("childProcessId", childProcessId);
        params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
        String href = Commons.getActionUrl(ProcessGraphImageAction.ACTION_PATH, params, pageContext, PortletUrlType.Resource);
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
            ExecutionService executionService = DelegateFactory.getExecutionService();
            List<GraphElementPresentation> elements = executionService.getProcessGraphElements(getSubject(), getIdentifiableId());
            ProcessGraphElementPresentationVisitor operation = new ProcessGraphElementPresentationVisitor(taskId, pageContext, formDataTD);
            for (GraphElementPresentation graphElementPresentation : elements) {
                graphElementPresentation.visit(operation);
            }
            if (!operation.getResultMap().isEmpty()) {
                formDataTD.addElement(operation.getResultMap());
                img.setUseMap("#processMap");
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
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
