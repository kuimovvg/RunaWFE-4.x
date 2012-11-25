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
package ru.runa.wf.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;

public class AjaxFreemarkerTagServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AjaxFreemarkerTagServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Got ajax request: " + req.getQueryString());
        long startTime = System.currentTimeMillis();
        try {
            String tagId = req.getParameter("tag");
            AjaxFreemarkerTag ajaxTag = (AjaxFreemarkerTag) req.getSession().getAttribute(tagId);
            if (ajaxTag == null) {
                throw new NullPointerException("No tag found in session: " + tagId);
            }
            ajaxTag.processAjaxRequest(req, resp);
        } catch (Exception e) {
            log.error("", e);
            throw new ServletException(e);
        }
        long endTime = System.currentTimeMillis();
        log.debug("Request processed for (ms): " + (endTime - startTime));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.warn("ajax post request will not be processed");
    }
}
