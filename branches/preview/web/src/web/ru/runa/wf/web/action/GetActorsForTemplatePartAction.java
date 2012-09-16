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
package ru.runa.wf.web.action;

import java.io.OutputStream;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.common.base.Charsets;

import ru.runa.af.Actor;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.commons.IOCommons;
import ru.runa.wf.web.html.vartag.AbstractActorComboBoxVarTag;

/**
 * @struts:action path="/unsecuredGetActors" name="getActors" validate="false"
 */
public class GetActorsForTemplatePartAction extends Action {

    protected static final Log log = LogFactory.getLog(GetActorsForTemplatePartAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer xmlBuffer = new StringBuffer("<actors>\n");
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            String dataSourceName = request.getParameter("dataSource");
            String actorVarTagClassName = dataSourceName.substring(0, dataSourceName.indexOf("_"));
            String varName = "staff";
            AbstractActorComboBoxVarTag actorVarTag = (AbstractActorComboBoxVarTag) Class.forName(actorVarTagClassName).newInstance();
            List<Actor> actors = actorVarTag.getActors(subject, varName, null);
            for (Actor actor : actors) {
                xmlBuffer.append("<actor id=\"").append(actor.getCode()).append("\" name=\"").append(actor.getFullName()).append("\"/>\n");
            }
        } catch (InvalidSessionException e) {
            log.warn("No user session");
            // for structure definition
            xmlBuffer.append("<actor id=\"0\" name=\"Tiberius Claudius Drusus\"/>\n");
            xmlBuffer.append("<actor id=\"1\" name=\"Octavia Minor\"/>\n");
        } catch (Throwable e) {
            xmlBuffer.append("<actor id=\"-1000\" name=\"Error:").append(e.getMessage()).append("\"/>\n");
            log.error(e.getMessage(), e);
        }
        xmlBuffer.append("</actors>");
        response.setContentType("text/xml");
        OutputStream os = response.getOutputStream();
        os.write(xmlBuffer.toString().getBytes(Charsets.UTF_8));
        os.flush();
        return null;
    }
}
