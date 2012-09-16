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

import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on Mar 24, 2006
 * 
 */
public abstract class AbstractActorComboBoxVarTag implements VarTag {
    private static final Log log = LogFactory.getLog(AbstractActorComboBoxVarTag.class);

    protected Select createSelect(String selectName, List<Actor> actors, Actor defaultSelectedActor) {
        Select select = new Select();
        select.setName(selectName);
        try {
            for (Actor actor : actors) {
                String actorPropertyToUseAsValue = BeanUtils.getProperty(actor, getActorPropertyToUse());
                String actorPropertyToDisplay = BeanUtils.getProperty(actor, getActorPropertyToDisplay());
                Option option = new Option(actorPropertyToUseAsValue).addElement(actorPropertyToDisplay);
                select.addElement(option);
                if (defaultSelectedActor.equals(actor)) {
                    option.setSelected(true);
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
        return select;
    }

    @Override
    public String getHtml(Subject subject, String varName, Object varValue, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        try {
            StringBuilder htmlContent = new StringBuilder();

            List<Actor> actors = getActors(subject, varName, varValue);
            Actor defaultActor = null;
            if (varValue != null) {
                try {
                    defaultActor = DelegateFactory.getInstance().getExecutorService()
                            .getActorByCode(subject, Long.valueOf((String) varValue));
                } catch (Throwable e) {
                    log.warn("Unable to fetch actor value", e);
                }
            }
            if (defaultActor == null) {
                defaultActor = DelegateFactory.getInstance().getAuthenticationService().getActor(subject);
            }

            htmlContent.append(createSelect(varName, actors, defaultActor).toString());

            return htmlContent.toString();
        } catch (AuthorizationException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (ExecutorOutOfDateException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

    public abstract List<Actor> getActors(Subject subject, String varName, Object varValue) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException;

    public String getActorPropertyToUse() {
        return "code";
    }

    public String getActorPropertyToDisplay() {
        return "fullName";
    }

}
