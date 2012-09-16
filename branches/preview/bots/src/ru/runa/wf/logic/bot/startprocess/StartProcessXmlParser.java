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
package ru.runa.wf.logic.bot.startprocess;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.commons.xml.PathEntityResolver;
import ru.runa.commons.xml.XMLHelper;

public class StartProcessXmlParser {
    private static final String PROCESS_ELEMENT_NAME = "process";

    private static final String TITLE_ATTRIBUTE_NAME = "name";

    private static final String VARIABLE_ELEMENT_NAME = "variable";

    private static final String VARFROM_ATTRIBUTE_NAME = "from";

    private static final String VARTO_ATTRIBUTE_NAME = "to";

    private static final String STARTED_PROCESS_ID = "started-process-id";

    private static final String VARIABLE_NAME = "variable-name";

    private static final String XSD_PATH = "/process-start.xsd";

    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    /**
     * Parses StartProcessXmlHandler configuration
     * 
     * @param configurationPath
     * @return
     */
    public static StartProcessTask[] parse(String configurationPath) throws StartProcessTaskXmlParserException {
        try {
            Document document = XMLHelper.getDocument(configurationPath, PATH_ENTITY_RESOLVER);
            StartProcessTask[] startProcessTask = parseStartProcess(document);
            return startProcessTask;
        } catch (Exception e) {
            throw new StartProcessTaskXmlParserException(e);
        }
    }

    public static StartProcessTask[] parse(InputStream is) throws StartProcessTaskXmlParserException {
        try {
            Document document = XMLHelper.getDocument(is, PATH_ENTITY_RESOLVER);
            StartProcessTask[] startProcessTask = parseStartProcess(document);
            return startProcessTask;
        } catch (Exception e) {
            throw new StartProcessTaskXmlParserException(e);
        }
    }

    private static String getStartedProcessIdName(Element processElement) {
        NodeList spIdNodes = processElement.getElementsByTagName(STARTED_PROCESS_ID);
        String startedProcessId = null;
        if (spIdNodes != null && spIdNodes.getLength() > 0) {
            Node spIdNode = spIdNodes.item(0);
            NamedNodeMap nnm = spIdNode.getAttributes();
            startedProcessId = nnm.getNamedItem(VARIABLE_NAME).getNodeValue();
        }
        return startedProcessId;
    }

    private static StartProcessTask[] parseStartProcess(Document document) {
        NodeList processElementList = document.getElementsByTagName(PROCESS_ELEMENT_NAME);
        StartProcessTask[] startProcessTasks = new StartProcessTask[processElementList.getLength()];
        for (int i = 0; i < startProcessTasks.length; i++) {
            Element processElement = (Element) processElementList.item(i);
            String title = processElement.getAttribute(TITLE_ATTRIBUTE_NAME);
            Variable[] variables = parseProcessVariables(processElement);
            String startedProcessId = getStartedProcessIdName(processElement);
            startProcessTasks[i] = new StartProcessTask(title, variables, startedProcessId);
        }
        return startProcessTasks;
    }

    private static Variable[] parseProcessVariables(Element processElement) {
        NodeList variableElementList = processElement.getElementsByTagName(VARIABLE_ELEMENT_NAME);
        Variable[] variables = new Variable[variableElementList.getLength()];
        for (int j = 0; j < variables.length; j++) {
            Element variableElement = (Element) variableElementList.item(j);
            String varFrom = variableElement.getAttribute(VARFROM_ATTRIBUTE_NAME);
            String varTo = variableElement.getAttribute(VARTO_ATTRIBUTE_NAME);
            variables[j] = new Variable(varFrom, varTo);
        }
        return variables;
    }
}
