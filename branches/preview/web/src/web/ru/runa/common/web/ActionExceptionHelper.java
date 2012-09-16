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
package ru.runa.common.web;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorNotInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.IncorrectBotArchiveException;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.SubstitutionOutOfDateException;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.presentation.filter.FilterFormatException;
import ru.runa.wf.DelegationClassNotFound;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveFormatException;
import ru.runa.wf.ProcessDefinitionArchiveVariableNotDefinedException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionFileDoesNotExistException;
import ru.runa.wf.ProcessDefinitionNameMismatchException;
import ru.runa.wf.ProcessDefinitionXMLFormatException;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.web.html.vartag.VarTagTypeMismatchException;

import com.google.common.base.Throwables;

/**
 * Created 27.05.2005
 * 
 */
public class ActionExceptionHelper {

    private static final Log log = LogFactory.getLog(ActionExceptionHelper.class);

    private ActionExceptionHelper() {
    }

    public static void addException(ActionMessages errors, Exception e) {
        Throwable rootCause = Throwables.getRootCause(e);
        if (rootCause instanceof Exception) {
            e = (Exception) rootCause;
        } else {
            e = new Exception(rootCause);
        }
        errors.add(ActionMessages.GLOBAL_MESSAGE, getActionMessage(e));
        // if (e instanceof InternalApplicationException) {
        log.error("web exception", e);
        // }
    }

    public static String getErrorMessage(Throwable e, PageContext pageContext) throws JspException {
        ActionMessage actionMessage = getActionMessage(e);
        return Commons.getMessage(actionMessage.getKey(), pageContext, actionMessage.getValues());
    }

