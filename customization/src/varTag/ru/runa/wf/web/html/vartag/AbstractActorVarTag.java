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
package ru.runa.wf.web.html.vartag;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 09.05.2005
 * 
 */
public abstract class AbstractActorVarTag implements VarTag {
    private static final Log log = LogFactory.getLog(AbstractActorVarTag.class);

    final public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        if (var == null) {
            log.warn("Vartag variable is not set: " + varName);
            return "<p class='error'>null</p>";
        }
        try {
            Long code = parseExecutorCode(varName, var);
            Actor actor = getActor(subject, code);
            return actorToString(actor);
        } catch (WorkflowFormProcessingException e) {
            if (var.toString().startsWith("G")) {
                log.warn("Value " + var + " was provided to display actor tag");
                Long groupId = parseExecutorCode(varName, var.toString().substring(1));
                return "<p style='color: blue;'>" + getGroup(subject, groupId).getName() + "</p>";
            } else {
                throw e;
            }
        } catch (ExecutorOutOfDateException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (AuthorizationException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    public abstract String actorToString(Actor actor);

    private Actor getActor(Subject subject, Long code) throws AuthenticationException, AuthorizationException, ExecutorOutOfDateException {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        return executorService.getActorByCode(subject, code);
    }

    private Group getGroup(Subject subject, Long id) throws AuthenticationException, WorkflowFormProcessingException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            return executorService.getGroup(subject, id);
        } catch (AuthorizationException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (ExecutorOutOfDateException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    private Long parseExecutorCode(String varName, Object var) throws WorkflowFormProcessingException {
        if (var instanceof String) {
            try {
                return new Long((String) var);
            } catch (NumberFormatException exception) {
                throw VarTagUtils.createTypeMismatchException(varName, var, this.getClass(), Long.class);
            }
        } else if (var instanceof Number) {
            return ((Number) var).longValue();
        }
        throw VarTagUtils.createTypeMismatchException(varName, var, this.getClass(), String.class);
    }
}
