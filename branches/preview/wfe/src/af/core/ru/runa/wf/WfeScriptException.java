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
package ru.runa.wf;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Created on 30.09.2005
 */
public class WfeScriptException extends Exception {

    private static final long serialVersionUID = -5168439440487264871L;
    private static final String FAIELD_TO_HANDLE_ELEMENT = "Failed to handle element ";
    private static final String UNKNOWN_ELEMENT_MESSAGE = "Unkown Element ";
    private static final String BECAUSE_OF_ERROR_MESSAGE = "Because of error: ";

    public WfeScriptException(String msg) {
        super(msg);
    }

    public WfeScriptException(Element element, Throwable cause) {
        super(FAIELD_TO_HANDLE_ELEMENT + getTextForElement(element) + " " + BECAUSE_OF_ERROR_MESSAGE + cause.getMessage(), cause);
    }

    /**
     * Use for unknown element exception only
     * 
     * @param message
     * @param element
     */
    public WfeScriptException(Element element) {
        super(UNKNOWN_ELEMENT_MESSAGE + getTextForElement(element));
    }

    private static String getTextForElement(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("<" + element.getNodeName() + " ");
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            sb.append(attributes.item(i).getNodeName() + "=\"" + attributes.item(i).getNodeValue() + "\" ");
        }
        sb.append(" >");
        return sb.toString();
    }
}