    private static ActionMessage getActionMessage(Throwable e) {
        ActionMessage actionMessage;
        if (e instanceof AuthenticationException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_AUTHENTICATION);
        } else if (e instanceof AuthorizationException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_AUTHORIZATION);
        } else if (e instanceof WeakPasswordException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_PASSWORD_IS_WEAK);
        } else if (e instanceof InternalApplicationException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_INTERNAL);
        } else if (e instanceof ExecutorOutOfDateException) {
            ExecutorOutOfDateException exception = (ExecutorOutOfDateException) e;
            if (exception.getExecutorClass().equals(Actor.class)) {
                actionMessage = new ActionMessage(Messages.EXCEPTION_ACTOR_DOES_NOT_EXISTS, exception.getExecutorName());
            } else if (exception.getExecutorClass().equals(Group.class)) {
                actionMessage = new ActionMessage(Messages.EXCEPTION_GROUP_DOES_NOT_EXISTS, exception.getExecutorName());
            } else {
                actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS, exception.getExecutorName());
            }
        } else if (e instanceof UnapplicablePermissionException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_INTERNAL);
        } else if (e instanceof ExecutorAlreadyInGroupException) {
            ExecutorAlreadyInGroupException exception = (ExecutorAlreadyInGroupException) e;
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_IN_GROUP, exception.getExecutorName(),
                    exception.getGroupName());
        } else if (e instanceof ExecutorAlreadyExistsException) {
            ExecutorAlreadyExistsException exception = (ExecutorAlreadyExistsException) e;
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_EXISTS, exception.getExecutorName());
        } else if (e instanceof ExecutorNotInGroupException) {
            ExecutorNotInGroupException exception = (ExecutorNotInGroupException) e;
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_IN_GROUP, exception.getExecutorName(),
                    exception.getGroupName());
        } else if (e instanceof ProcessInstanceDoesNotExistException) {
            ProcessInstanceDoesNotExistException exception = (ProcessInstanceDoesNotExistException) e;
            actionMessage = new ActionMessage(Messages.ERROR_WEB_CLIENT_INSTANCE_DOES_NOT_EXIST, exception.getName());
        } else if (e instanceof ProcessDefinitionAlreadyExistsException) {
            ProcessDefinitionAlreadyExistsException exception = (ProcessDefinitionAlreadyExistsException) e;
            actionMessage = new ActionMessage(Messages.ERROR_WEB_CLIENT_DEFINITION_ALREADY_EXISTS, exception.getName());
        } else if (e instanceof ProcessDefinitionDoesNotExistException) {
            ProcessDefinitionDoesNotExistException exception = (ProcessDefinitionDoesNotExistException) e;
            actionMessage = new ActionMessage(Messages.ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST, exception.getName());
        } else if (e instanceof ProcessDefinitionFileDoesNotExistException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_FILE_DOES_NOT_EXIST_ERROR, e.getMessage());
        } else if (e instanceof DelegationClassNotFound) {
            DelegationClassNotFound ex = (DelegationClassNotFound) e;
            actionMessage = new ActionMessage(Messages.DEFINITION_DELEGATION_CLASS_CAN_NOT_BE_FOUND_ERROR, ex.getClassName());
        } else if (e instanceof ProcessDefinitionArchiveFormatException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_ARCHIVE_FORMAT_ERROR);
        } else if (e instanceof ProcessDefinitionXMLFormatException) {
            actionMessage = new ActionMessage(Messages.DEFINITION_FILE_FORMAT_ERROR, e.getMessage());
        } else if (e instanceof ProcessDefinitionArchiveVariableNotDefinedException) {
            ProcessDefinitionArchiveVariableNotDefinedException ex = (ProcessDefinitionArchiveVariableNotDefinedException) e;
            actionMessage = new ActionMessage(Messages.DEFINITION_VARIABLE_NOT_PRESENT, ex.getVarName(), ex.getDefinedIn());
        } else if (e instanceof IOException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_INTERNAL);
        } else if (e instanceof ProcessDefinitionNameMismatchException) {
            ProcessDefinitionNameMismatchException exception = (ProcessDefinitionNameMismatchException) e;
            actionMessage = new ActionMessage(Messages.ERROR_WEB_CLIENT_DEFINITION_NAME_MISMATCH, exception.getDeployedProcessDefinitionName(),
                    exception.getGivenProcessDefinitionName());
        } else if (e instanceof TaskDoesNotExistException) {
            TaskDoesNotExistException exception = (TaskDoesNotExistException) e;
            actionMessage = new ActionMessage(Messages.ERROR_WEB_CLIENT_TASK_DOES_NOT_EXIST, exception.getName());
        } else if (e instanceof SubstitutionOutOfDateException) {
            actionMessage = new ActionMessage(Messages.SUBSTITUTION_OUT_OF_DATE);
        } else if (e instanceof InvalidSessionException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_SESSION_INVALID);
        } else if (e instanceof FilterFormatException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_TABLE_VIEW_SETUP_FORMAT_INCORRECT);
        } else if (e instanceof VarTagTypeMismatchException) {
            VarTagTypeMismatchException exception = (VarTagTypeMismatchException) e;
            String variableType = (exception.getVariableType() != null) ? exception.getVariableType().getName() : null;
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_VARTAG_TYPE_MISMATCH, exception.getVariableName(), variableType,
                    exception.getVarTagClass().getName());
        } else if (e instanceof ru.runa.wf.ProcessDefinitionTypeNotPresentException) {
            actionMessage = new ActionMessage(Messages.EXCEPTION_DEFINITION_TYPE_NOT_PRESENT);
        } else if (e instanceof TaskAlreadyAcceptedException) {
            TaskAlreadyAcceptedException exception = (TaskAlreadyAcceptedException) e;
            actionMessage = new ActionMessage(Messages.TASK_WAS_ALREADY_ACCEPTED, exception.getTaskId());
        } else if (e instanceof SuperProcessInstanceExistsException) {
            SuperProcessInstanceExistsException exc = (SuperProcessInstanceExistsException) e;
            actionMessage = new ActionMessage(Messages.INSTANCE_HAS_SUPER_PROCESS, exc.getDefinitionName(), exc.getParentDefinitionName());
        } else if (e instanceof IncorrectBotArchiveException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_INCORRECT_BOT_ARCHIVE);
        } else if (e instanceof RelationDoesNotExistsException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_RELATION_GROUP_DOESNOT_EXISTS);
        } else if (e instanceof RelationExistException) {
            actionMessage = new ActionMessage(Messages.MESSAGE_RELATION_GROUP_EXISTS, ((RelationExistException) e).getRelationName());
        } else {
            actionMessage = new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_UNKNOWN, e.toString());
        }
        return actionMessage;
    }
}
