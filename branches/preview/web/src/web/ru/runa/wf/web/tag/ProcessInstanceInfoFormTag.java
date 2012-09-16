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
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.Element;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.presentation.ProcessDefinitionClassPresentation;
import ru.runa.wf.presentation.ProcessInstanceClassPresentation;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.action.CancelProcessInstanceAction;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wf.web.forms.format.DateTimeFormat;

/**
 * Created on 29.11.2004
 * 
 * @jsp.tag name = "processInstanceInfoForm" body-content = "JSP"
 */
public class ProcessInstanceInfoFormTag extends ProcessInstanceBaseFormTag {
    private static final long serialVersionUID = -1275657878697999574L;

    Long taskId;

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    protected boolean isVisible() {
        return true;
    }

    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_CANCEL_INSTANCE, pageContext);
    }

    protected boolean isFormButtonVisible() throws JspException {
        return !getProcessInstance().isEnded();
    }

    protected void fillFormData(TD tdFormElement) throws JspException {
        ProcessInstanceStub processInstanceStub = getProcessInstance();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(ProcessDefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));

        Element processDefinitionHref;
        try {
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            ProcessDefinition definitionStub = definitionService
                    .getProcessDefinitionStub(getSubject(), processInstanceStub.getProcessDefinitionNativeId());
            String url = Commons.getActionUrl(ru.runa.wf.web.Resources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME, String
                    .valueOf(definitionStub.getNativeId()), pageContext, PortletUrl.Render);
            processDefinitionHref = new A(url, processInstanceStub.getName());
        } catch (AuthorizationException e1) {
            processDefinitionHref = new StringElement(processInstanceStub.getName());
        } catch (Exception e) {
            throw new JspException(e);
        }
        nameTR.addElement(new TD(processDefinitionHref).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR instanceIdTR = new TR();
        table.addElement(instanceIdTR);
        String idName = Messages.getMessage(ProcessInstanceClassPresentation.PROCESS_INSTANCE_BATCH_PRESENTATION_ID, pageContext);
        instanceIdTR.addElement(new TD(idName).setClass(Resources.CLASS_LIST_TABLE_TD));
        instanceIdTR.addElement(new TD(new Long(processInstanceStub.getId()).toString()).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        String definitionVersion = Messages.getMessage(ProcessDefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION, pageContext);
        versionTR.addElement(new TD(definitionVersion).setClass(Resources.CLASS_LIST_TABLE_TD));
        versionTR.addElement(new TD(String.valueOf(processInstanceStub.getVersion())).setClass(Resources.CLASS_LIST_TABLE_TD));

        DateTimeFormat dateTimeFormat = new DateTimeFormat();
        TR startedTR = new TR();
        table.addElement(startedTR);
        String startedName = Messages.getMessage(ProcessInstanceClassPresentation.PROCESS_INSTANCE_BATCH_PRESENTATION_STARTED, pageContext);
        startedTR.addElement(new TD(startedName).setClass(Resources.CLASS_LIST_TABLE_TD));
        startedTR.addElement(new TD(dateTimeFormat.format(processInstanceStub.getStartDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

        if (processInstanceStub.isEnded()) {
            TR endedTR = new TR();
            table.addElement(endedTR);
            String endedName = Messages.getMessage(ProcessInstanceClassPresentation.PROCESS_INSTANCE_BATCH_PRESENTATION_ENDED, pageContext);
            endedTR.addElement(new TD(endedName).setClass(Resources.CLASS_LIST_TABLE_TD));
            endedTR.addElement(new TD(dateTimeFormat.format(processInstanceStub.getEndDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        try {
            ProcessInstanceStub parentProcessInstance = executionService.getSuperProcessInstanceStub(getSubject(), getIdentifiableId());
            if (parentProcessInstance != null) {
                TR parentTR = new TR();
                table.addElement(parentTR);
                String parentNameString = Messages.getMessage(Messages.LABEL_PARENT_PROCESS, pageContext);
                parentTR.addElement(new TD(parentNameString).setClass(Resources.CLASS_LIST_TABLE_TD));
                TD td = new TD();
                td.setClass(Resources.CLASS_LIST_TABLE_TD);
                Element inner;
                String parentProcessDefinitionName = parentProcessInstance.getName();
                if (checkReadable(parentProcessInstance)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(IdForm.ID_INPUT_NAME, String.valueOf(parentProcessInstance.getId()));
                    params.put(TaskIdForm.TASK_ID_INPUT_NAME, String.valueOf(taskId));
                    params.put("childProcessId", String.valueOf(processInstanceStub.getId()));
                    inner = new A(Commons.getActionUrl(ShowGraphModeHelper.getManageProcessInstanceAction(), params, pageContext, PortletUrl.Render),
                            parentProcessDefinitionName);
                } else {
                    inner = new StringElement(parentProcessDefinitionName);
                }
                td.addElement(inner);
                parentTR.addElement(td);
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    private boolean checkReadable(ProcessInstanceStub parentProcessInstance) throws AuthorizationException, AuthenticationException {
        AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance().getAuthorizationService();
        return authorizationService.isAllowed(getSubject(), ProcessInstancePermission.READ, parentProcessInstance);
    }

    protected Permission getPermission() {
        return ProcessInstancePermission.CANCEL_INSTANCE;
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS_INSTANCE, pageContext);
    }

    public String getAction() {
        return CancelProcessInstanceAction.ACTION_PATH;
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.CANCEL_PROCESS_INSTANCE_PARAMETER;
    }
}
