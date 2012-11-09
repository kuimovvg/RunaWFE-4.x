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
package ru.runa.wf.logic.bot.updatepermission;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.wf.logic.bot.TaskHandlerException;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionNotFoundException;

import com.google.common.collect.Lists;

public class UpdatePermissionsXmlParser {
    private static final String CONDITION_ELEMENT_NAME = "condition";

    private static final String CONDITION_VAR_NAME_ATTRIBUTE_NAME = "varName";

    private static final String CONDITION_VAR_VALUE_ATTRIBUTE_NAME = "varValue";

    private static final String ORGFUNCTION_ELEMENT_NAME = "orgFunction";

    private static final String METHOD_ELEMENT_NAME = "method";

    public static final String PERMISSION_VARIABLE_ELEMENT_NAME = "permission";

    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver("update-permissions.xsd");

    private UpdatePermissionsXmlParser() {
        // prevents direct object instantiation
    }

    public static UpdatePermissionsSettings read(InputStream inputStream) throws TaskHandlerException {
        try {
            Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);

            String[] orgFunctions = getElementValues(document, ORGFUNCTION_ELEMENT_NAME);
            String method = getElementValue(document, METHOD_ELEMENT_NAME);
            List<Permission> permissions = getPermissions(getElementValues(document, PERMISSION_VARIABLE_ELEMENT_NAME));
            UpdatePermissionsSettings settings = new UpdatePermissionsSettings(orgFunctions, method, permissions);

            NodeList nodeList = document.getElementsByTagName(CONDITION_ELEMENT_NAME);
            if (nodeList.getLength() > 0) {
                Element conditionNode = (Element) nodeList.item(0);
                settings.setCondition(conditionNode.getAttribute(CONDITION_VAR_NAME_ATTRIBUTE_NAME),
                        conditionNode.getAttribute(CONDITION_VAR_VALUE_ATTRIBUTE_NAME));
            }
            return settings;
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private static List<Permission> getPermissions(String[] permissionNames) throws PermissionNotFoundException {
        List<Permission> permissions = Lists.newArrayListWithExpectedSize(permissionNames.length);
        for (int i = 0; i < permissionNames.length; i++) {
            permissions.add(ProcessPermission.CANCEL_PROCESS.getPermission(permissionNames[i]));
        }
        return permissions;
    }

    private static String getElementValue(Document document, String elementName) {
        NodeList nodeList = document.getElementsByTagName(elementName);
        return nodeList.item(0).getChildNodes().item(0).getNodeValue();
    }

    private static String[] getElementValues(Document document, String elementName) {
        NodeList nodeList = document.getElementsByTagName(elementName);
        String[] values = new String[nodeList.getLength()];
        for (int i = 0; i < values.length; i++) {
            values[i] = nodeList.item(i).getChildNodes().item(0).getNodeValue();
        }
        return values;
    }
}
