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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public class AjaxCommandServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AjaxCommandServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Got ajax request: " + request.getQueryString());
        try {
            String command = request.getParameter("command");
            User user = Commons.getUser(request.getSession());
            StringBuffer result = new StringBuffer();
            if ("getParamDialogData".equals(command)) {
                result.append("[");

                String rendererClassName = request.getParameter("renderer");
                ParamRenderer renderer = ClassLoaderUtil.instantiate(rendererClassName);
                List<String[]> data = renderer.loadJSEditorData(user);

                for (int i = 0; i < data.size(); i++) {
                    if (i != 0) {
                        result.append(", ");
                    }
                    String[] strings = data.get(i);
                    result.append("{\"value\":\"").append(normalize(strings[0])).append("\", \"text\": \"").append(normalize(strings[1]))
                            .append("\"}");
                }
                result.append("]");
            } else {
                log.warn("Request not handled, unknown command '" + command + "'");
            }
            response.getOutputStream().write(result.toString().getBytes(Charsets.UTF_8));
        } catch (Exception e) {
            log.error("command", e);
            throw new ServletException(e);
        }
    }

    private String normalize(String data) {
        if (data.contains("\"")) {
            return data.replaceAll("\"", "'");
        }
        return data;
    }
}
