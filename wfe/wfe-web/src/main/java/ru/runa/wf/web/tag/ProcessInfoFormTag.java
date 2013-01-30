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

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.ecs.Element;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.DefinitionService;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.format.DateTimeFormat;

import com.google.common.collect.Maps;

/**
 * Created on 29.11.2004
 * 
 * @jsp.tag name = "processInfoForm" body-content = "JSP"
 */
public class ProcessInfoFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = -1275657878697999574L;

    private Long taskId;

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_CANCEL_PROCESS, pageContext);
    }

    @Override
    protected boolean isFormButtonVisible() throws JspException {
        return !getProcess().isEnded();
    }

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        WfProcess process = getProcess();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(DefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));

        Element processDefinitionHref;
        try {
            DefinitionService definitionService = Delegates.getDefinitionService();
            WfDefinition definitionStub = definitionService.getProcessDefinition(getUser(), process.getProcessDefinitionId());
            String url = Commons.getActionUrl(ru.runa.common.WebResources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME,
                    definitionStub.getId(), pageContext, PortletUrlType.Render);
            processDefinitionHref = new A(url, process.getName());
        } catch (AuthorizationException e1) {
            processDefinitionHref = new StringElement(process.getName());
        } catch (Exception e) {
            throw new JspException(e);
        }
        nameTR.addElement(new TD(processDefinitionHref).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR processIdTR = new TR();
        table.addElement(processIdTR);
        String idName = Messages.getMessage(ProcessClassPresentation.PROCESS_BATCH_PRESENTATION_ID, pageContext);
        processIdTR.addElement(new TD(idName).setClass(Resources.CLASS_LIST_TABLE_TD));
        processIdTR.addElement(new TD(new Long(process.getId()).toString()).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        String definitionVersion = Messages.getMessage(DefinitionClassPresentation.PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION, pageContext);
        versionTR.addElement(new TD(definitionVersion).setClass(Resources.CLASS_LIST_TABLE_TD));
        versionTR.addElement(new TD(String.valueOf(process.getVersion())).setClass(Resources.CLASS_LIST_TABLE_TD));

        DateTimeFormat dateTimeFormat = new DateTimeFormat();
        TR startedTR = new TR();
        table.addElement(startedTR);
        String startedName = Messages.getMessage(ProcessClassPresentation.PROCESS_BATCH_PRESENTATION_STARTED, pageContext);
        startedTR.addElement(new TD(startedName).setClass(Resources.CLASS_LIST_TABLE_TD));
        startedTR.addElement(new TD(dateTimeFormat.format(process.getStartDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

        if (process.isEnded()) {
            TR endedTR = new TR();
            table.addElement(endedTR);
            String endedName = Messages.getMessage(ProcessClassPresentation.PROCESS_BATCH_PRESENTATION_ENDED, pageContext);
            endedTR.addElement(new TD(endedName).setClass(Resources.CLASS_LIST_TABLE_TD));
            endedTR.addElement(new TD(dateTimeFormat.format(process.getEndDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        ExecutionService executionService = Delegates.getExecutionService();
        try {
            WfProcess parentProcess = executionService.getParentProcess(getUser(), getIdentifiableId());
            if (parentProcess != null) {
                TR parentTR = new TR();
                table.addElement(parentTR);
                String parentNameString = Messages.getMessage(Messages.LABEL_PARENT_PROCESS, pageContext);
                parentTR.addElement(new TD(parentNameString).setClass(Resources.CLASS_LIST_TABLE_TD));
                TD td = new TD();
                td.setClass(Resources.CLASS_LIST_TABLE_TD);
                Element inner;
                String parentProcessDefinitionName = parentProcess.getName();
                if (checkReadable(parentProcess)) {
                    Map<String, Object> params = Maps.newHashMap();
                    params.put(IdForm.ID_INPUT_NAME, parentProcess.getId());
                    params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
                    params.put("childProcessId", process.getId());
                    inner = new A(Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render),
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

    private boolean checkReadable(WfProcess parentProcess) throws AuthorizationException, AuthenticationException {
        AuthorizationService authorizationService = ru.runa.service.delegate.Delegates.getAuthorizationService();
        return authorizationService.isAllowed(getUser(), ProcessPermission.READ, parentProcess);
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.CANCEL_PROCESS;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS, pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.CANCEL_PROCESS_PARAMETER;
    }
}
