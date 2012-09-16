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
package ru.runa.common.web.tag;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;

import ru.runa.common.web.ActionExceptionHelper;

/**
 * Created on 07.09.2004
 * 
 * @jsp.tag name = "globalExceptions" body-content = "empty" description = "Tag translate global JSP exceptions declared in web.xml into Struts ActionErrors and save them"
 */
public class GlobalExceptions extends TagSupport {
    private static final long serialVersionUID = 7404767259810706586L;

    private static final Log log = LogFactory.getLog(GlobalExceptions.class);

    private static final String EXCEPTION_REQUEST_ATTRIBUTE_NAME = "javax.servlet.error.exception";

    private static final String ERROR_CODE_REQUEST_ATTRIBURE_NAME = "javax.servlet.error.status_code";

    private static final String REQUEST_URI_REQUEST_ATTRIBURE_NAME = "javax.servlet.error.request_uri";

    public int doStartTag() {
        Exception exception = (Exception) pageContext.getRequest().getAttribute(EXCEPTION_REQUEST_ATTRIBUTE_NAME);
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                log.debug(exception);
            }
            ActionExceptionHelper.addException(getActionErrors(), exception);
        }
        //TODO Check whether such error could occur? 
        Integer code = ((Integer) pageContext.getRequest().getAttribute(ERROR_CODE_REQUEST_ATTRIBURE_NAME));
        if (code != null) {
            String uri = (String) pageContext.getRequest().getAttribute(REQUEST_URI_REQUEST_ATTRIBURE_NAME);
            log.debug("Code: " + code + ", requested uri: " + uri);
        }
        return SKIP_BODY;
    }

    private ActionErrors getActionErrors() {
        ActionErrors messages = (ActionErrors) pageContext.getRequest().getAttribute(Globals.ERROR_KEY);
        if (messages == null) {
            messages = new ActionErrors();
            pageContext.getRequest().setAttribute(Globals.ERROR_KEY, messages);
        }
        return messages;
    }
}
