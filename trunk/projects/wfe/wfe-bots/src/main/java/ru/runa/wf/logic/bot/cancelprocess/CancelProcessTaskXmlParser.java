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
package ru.runa.wf.logic.bot.cancelprocess;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;

/**
 * Created on 01.04.2005
 * 
 */
public class CancelProcessTaskXmlParser {
    private static final String PROCESS_TO_CANCEL = "processToCancel";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String TASK_HANDLER_CONFIGURATION_ATTRIBUT_NAME = "taskHandlerConfiguration";
    private static final String PROCESS_ID_VARIABLE_ATTRIBUTE_NAME = "processIdVariable";
    private static final String PROCESSES_ELEMENT_NAME = "processes";
    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver("cancel-process.xsd");

    /**
     * Parses DatabaseTaskHandler configuration
     * 
     * @param configurationPath
     * @return
     */
    public static CancelProcessTask parse(String configurationPath) throws CancelProcessTaskXmlParserException {
        try {
            Document document = XMLHelper.getDocument(configurationPath, PATH_ENTITY_RESOLVER);
            return parseDatabaseTasks(document);
        } catch (Exception e) {
            throw new CancelProcessTaskXmlParserException(e);
        }
    }

    public static CancelProcessTask parse(InputStream configurationInputStream) throws CancelProcessTaskXmlParserException {
        try {
            Document document = XMLHelper.getDocument(configurationInputStream, PATH_ENTITY_RESOLVER);
            return parseDatabaseTasks(document);
        } catch (Exception e) {
            throw new CancelProcessTaskXmlParserException(e);
        }
    }

    private static CancelProcessTask parseDatabaseTasks(Document document) {
        NodeList processesElementList = document.getElementsByTagName(PROCESSES_ELEMENT_NAME);
        String processIdVariable = ((Element) processesElementList.item(0)).getAttribute(PROCESS_ID_VARIABLE_ATTRIBUTE_NAME);
        NodeList taskElementList = document.getElementsByTagName(PROCESS_TO_CANCEL);
        Map<String, String> taskMap = new HashMap<String, String>(taskElementList.getLength());
        for (int i = 0; i < taskElementList.getLength(); i++) {
            Element processEllement = (Element) taskElementList.item(i);
            String name = processEllement.getAttribute(NAME_ATTRIBUTE_NAME);
            String handlerConfiguration = processEllement.getAttribute(TASK_HANDLER_CONFIGURATION_ATTRIBUT_NAME);
            taskMap.put(name, handlerConfiguration);
        }
        return new CancelProcessTask(processIdVariable, taskMap);
    }
}
