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
package ru.runa.wf.web;

import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.MultipartRequestHandler;

/**
 * Created on 21.06.2005
 *
 *
 */
public class VariablesMultipartRequestHandler implements MultipartRequestHandler {

    private ActionServlet actionServlet;
    private ActionMapping actionMapping;
    private final Hashtable<String, Object> allElements;
    private final Hashtable<String, Object> textElements;
    private final Hashtable<String, Object> fileElements;

    public VariablesMultipartRequestHandler() {
        allElements = new Hashtable<String, Object>();
        textElements = new Hashtable<String, Object>();
        fileElements = new Hashtable<String, Object>();
    }

    public void setServlet(ActionServlet arg0) {
        actionServlet = arg0;
    }

    public void setMapping(ActionMapping arg0) {
        actionMapping = arg0;
    }

    public ActionServlet getServlet() {
        return actionServlet;
    }

    public ActionMapping getMapping() {
        return actionMapping;
    }

    public void handleRequest(HttpServletRequest request) throws ServletException {
    }

    public Hashtable<String, Object> getTextElements() {
        return textElements;
    }

    public Hashtable<String, Object> getFileElements() {
        return fileElements;
    }

    public void addVariable(String variableName, String variableValue) {
        allElements.put(variableName, new String[] { variableValue });
    }

    public Hashtable<String, Object> getAllElements() {
        return allElements;
    }

    public void rollback() {
    }

    public void finish() {
    }
}
