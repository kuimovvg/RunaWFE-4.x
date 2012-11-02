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
package ru.runa.wf.logic.bot.assigner;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wf.logic.bot.TaskHandlerException;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;

public class AssignerSettingsXmlParser {

    private static final String CONDITION_ELEMENT_NAME = "condition";

    private static final String SWIMLANE_ELEMENT_NAME = "swimlaneName";
    private static final String FUNCTION_ELEMENT_NAME = "function";
    private static final String VARIABLE_ELEMENT_NAME = "variableName";

    private static final String XSD_PATH = "/assigner.xsd";

    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    private AssignerSettingsXmlParser() {
        //prevents direct object instantiation
    }

    public static AssignerSettings read(InputStream inputStream) throws TaskHandlerException {
        try {
            Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);

            AssignerSettings assignerSettings = new AssignerSettings();
            NodeList nodeList = document.getElementsByTagName(CONDITION_ELEMENT_NAME);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node conditionNode = nodeList.item(i);

                String swimlaneName = null;
                String functionClassName = null;
                String variableName = null;

                NodeList childNodeList = conditionNode.getChildNodes();
                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Node child = childNodeList.item(j);
                    if (child.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if (SWIMLANE_ELEMENT_NAME.equals(child.getNodeName())) {
                        swimlaneName = child.getFirstChild().getNodeValue();
                    } else if (FUNCTION_ELEMENT_NAME.equals(child.getNodeName())) {
                        functionClassName = child.getFirstChild().getNodeValue();
                    } else if (VARIABLE_ELEMENT_NAME.equals(child.getNodeName())) {
                        variableName = child.getFirstChild().getNodeValue();
                    } else {
                        // ignore #text nodes
                    }
                }
                assignerSettings.addAssignerCondition(new AssignerSettings.Condition(swimlaneName, functionClassName, variableName));
            }
            return assignerSettings;
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
