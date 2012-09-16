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
package ru.runa.common.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.InvalidSessionException;
import ru.runa.common.web.Messages;

/**
 * This filter checks that the user session is active.
 * 
 * @web.filter name="session"
 * @web.filter-mapping url-pattern = "/*"
 */
public class HTTPSessionFilter extends HTTPFilterBase {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String query = request.getRequestURI();
        if (query.endsWith("do") && !query.endsWith("/start.do") && !query.endsWith("login.do")) {
            try {
                SubjectHttpSessionHelper.getActorSubject(request.getSession());
            } catch (InvalidSessionException e) {
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.EXCEPTION_WEB_CLIENT_SESSION_INVALID));
                request.setAttribute(Globals.ERROR_KEY, errors);
                request.getRequestDispatcher("start.do").forward(request, response);
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } catch (ServletException e) {
            if (e.getRootCause() instanceof InvalidSessionException) {
                log.warn("session expired while accessing " + query);
            }
            throw e;
        }
    }

}
