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
package ru.runa.wf.logic.bot.cr;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.SimpleErrorHandler;

public class ConfigXmlParser {
    private static final String JNDI_NAME_ATTRIBUTE_NAME = "name";
    private static final String USERNAME_ATTRIBUTE_NAME = "username";
    private static final String PASSWORD_ATTRIBUTE_NAME = "password";
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String OPERATION_ATTRIBUTE_NAME = "operation";
    private static final String VARIABLE_ATTRIBUTE_NAME = "variableName";
    private static final String PATH_ATTRIBUTE_NAME = "path";
    private static final String FILE_ATTRIBUTE_NAME = "fileName";

    public static JcrTaskConfig parse(InputStream inputStream) {
        try {
            Document document = getDocument(inputStream);
            String repositoryName = document.getDocumentElement().getAttribute(JNDI_NAME_ATTRIBUTE_NAME);
            String userName = document.getDocumentElement().getAttribute(USERNAME_ATTRIBUTE_NAME);
            String password = document.getDocumentElement().getAttribute(PASSWORD_ATTRIBUTE_NAME);

            JcrTaskConfig config = new JcrTaskConfig(repositoryName, userName, password);

            NodeList taskNodeList = document.getElementsByTagName(TASK_ELEMENT_NAME);
            for (int i = 0; i < taskNodeList.getLength(); i++) {
                Element taskNode = (Element) taskNodeList.item(i);
                String operationName = taskNode.getAttribute(OPERATION_ATTRIBUTE_NAME);
                String variableName = taskNode.getAttribute(VARIABLE_ATTRIBUTE_NAME);
                String path = taskNode.getAttribute(PATH_ATTRIBUTE_NAME);
                String fileName = taskNode.getAttribute(FILE_ATTRIBUTE_NAME);
                JcrTask task = new JcrTask(operationName, variableName, path, fileName);
                config.addTask(task);
            }
            return config;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    private static Document getDocument(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        //factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(SimpleErrorHandler.getInstance());
        return documentBuilder.parse(is);
    }
}
