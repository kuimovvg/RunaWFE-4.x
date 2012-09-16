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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class UsersEmailVarTag extends SendEmailAction_Common implements VarTag {
    
    @Autowired
    private ExecutorLogic executorLogic;

    public boolean isEmailCorrect(String email) {
        return email != null && email.length() > 0;
    }

    public Set<Actor> getEmailedForActor(Actor actor, TaskInstance taskInstance) {
        Set<Actor> retVal = new HashSet<Actor>();
        if (actor.isActive()) {
            if (isEmailCorrect(actor.getEmail())) {
                retVal.add(actor);
            }
            return retVal;
        }

        /*Map<Substitution, Set<Actor>> sub = new SubstitutionLogic().getSubstitutors(actor);
        for(Substitution key : sub.keySet()){
        	if(key.isEnabled() && (key.getCriteria().equals(Substitution.ALWAYS_CRITERIA) || key.getCriteria().equals(token.getProcessInstance().getDefinition().getName() + "." + token.getState().getSwimlane().getName()))){
        		Set<Actor> substitutors = sub.get(key);
        		for(Actor substitutor : substitutors){
        			if(!substitutor.isActive())
        				continue;
        			if(isEmailCorrect(substitutor.getEmail()))
        				retVal.add(substitutor);
        		}
        		return retVal;
        	}
        }*/
        return retVal;
    }

    public String getHtml(Subject subject, String varName, Object varValue, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        try {
            StringBuilder retVal = new StringBuilder();
            String executorCode = getExecutorCode(varValue);
            Executor executor = new ExecutorLogic().getActorByCode(subject, Long.parseLong(executorCode));
            TaskInstance taskInstance = getToken(varValue);
            if (executor == null || taskInstance == null) {
                return "";
            }
            if (executor instanceof Actor) {
                Set<Actor> emailed = getEmailedForActor((Actor) executor, taskInstance);
                for (Actor actor : emailed) {
                    retVal.append(actor.getEmail()).append(", ");
                }
                return retVal.toString();
            } else {
                boolean isActive = false;

                List<Executor> childrens = executorLogic.getAllExecutorsFromGroup(subject, (Group) executor);
                for (Executor ex : childrens) {
                    if (ex instanceof Actor && ((Actor) ex).isActive()) {
                        isActive = true;
                        break;
                    }
                }

                if (isActive) {
                    for (Executor ex : childrens) {
                        if (ex instanceof Actor && ((Actor) ex).isActive()) {
                            String email = ((Actor) ex).getEmail();
                            if (isEmailCorrect(email)) {
                                retVal.append(email).append(",");
                            }
                        }
                    }
                    return retVal.toString();
                } else {
                    Set<Actor> emailed = new HashSet<Actor>();
                    for (Executor ex : childrens) {
                        if (ex instanceof Actor) {
                            emailed.addAll(getEmailedForActor((Actor) ex, taskInstance));
                        }
                    }
                    for (Actor actor : emailed) {
                        retVal.append(actor.getEmail()).append(',');
                    }
                    return retVal.toString();
                }
            }
        } catch (ExecutorOutOfDateException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (AuthorizationException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }
}
